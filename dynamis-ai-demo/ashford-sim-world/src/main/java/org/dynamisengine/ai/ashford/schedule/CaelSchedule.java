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
package org.dynamisengine.ai.ashford.schedule;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.dynamisengine.ai.ashford.model.AshfordCharacters;
import org.dynamisengine.ai.ashford.model.AshfordConstants;

public final class CaelSchedule {

    private CaelSchedule() {
    }

    public static List<ScheduleEntry> build() {
        List<ScheduleEntry> entries = new ArrayList<>();

        // Day 1
        add(entries, 1, 240, 300, AshfordConstants.Location.CROSSED_KEYS, "Arrives — takes a room", HashMap.empty());
        add(entries, 1, 300, 599, AshfordConstants.Location.MARKET_SQUARE, "Observes social topology and routines", HashMap.empty());
        add(entries, 1, 600, 839, AshfordConstants.Location.MANOR_HOUSE, "Commissioned by Edric", HashMap.of("beat.trigger", "1"));
        add(entries, 1, 840, 959, AshfordConstants.Location.CROSSED_KEYS, "Visible in common room", HashMap.empty());
        add(entries, 1, 960, 1079, AshfordConstants.Location.CROSSED_KEYS, "Accepts second retainer", HashMap.of("beat.trigger", "2"));
        add(entries, 1, 1080, 1439, AshfordConstants.Location.CROSSED_KEYS, "Review and light sleep", HashMap.empty());

        // Day 2
        add(entries, 2, 240, 399, AshfordConstants.Location.WORKERS_QUARTER, "Surveys workers quarter routines", HashMap.empty());
        add(entries, 2, 400, 599, AshfordConstants.Location.CROSSED_KEYS, "Breakfast and questions about town rhythm", HashMap.empty());
        add(entries, 2, 600, 719, AshfordConstants.Location.MILL, "Approaches Oskar", HashMap.empty());
        add(entries, 2, 720, 839, AshfordConstants.Location.RIVER_DOCKS, "Watches dock confrontation", HashMap.of("beat.trigger", "4"));
        add(entries, 2, 840, 959, AshfordConstants.Location.RIVER_DOCKS, "Maps upstream origin point", HashMap.empty());
        add(entries, 2, 960, 1199, AshfordConstants.Location.MARKET_SQUARE, "Maintains public cover", HashMap.empty());
        add(entries, 2, 1200, 1439, AshfordConstants.Location.RIVER_DOCKS, "Night river walk", HashMap.empty());

        // Day 3
        add(entries, 3, 240, 399, AshfordConstants.Location.CROSSED_KEYS, "Late rise and meal", HashMap.empty());
        add(entries, 3, 400, 599, AshfordConstants.Location.MARKET_SQUARE, "Elimination process for manor-side figures", HashMap.empty());
        add(entries, 3, 600, 660, AshfordConstants.Location.CROSSED_KEYS, "Pip testimony window", HashMap.of("beat.window", "5"));
        add(entries, 3, 660, 719, AshfordConstants.Location.CROSSED_KEYS, "Arranges Pip cover", HashMap.empty());
        add(entries, 3, 720, 839, AshfordConstants.Location.WATCH_HOUSE, "Watch house perimeter observation", HashMap.empty());
        add(entries, 3, 840, 959, AshfordConstants.Location.MARKET_SQUARE, "Handles harassment operation", HashMap.of("beat.trigger", "6"));
        add(entries, 3, 960, 1079, AshfordConstants.Location.TEMPLE, "Quiet planning window", HashMap.empty());
        add(entries, 3, 1080, 1439, AshfordConstants.Location.CROSSED_KEYS, "Asymmetric injection and prep", HashMap.empty());

        // Day 4
        add(entries, 4, 240, 399, AshfordConstants.Location.MANOR_HOUSE, "Gate observation and suspect identification", HashMap.empty());
        add(entries, 4, 400, 599, AshfordConstants.Location.MARKET_SQUARE, "Appears idle while tracking movement", HashMap.empty());
        add(entries, 4, 600, 959, AshfordConstants.Location.CROSSED_KEYS, "Maintains neutral visibility", HashMap.empty());
        add(entries, 4, 960, 1079, AshfordConstants.Location.TEMPLE, "Awaits summons", HashMap.empty());
        add(entries, 4, 1080, 1199, AshfordConstants.Location.TEMPLE, "Meeting with Brother Cael",
            HashMap.of("beat.precursor", "8"));
        add(entries, 4, 1200, 1439, AshfordConstants.Location.CROSSED_KEYS, "Processes assembled truth", HashMap.empty());

        // Day 5
        add(entries, 5, 240, 359, AshfordConstants.Location.CROSSED_KEYS, "Breakfast", HashMap.empty());
        add(entries, 5, 360, 539, AshfordConstants.Location.MARKET_SQUARE, "Injects pressure and true intel via Piers", HashMap.empty());
        add(entries, 5, 540, 839, AshfordConstants.Location.CROSSED_KEYS, "Waits for reactions", HashMap.empty());
        add(entries, 5, 840, 920, AshfordConstants.Location.MANOR_HOUSE, "Aldric conversation — final confirmation",
            HashMap.of("cael.belief.complete", "true"));
        add(entries, 5, 920, 959, AshfordConstants.Location.CROSSED_KEYS, "Leaves manor and regroups", HashMap.empty());
        add(entries, 5, 960, 1439, AshfordConstants.Location.MARKET_SQUARE, "Sits with complete truth", HashMap.empty());

        // Day 6
        add(entries, 6, 240, 359, AshfordConstants.Location.WATCH_HOUSE, "Discloses full truth to Wulf",
            HashMap.of("beat.trigger", "12"));
        add(entries, 6, 360, 479, AshfordConstants.Location.MANOR_HOUSE, "Collects commission fee", HashMap.empty());
        add(entries, 6, 480, 539, AshfordConstants.Location.MANOR_HOUSE, "Passes Lady Maren without comment", HashMap.empty());
        add(entries, 6, 540, 1439, AshfordConstants.Location.ROAD_OUT, "Departs", HashMap.empty());

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
            AshfordCharacters.CAEL_MOURNE.id(),
            dayStart + tickStartWithinDay,
            dayStart + tickEndWithinDay,
            location,
            activity,
            metadata
        ));
    }
}
