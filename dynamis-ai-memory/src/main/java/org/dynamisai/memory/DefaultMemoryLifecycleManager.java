package org.dynamisai.memory;

import org.dynamisai.core.EntityId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class DefaultMemoryLifecycleManager implements MemoryLifecycleManager {

    private static final Logger log =
        LoggerFactory.getLogger(DefaultMemoryLifecycleManager.class);

    private final MemoryBudget defaultBudget;
    private final VectorMemoryStore archiveStore;
    private final Map<EntityId, MemoryBudget> budgets = new ConcurrentHashMap<>();

    // Active records by owner — excludes ARCHIVED (those live in archiveStore)
    // and PRUNED (gone forever)
    private final Map<EntityId, Map<UUID, MemoryRecord>> activeRecords =
        new ConcurrentHashMap<>();

    public DefaultMemoryLifecycleManager(MemoryBudget defaultBudget,
                                         VectorMemoryStore archiveStore) {
        this.defaultBudget = defaultBudget;
        this.archiveStore = archiveStore;
    }

    public DefaultMemoryLifecycleManager() {
        this(MemoryBudget.tier1(), new InHeapVectorMemoryStore());
    }

    /** Register a custom budget for a specific NPC. */
    public void registerBudget(EntityId owner, MemoryBudget budget) {
        budgets.put(owner, budget);
    }

    @Override
    public void addRawEvent(MemoryRecord record) {
        if (record.stage() != MemoryLifecycleStage.RAW_EVENT) {
            throw new IllegalArgumentException("addRawEvent only accepts RAW_EVENT records");
        }
        getActiveMap(record.owner()).put(record.id(), record);
        pruneStageIfOverBudget(record.owner(), MemoryLifecycleStage.RAW_EVENT);
    }

    @Override
    public void consolidate(EntityId owner) {
        Map<UUID, MemoryRecord> records = getActiveMap(owner);
        MemoryBudget budget = budgets.getOrDefault(owner, defaultBudget);

        // Snapshot promotable IDs first so records only advance one stage per consolidate tick.
        List<UUID> rawToShort = records.values().stream()
            .filter(r -> r.stage() == MemoryLifecycleStage.RAW_EVENT && r.importanceScore() >= 0.3f)
            .map(MemoryRecord::id)
            .toList();
        List<UUID> shortToConsolidated = records.values().stream()
            .filter(r -> r.stage() == MemoryLifecycleStage.SHORT_TERM && r.importanceScore() >= 0.5f)
            .map(MemoryRecord::id)
            .toList();

        // RAW_EVENT → SHORT_TERM
        for (UUID id : rawToShort) {
            records.computeIfPresent(id, (k, r) -> r.withStage(MemoryLifecycleStage.SHORT_TERM));
        }

        // SHORT_TERM → CONSOLIDATED (only records that were already SHORT_TERM this tick)
        for (UUID id : shortToConsolidated) {
            records.computeIfPresent(id, (k, r) -> r.withStage(MemoryLifecycleStage.CONSOLIDATED));
        }

        // CONSOLIDATED → ARCHIVED: move oldest low-access records off to archive
        List<MemoryRecord> toArchive = records.values().stream()
            .filter(r -> r.stage() == MemoryLifecycleStage.CONSOLIDATED)
            .filter(r -> r.importanceScore() < 0.4f)
            .sorted(Comparator.comparing(MemoryRecord::lastAccessed))
            .limit(5L)
            .toList();

        for (MemoryRecord r : toArchive) {
            MemoryRecord archived = r.withStage(MemoryLifecycleStage.ARCHIVED);
            archiveStore.store(archived, InHeapVectorMemoryStore.keywordEmbedding(archived.summary(), 32));
            records.remove(r.id());
            log.debug("Archived memory {} for owner {}", r.id(), owner);
        }

        // Prune each stage if over budget
        pruneStageIfOverBudget(owner, MemoryLifecycleStage.RAW_EVENT);
        pruneStageIfOverBudget(owner, MemoryLifecycleStage.SHORT_TERM);
        pruneStageIfOverBudget(owner, MemoryLifecycleStage.CONSOLIDATED);

        // Prune archived if over budget
        int archivedCount = archiveStore.countForOwner(owner);
        if (archivedCount > budget.maxArchived()) {
            List<MemoryRecord> archivedRecords = archiveStore.getAllForOwner(owner)
                .stream()
                .sorted(Comparator.comparingDouble(MemoryRecord::importanceScore))
                .limit(archivedCount - budget.maxArchived())
                .toList();
            for (MemoryRecord r : archivedRecords) {
                archiveStore.remove(r.id());
                log.debug("Pruned archived memory {} for owner {}", r.id(), owner);
            }
        }
    }

    @Override
    public List<MemoryRecord> getMemories(EntityId owner) {
        List<MemoryRecord> active = new ArrayList<>(getActiveMap(owner).values());
        active.addAll(archiveStore.getAllForOwner(owner));
        active.sort(Comparator.comparingDouble(MemoryRecord::importanceScore).reversed());
        return Collections.unmodifiableList(active);
    }

    @Override
    public List<MemoryRecord> getMemoriesAtStage(EntityId owner,
                                                 MemoryLifecycleStage stage) {
        if (stage == MemoryLifecycleStage.ARCHIVED) {
            return archiveStore.getAllForOwner(owner).stream()
                .filter(r -> r.stage() == MemoryLifecycleStage.ARCHIVED)
                .collect(Collectors.toList());
        }
        return getActiveMap(owner).values().stream()
            .filter(r -> r.stage() == stage)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<MemoryRecord> getById(UUID id) {
        for (Map<UUID, MemoryRecord> map : activeRecords.values()) {
            MemoryRecord r = map.get(id);
            if (r != null) return Optional.of(r);
        }
        return archiveStore.getById(id);
    }

    @Override
    public void purgeAll(EntityId owner) {
        activeRecords.remove(owner);
        archiveStore.getAllForOwner(owner)
            .forEach(r -> archiveStore.remove(r.id()));
        log.debug("Purged all memories for owner {}", owner);
    }

    @Override
    public MemoryStats getStats(EntityId owner) {
        Map<UUID, MemoryRecord> records = getActiveMap(owner);
        int raw = countAtStage(records, MemoryLifecycleStage.RAW_EVENT);
        int st = countAtStage(records, MemoryLifecycleStage.SHORT_TERM);
        int con = countAtStage(records, MemoryLifecycleStage.CONSOLIDATED);
        int arc = archiveStore.countForOwner(owner);
        return new MemoryStats(owner, raw, st, con, arc, raw + st + con + arc);
    }

    private Map<UUID, MemoryRecord> getActiveMap(EntityId owner) {
        return activeRecords.computeIfAbsent(owner, k -> new ConcurrentHashMap<>());
    }

    private void pruneStageIfOverBudget(EntityId owner, MemoryLifecycleStage stage) {
        MemoryBudget budget = budgets.getOrDefault(owner, defaultBudget);
        int limit = switch (stage) {
            case RAW_EVENT -> budget.maxRawEvents();
            case SHORT_TERM -> budget.maxShortTerm();
            case CONSOLIDATED -> budget.maxConsolidated();
            default -> Integer.MAX_VALUE;
        };
        Map<UUID, MemoryRecord> records = getActiveMap(owner);
        List<MemoryRecord> atStage = records.values().stream()
            .filter(r -> r.stage() == stage)
            .sorted(Comparator.comparingDouble(MemoryRecord::importanceScore))
            .toList();
        int excess = atStage.size() - limit;
        for (int i = 0; i < excess; i++) {
            records.remove(atStage.get(i).id());
            log.debug("Pruned {} memory {} for owner {}",
                stage, atStage.get(i).id(), owner);
        }
    }

    private int countAtStage(Map<UUID, MemoryRecord> records,
                             MemoryLifecycleStage stage) {
        return (int) records.values().stream()
            .filter(r -> r.stage() == stage).count();
    }
}
