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

public final class AshfordConstants {

    public static final int TICKS_PER_DAY = 1440;
    public static final int TOTAL_DAYS = 7;
    public static final int TOTAL_TICKS = 10_080;
    public static final int COGNITION_INTERVAL = 60;
    public static final int DRAMA_EVAL_INTERVAL = 120;

    public static final int DAWN_START = 0;
    public static final int MORNING_START = 240;
    public static final int AFTERNOON_START = 600;
    public static final int EVENING_START = 960;
    public static final int NIGHT_START = 1200;

    public static final float RUMOR_ACTIONABLE_THRESHOLD = 0.15f;
    public static final float RUMOR_SEED_FIDELITY_BECK = 0.90f;
    public static final float RUMOR_SEED_FIDELITY_WYNNE = 0.85f;

    public enum Faction {
        SCALES, CREST, NEUTRAL
    }

    public enum Period {
        DAWN, MORNING, AFTERNOON, EVENING, NIGHT
    }

    public enum Location {
        MARKET_SQUARE, CROSSED_KEYS, MERCHANT_HALL, MANOR_HOUSE,
        WATCH_HOUSE, RIVER_DOCKS, TEMPLE, PHYSIC_HOUSE,
        MILL, WAREHOUSE_DISTRICT, WORKERS_QUARTER, ROAD_OUT
    }

    public enum NeedType {
        SAFETY, SUSTENANCE, REST, SOCIAL, PURPOSE
    }

    private AshfordConstants() {
    }

    public static Period periodForTick(int tickWithinDay) {
        if (tickWithinDay < 0 || tickWithinDay >= TICKS_PER_DAY) {
            throw new IllegalArgumentException("tickWithinDay must be in [0, 1439]");
        }
        if (tickWithinDay < MORNING_START) {
            return Period.DAWN;
        }
        if (tickWithinDay < AFTERNOON_START) {
            return Period.MORNING;
        }
        if (tickWithinDay < EVENING_START) {
            return Period.AFTERNOON;
        }
        if (tickWithinDay < NIGHT_START) {
            return Period.EVENING;
        }
        return Period.NIGHT;
    }

    public static int dayForTick(int absoluteTick) {
        if (absoluteTick < 0) {
            throw new IllegalArgumentException("absoluteTick must be >= 0");
        }
        return (absoluteTick / TICKS_PER_DAY) + 1;
    }

    public static int tickWithinDay(int absoluteTick) {
        if (absoluteTick < 0) {
            throw new IllegalArgumentException("absoluteTick must be >= 0");
        }
        return absoluteTick % TICKS_PER_DAY;
    }
}
