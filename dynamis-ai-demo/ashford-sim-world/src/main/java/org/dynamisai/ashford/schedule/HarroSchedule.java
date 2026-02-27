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

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.dynamisai.ashford.model.AshfordCharacters;
import org.dynamisai.ashford.model.AshfordConstants;

public final class HarroSchedule {

    private HarroSchedule() {
    }

    public static List<ScheduleEntry> build() {
        List<ScheduleEntry> entries = new ArrayList<>();

        // Day 1
        add(entries, 1, 0, 240, AshfordConstants.Location.MANOR_HOUSE, "Sleep", HashMap.empty());
        add(entries, 1, 240, 480, AshfordConstants.Location.MANOR_HOUSE, "Morning routine and briefings", HashMap.empty());
        add(entries, 1, 480, 599, AshfordConstants.Location.WATCH_HOUSE, "Courtesy visit — plants Bastian Thorn trail", HashMap.empty());
        add(entries, 1, 600, 960, AshfordConstants.Location.MANOR_HOUSE, "Records and household management", HashMap.empty());
        add(entries, 1, 960, 1200, AshfordConstants.Location.MANOR_HOUSE, "Supper and log review", HashMap.empty());
        add(entries, 1, 1200, 1440, AshfordConstants.Location.MANOR_HOUSE, "Sleep", HashMap.empty());

        // Day 2
        add(entries, 2, 0, 240, AshfordConstants.Location.MANOR_HOUSE, "Sleep", HashMap.empty());
        add(entries, 2, 240, 480, AshfordConstants.Location.MANOR_HOUSE, "Morning routine and briefing", HashMap.empty());
        add(entries, 2, 480, 600, AshfordConstants.Location.RIVER_DOCKS, "Checks dock origin point", HashMap.empty());
        add(entries, 2, 600, 840, AshfordConstants.Location.WATCH_HOUSE, "Courtesy visit and redirection", HashMap.empty());
        add(entries, 2, 840, 960, AshfordConstants.Location.MANOR_HOUSE, "Meets Piers Knot", HashMap.empty());
        add(entries, 2, 960, 1200, AshfordConstants.Location.MANOR_HOUSE, "Supper and Rowan visit", HashMap.empty());
        add(entries, 2, 1200, 1440, AshfordConstants.Location.MANOR_HOUSE, "Interrupted sleep", HashMap.empty());

        // Day 3
        add(entries, 3, 0, 240, AshfordConstants.Location.MANOR_HOUSE, "Poor sleep", HashMap.empty());
        add(entries, 3, 240, 720, AshfordConstants.Location.MANOR_HOUSE, "Briefings and grounds walk", HashMap.empty());
        add(entries, 3, 720, 839, AshfordConstants.Location.MANOR_HOUSE, "Instructs Piers harassment operation",
            HashMap.of("beat.trigger", "6"));
        add(entries, 3, 839, 960, AshfordConstants.Location.MANOR_HOUSE, "Normal briefing cover", HashMap.empty());
        add(entries, 3, 960, 1200, AshfordConstants.Location.MANOR_HOUSE, "Contingency review", HashMap.empty());
        add(entries, 3, 1200, 1440, AshfordConstants.Location.MANOR_HOUSE, "Poor sleep", HashMap.empty());

        // Day 4
        add(entries, 4, 0, 360, AshfordConstants.Location.MANOR_HOUSE, "Minimal rest and routine", HashMap.empty());
        add(entries, 4, 360, 600, AshfordConstants.Location.MANOR_HOUSE, "Intercepts records request", HashMap.empty());
        add(entries, 4, 600, 720, AshfordConstants.Location.WATCH_HOUSE, "Third courtesy visit", HashMap.empty());
        add(entries, 4, 720, 960, AshfordConstants.Location.MANOR_HOUSE, "Rowan visit and logistics", HashMap.empty());
        add(entries, 4, 960, 1200, AshfordConstants.Location.MANOR_HOUSE, "Supper and final prep", HashMap.empty());
        add(entries, 4, 1200, 1440, AshfordConstants.Location.MANOR_HOUSE, "Light sleep", HashMap.empty());

        // Day 5
        add(entries, 5, 0, 480, AshfordConstants.Location.MANOR_HOUSE, "Awake early and morning briefing", HashMap.empty());
        add(entries, 5, 480, 539, AshfordConstants.Location.MANOR_HOUSE, "Dispatches servant to intercept Oona",
            HashMap.of("beat.trigger", "9").put("harro.overreach", "true"));
        add(entries, 5, 539, 960, AshfordConstants.Location.MANOR_HOUSE, "Waits and activates contingency", HashMap.empty());
        add(entries, 5, 960, 1200, AshfordConstants.Location.MANOR_HOUSE, "Final supper and packing", HashMap.empty());
        add(entries, 5, 1200, 1440, AshfordConstants.Location.ROAD_OUT, "Departs — contingency activated", HashMap.empty());

        // Day 6 intentionally empty (PATTERN_BREAK)

        // Day 7
        add(entries, 7, 0, 479, AshfordConstants.Location.WATCH_HOUSE, "In custody", HashMap.empty());
        add(entries, 7, 480, 600, AshfordConstants.Location.WATCH_HOUSE, "Confession",
            HashMap.of("beat.trigger", "13"));
        add(entries, 7, 600, 1440, AshfordConstants.Location.WATCH_HOUSE, "Awaiting Crown disposition", HashMap.empty());

        entries.sort(Comparator.comparingInt(ScheduleEntry::tickStart));
        return List.copyOf(entries);
    }

    private static void add(List<ScheduleEntry> entries,
                            int day,
                            int tickStartWithinDay,
                            int tickEndWithinDay,
                            AshfordConstants.Location location,
                            String activity,
                            Map<String, String> metadata) {
        int dayStart = (day - 1) * AshfordConstants.TICKS_PER_DAY;
        entries.add(new ScheduleEntry(
            AshfordCharacters.HARRO_VANE.id(),
            dayStart + tickStartWithinDay,
            dayStart + tickEndWithinDay,
            location,
            activity,
            metadata
        ));
    }
}
