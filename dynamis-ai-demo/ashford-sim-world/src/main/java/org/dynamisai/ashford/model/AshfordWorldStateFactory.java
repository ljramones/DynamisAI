/*
 * Copyright 2026 Larry Mitchell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dynamisai.ashford.model;

import io.vavr.collection.HashMap;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.dynamisai.cognition.AffectVector;
import org.dynamisai.core.EntityId;
import org.dynamisai.core.EntityState;
import org.dynamisai.core.EnvironmentState;
import org.dynamisai.core.GlobalFacts;
import org.dynamisai.core.Location;
import org.dynamisai.core.ThreatLevel;
import org.dynamisai.core.WorldSnapshot;
import org.dynamisai.planning.WorldState;

public final class AshfordWorldStateFactory {

    private static final Map<AshfordConstants.Location, Location> LOCATION_COORDS = Map.ofEntries(
        new AbstractMap.SimpleEntry<>(AshfordConstants.Location.MARKET_SQUARE, new Location(0f, 0f, 0f)),
        new AbstractMap.SimpleEntry<>(AshfordConstants.Location.CROSSED_KEYS, new Location(20f, 0f, 10f)),
        new AbstractMap.SimpleEntry<>(AshfordConstants.Location.MERCHANT_HALL, new Location(-20f, 0f, 8f)),
        new AbstractMap.SimpleEntry<>(AshfordConstants.Location.MANOR_HOUSE, new Location(35f, 0f, -15f)),
        new AbstractMap.SimpleEntry<>(AshfordConstants.Location.WATCH_HOUSE, new Location(28f, 0f, -8f)),
        new AbstractMap.SimpleEntry<>(AshfordConstants.Location.RIVER_DOCKS, new Location(5f, 0f, 35f)),
        new AbstractMap.SimpleEntry<>(AshfordConstants.Location.TEMPLE, new Location(-8f, 0f, -22f)),
        new AbstractMap.SimpleEntry<>(AshfordConstants.Location.PHYSIC_HOUSE, new Location(-15f, 0f, -10f)),
        new AbstractMap.SimpleEntry<>(AshfordConstants.Location.MILL, new Location(18f, 0f, 48f)),
        new AbstractMap.SimpleEntry<>(AshfordConstants.Location.WAREHOUSE_DISTRICT, new Location(-5f, 0f, 28f)),
        new AbstractMap.SimpleEntry<>(AshfordConstants.Location.WORKERS_QUARTER, new Location(-28f, 0f, 20f)),
        new AbstractMap.SimpleEntry<>(AshfordConstants.Location.ROAD_OUT, new Location(60f, 0f, 60f))
    );

    private AshfordWorldStateFactory() {
    }

    public static WorldState initialWorldState() {
        EntityId owner = AshfordCharacters.HARRO_VANE.id();
        WorldState ws = WorldState.withoutNav(
            owner,
            0L,
            AffectVector.neutral(),
            ThreatLevel.NONE,
            null,
            null,
            Map.of()
        );

        ws = withFlag(ws, "dole.alive", "false");
        ws = withFlag(ws, "dole.location", "RIVER_MILL");
        ws = withFlag(ws, "harro.cleanup_in_progress", "true");
        ws = withFlag(ws, "rowan.affect.guilt", "HIGH");
        ws = withFlag(ws, "oona.knowledge.cause_of_death", "KNOWN");
        ws = withFlag(ws, "oona.disclosure", "NONE");
        ws = withFlag(ws, "oona.schedule.disruption", "0");
        ws = withFlag(ws, "cael.in_town", "false");
        ws = withFlag(ws, "cael.belief.complete", "false");
        ws = withFlag(ws, "cael.belief.suspect_profile", "UNKNOWN");
        ws = withFlag(ws, "cael.belief.opponent_aware", "false");
        ws = withFlag(ws, "harro.fled", "false");
        ws = withFlag(ws, "harro.in_custody", "false");
        ws = withFlag(ws, "harro.confessed", "false");
        ws = withFlag(ws, "harro.named", "false");
        ws = withFlag(ws, "wulf.evidence.physical", "false");
        ws = withFlag(ws, "wulf.evidence.medical", "false");
        ws = withFlag(ws, "wulf.warrant.issued", "false");
        ws = withFlag(ws, "pip.cover_active", "false");
        ws = withFlag(ws, "finn.disclosure", "NONE");
        ws = withFlag(ws, "suki.button.held", "true");
        ws = withFlag(ws, "suki.button.delivered", "false");
        ws = withFlag(ws, "faction.dock.confrontation", "false");
        ws = withFlag(ws, "equilibrium.restored", "false");

        for (int i = 1; i <= 14; i++) {
            ws = withFlag(ws, "beat" + i + ".fired", "false");
        }
        return ws;
    }

    public static WorldSnapshot initialWorldSnapshot() {
        Map<EntityId, EntityState> states = new LinkedHashMap<>();
        states.put(AshfordCharacters.VESPERA_HOLT.id(), state(AshfordCharacters.VESPERA_HOLT.id(), AshfordConstants.Location.MERCHANT_HALL));
        states.put(AshfordCharacters.DRAVEN_COLE.id(), state(AshfordCharacters.DRAVEN_COLE.id(), AshfordConstants.Location.WAREHOUSE_DISTRICT));
        states.put(AshfordCharacters.MIRA_ASHBY.id(), state(AshfordCharacters.MIRA_ASHBY.id(), AshfordConstants.Location.MERCHANT_HALL));
        states.put(AshfordCharacters.PELL_WYNNE.id(), state(AshfordCharacters.PELL_WYNNE.id(), AshfordConstants.Location.MERCHANT_HALL));
        states.put(AshfordCharacters.TOBIAS_FARR.id(), state(AshfordCharacters.TOBIAS_FARR.id(), AshfordConstants.Location.MERCHANT_HALL));
        states.put(AshfordCharacters.CATRIN_LUCE.id(), state(AshfordCharacters.CATRIN_LUCE.id(), AshfordConstants.Location.MARKET_SQUARE));
        states.put(AshfordCharacters.OSWIN_SALT.id(), state(AshfordCharacters.OSWIN_SALT.id(), AshfordConstants.Location.MARKET_SQUARE));
        states.put(AshfordCharacters.BRINA_MAST.id(), state(AshfordCharacters.BRINA_MAST.id(), AshfordConstants.Location.CROSSED_KEYS));
        states.put(AshfordCharacters.HARLAND_BECK.id(), state(AshfordCharacters.HARLAND_BECK.id(), AshfordConstants.Location.MARKET_SQUARE));
        states.put(AshfordCharacters.SELA_TORCH.id(), state(AshfordCharacters.SELA_TORCH.id(), AshfordConstants.Location.MARKET_SQUARE));
        states.put(AshfordCharacters.PIP_FARR.id(), state(AshfordCharacters.PIP_FARR.id(), AshfordConstants.Location.MERCHANT_HALL));
        states.put(AshfordCharacters.NESSA_COIN.id(), state(AshfordCharacters.NESSA_COIN.id(), AshfordConstants.Location.CROSSED_KEYS));
        states.put(AshfordCharacters.CURWIN_ROPE.id(), state(AshfordCharacters.CURWIN_ROPE.id(), AshfordConstants.Location.RIVER_DOCKS));
        states.put(AshfordCharacters.ALDOUS_INK.id(), state(AshfordCharacters.ALDOUS_INK.id(), AshfordConstants.Location.MERCHANT_HALL));
        states.put(AshfordCharacters.MOTT_WHEEL.id(), state(AshfordCharacters.MOTT_WHEEL.id(), AshfordConstants.Location.WORKERS_QUARTER));
        states.put(AshfordCharacters.HILDE_BASKET.id(), state(AshfordCharacters.HILDE_BASKET.id(), AshfordConstants.Location.MARKET_SQUARE));
        states.put(AshfordCharacters.BROTHER_CAEL.id(), state(AshfordCharacters.BROTHER_CAEL.id(), AshfordConstants.Location.TEMPLE));
        states.put(AshfordCharacters.PHYSIC_OONA.id(), state(AshfordCharacters.PHYSIC_OONA.id(), AshfordConstants.Location.PHYSIC_HOUSE));
        states.put(AshfordCharacters.ASTRID_LOOM.id(), state(AshfordCharacters.ASTRID_LOOM.id(), AshfordConstants.Location.WORKERS_QUARTER));
        states.put(AshfordCharacters.FINN_BARGE.id(), state(AshfordCharacters.FINN_BARGE.id(), AshfordConstants.Location.RIVER_DOCKS));
        states.put(AshfordCharacters.EDRIC_ASHFORD.id(), state(AshfordCharacters.EDRIC_ASHFORD.id(), AshfordConstants.Location.MANOR_HOUSE));
        states.put(AshfordCharacters.MAREN_ASHFORD.id(), state(AshfordCharacters.MAREN_ASHFORD.id(), AshfordConstants.Location.MANOR_HOUSE));
        states.put(AshfordCharacters.WULF_DANE.id(), state(AshfordCharacters.WULF_DANE.id(), AshfordConstants.Location.WATCH_HOUSE));
        states.put(AshfordCharacters.HARRO_VANE.id(), state(AshfordCharacters.HARRO_VANE.id(), AshfordConstants.Location.MANOR_HOUSE));
        states.put(AshfordCharacters.ROWAN_ASHFORD.id(), state(AshfordCharacters.ROWAN_ASHFORD.id(), AshfordConstants.Location.MANOR_HOUSE));
        states.put(AshfordCharacters.ISOLT_CREED.id(), state(AshfordCharacters.ISOLT_CREED.id(), AshfordConstants.Location.MANOR_HOUSE));
        states.put(AshfordCharacters.BASTIAN_THORN.id(), state(AshfordCharacters.BASTIAN_THORN.id(), AshfordConstants.Location.WORKERS_QUARTER));
        states.put(AshfordCharacters.SILAS_PEN.id(), state(AshfordCharacters.SILAS_PEN.id(), AshfordConstants.Location.MANOR_HOUSE));
        states.put(AshfordCharacters.ALYS_BRAND.id(), state(AshfordCharacters.ALYS_BRAND.id(), AshfordConstants.Location.WATCH_HOUSE));
        states.put(AshfordCharacters.TOMAS_FENN.id(), state(AshfordCharacters.TOMAS_FENN.id(), AshfordConstants.Location.WATCH_HOUSE));
        states.put(AshfordCharacters.EDDA_HELM.id(), state(AshfordCharacters.EDDA_HELM.id(), AshfordConstants.Location.WATCH_HOUSE));
        states.put(AshfordCharacters.PIERS_KNOT.id(), state(AshfordCharacters.PIERS_KNOT.id(), AshfordConstants.Location.WATCH_HOUSE));
        states.put(AshfordCharacters.BRAM_SPEAR.id(), state(AshfordCharacters.BRAM_SPEAR.id(), AshfordConstants.Location.WATCH_HOUSE));
        states.put(AshfordCharacters.SUKI_CROSS.id(), state(AshfordCharacters.SUKI_CROSS.id(), AshfordConstants.Location.RIVER_DOCKS));
        states.put(AshfordCharacters.COOK_MARTA.id(), state(AshfordCharacters.COOK_MARTA.id(), AshfordConstants.Location.MANOR_HOUSE));
        states.put(AshfordCharacters.GROOM_ALDRIC.id(), state(AshfordCharacters.GROOM_ALDRIC.id(), AshfordConstants.Location.MANOR_HOUSE));
        states.put(AshfordCharacters.OSKAR_GRAIN.id(), state(AshfordCharacters.OSKAR_GRAIN.id(), AshfordConstants.Location.MILL));
        states.put(AshfordCharacters.FFION_DIRT.id(), state(AshfordCharacters.FFION_DIRT.id(), AshfordConstants.Location.ROAD_OUT));
        states.put(AshfordCharacters.BRET_HIDE.id(), state(AshfordCharacters.BRET_HIDE.id(), AshfordConstants.Location.WORKERS_QUARTER));
        states.put(AshfordCharacters.HEDGEWITCH_VORN.id(), state(AshfordCharacters.HEDGEWITCH_VORN.id(), AshfordConstants.Location.ROAD_OUT));
        states.put(AshfordCharacters.CAEL_MOURNE.id(), state(AshfordCharacters.CAEL_MOURNE.id(), AshfordConstants.Location.ROAD_OUT));

        return new WorldSnapshot(
            0L,
            HashMap.ofAll(states),
            new GlobalFacts(Map.of()),
            new EnvironmentState("clear", 0f, 0f)
        );
    }

    public static WorldState withFlag(WorldState ws, String key, String value) {
        Map<String, Object> next = new LinkedHashMap<>(ws.blackboard());
        next.put(key, value);
        return new WorldState(
            ws.owner(),
            ws.tick(),
            ws.affect(),
            ws.currentThreat(),
            ws.perception(),
            ws.memoryStats(),
            Map.copyOf(next),
            ws.agentPosition(),
            ws.goalPosition(),
            ws.distanceToGoal()
        );
    }

    private static EntityState state(EntityId id, AshfordConstants.Location locationId) {
        return new EntityState(
            id,
            LOCATION_COORDS.get(locationId),
            Map.of("location", locationId.name())
        );
    }
}
