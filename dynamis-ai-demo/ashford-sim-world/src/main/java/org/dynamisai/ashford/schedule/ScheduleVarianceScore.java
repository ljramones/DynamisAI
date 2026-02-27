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
package org.dynamisai.ashford.schedule;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.dynamisai.ashford.model.AshfordConstants;

public final class ScheduleVarianceScore {

    private ScheduleVarianceScore() {
    }

    public static float compute(List<ScheduleEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return 0f;
        }

        Map<Integer, EnumMap<AshfordConstants.Period, AshfordConstants.Location>> dayPeriodLocations =
            new HashMap<>();

        entries.stream()
            .sorted(Comparator.comparingInt(ScheduleEntry::tickStart))
            .forEach(entry -> dayPeriodLocations
                .computeIfAbsent(entry.day(), k -> new EnumMap<>(AshfordConstants.Period.class))
                .putIfAbsent(entry.period(), entry.location()));

        TreeSet<Integer> days = new TreeSet<>(dayPeriodLocations.keySet());
        if (days.size() < 2) {
            return 0f;
        }

        int comparisons = 0;
        int matches = 0;
        Integer previous = null;
        for (Integer day : days) {
            if (previous == null) {
                previous = day;
                continue;
            }
            EnumMap<AshfordConstants.Period, AshfordConstants.Location> prevMap = dayPeriodLocations.get(previous);
            EnumMap<AshfordConstants.Period, AshfordConstants.Location> currMap = dayPeriodLocations.get(day);
            for (AshfordConstants.Period period : AshfordConstants.Period.values()) {
                if (prevMap.containsKey(period) && currMap.containsKey(period)) {
                    comparisons++;
                    if (prevMap.get(period) == currMap.get(period)) {
                        matches++;
                    }
                }
            }
            previous = day;
        }
        return comparisons == 0 ? 0f : (float) matches / comparisons;
    }
}
