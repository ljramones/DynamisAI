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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dynamisengine.ai.ashford.model.AshfordConstants;
import org.junit.jupiter.api.Test;

class CaelScheduleTest {

    @Test
    void caelScheduleInvariants() {
        List<ScheduleEntry> cael = CaelSchedule.build();

        assertTrue(cael.stream().mapToInt(ScheduleEntry::day).min().orElseThrow() == 1);
        assertTrue(cael.stream().mapToInt(ScheduleEntry::day).max().orElseThrow() == 6);
        assertTrue(cael.stream().noneMatch(e -> e.day() == 7));

        assertTrue(cael.stream().anyMatch(e ->
            e.day() == 5 && "true".equals(e.metadata().getOrElse("cael.belief.complete", "false"))));

        assertTrue(cael.stream().anyMatch(e ->
            "12".equals(e.metadata().getOrElse("beat.trigger", ""))));

        float caelScore = ScheduleVarianceScore.compute(cael);
        assertTrue(caelScore < 0.4f);

        Map<Integer, AshfordConstants.Location> morningByDay = new HashMap<>();
        cael.stream()
            .filter(e -> e.period() == AshfordConstants.Period.MORNING && e.day() <= 5)
            .sorted(java.util.Comparator.comparingInt(ScheduleEntry::tickStart))
            .forEach(e -> morningByDay.putIfAbsent(e.day(), e.location()));

        for (int day = 1; day <= 5; day++) {
            assertTrue(morningByDay.containsKey(day));
        }
        for (int day = 1; day < 5; day++) {
            AshfordConstants.Location current = morningByDay.get(day);
            AshfordConstants.Location next = morningByDay.get(day + 1);
            assertTrue(current != next);
        }
    }
}
