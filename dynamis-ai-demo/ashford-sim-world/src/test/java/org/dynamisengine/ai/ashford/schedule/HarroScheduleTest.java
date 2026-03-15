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

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class HarroScheduleTest {

    @Test
    void harroScheduleInvariants() {
        List<ScheduleEntry> harro = HarroSchedule.build();

        List<ScheduleEntry> sorted = harro.stream()
            .sorted(Comparator.comparingInt(ScheduleEntry::tickStart))
            .toList();
        for (int i = 0; i < sorted.size() - 1; i++) {
            assertTrue(sorted.get(i).tickEnd() <= sorted.get(i + 1).tickStart());
        }

        assertTrue(harro.stream().noneMatch(e -> e.day() == 6));
        assertTrue(harro.stream().mapToInt(ScheduleEntry::day).max().orElseThrow() == 7);

        assertTrue(harro.stream().anyMatch(e ->
            e.day() == 5 && "true".equals(e.metadata().getOrElse("harro.overreach", "false"))));

        assertTrue(harro.stream().anyMatch(e ->
            "13".equals(e.metadata().getOrElse("beat.trigger", ""))));

        float score = ScheduleVarianceScore.compute(
            harro.stream().filter(e -> e.day() <= 5).collect(Collectors.toList()));
        assertTrue(score > 0.7f);
    }
}
