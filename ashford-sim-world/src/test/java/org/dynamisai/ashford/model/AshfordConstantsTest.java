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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AshfordConstantsTest {

    @Test
    void arithmeticConstantsMatch() {
        assertEquals(AshfordConstants.TOTAL_TICKS,
            AshfordConstants.TICKS_PER_DAY * AshfordConstants.TOTAL_DAYS);
    }

    @Test
    void periodForTickBoundaries() {
        assertEquals(AshfordConstants.Period.DAWN, AshfordConstants.periodForTick(0));
        assertEquals(AshfordConstants.Period.DAWN, AshfordConstants.periodForTick(239));
        assertEquals(AshfordConstants.Period.MORNING, AshfordConstants.periodForTick(240));
        assertEquals(AshfordConstants.Period.MORNING, AshfordConstants.periodForTick(599));
        assertEquals(AshfordConstants.Period.AFTERNOON, AshfordConstants.periodForTick(600));
        assertEquals(AshfordConstants.Period.AFTERNOON, AshfordConstants.periodForTick(959));
        assertEquals(AshfordConstants.Period.EVENING, AshfordConstants.periodForTick(960));
        assertEquals(AshfordConstants.Period.EVENING, AshfordConstants.periodForTick(1199));
        assertEquals(AshfordConstants.Period.NIGHT, AshfordConstants.periodForTick(1200));
        assertEquals(AshfordConstants.Period.NIGHT, AshfordConstants.periodForTick(1439));
    }

    @Test
    void dayForTickBoundaries() {
        assertEquals(1, AshfordConstants.dayForTick(0));
        assertEquals(1, AshfordConstants.dayForTick(1439));
        assertEquals(2, AshfordConstants.dayForTick(1440));
        assertEquals(7, AshfordConstants.dayForTick(10079));
    }

    @Test
    void tickWithinDayBoundaries() {
        assertEquals(0, AshfordConstants.tickWithinDay(0));
        assertEquals(1439, AshfordConstants.tickWithinDay(1439));
        assertEquals(0, AshfordConstants.tickWithinDay(1440));
        assertEquals(1, AshfordConstants.tickWithinDay(1441));
    }
}
