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
package org.dynamisengine.ai.ashford.model;

import io.vavr.collection.List;

public final class AshfordLocations {

    public static final AshfordLocation MARKET_SQUARE = location(
        AshfordConstants.Location.MARKET_SQUARE, "Market Square",
        AshfordConstants.Faction.NEUTRAL, true, 0.15f, 0.90f, 0.0f, 0.1f
    );
    public static final AshfordLocation CROSSED_KEYS = location(
        AshfordConstants.Location.CROSSED_KEYS, "The Crossed Keys Tavern",
        AshfordConstants.Faction.SCALES, false, 0.10f, 0.60f, 0.3f, 0.2f
    );
    public static final AshfordLocation MERCHANT_HALL = location(
        AshfordConstants.Location.MERCHANT_HALL, "The Merchant Hall",
        AshfordConstants.Faction.SCALES, false, 0.05f, 0.30f, 0.6f, 0.1f
    );
    public static final AshfordLocation MANOR_HOUSE = location(
        AshfordConstants.Location.MANOR_HOUSE, "The Manor House",
        AshfordConstants.Faction.CREST, false, 0.30f, 0.20f, 0.8f, 0.2f
    );
    public static final AshfordLocation WATCH_HOUSE = location(
        AshfordConstants.Location.WATCH_HOUSE, "The Watch House & Garrison",
        AshfordConstants.Faction.CREST, false, 0.20f, 0.40f, 0.5f, 0.1f
    );
    public static final AshfordLocation RIVER_DOCKS = location(
        AshfordConstants.Location.RIVER_DOCKS, "The River Docks & Crossing",
        AshfordConstants.Faction.NEUTRAL, false, 0.70f, 0.50f, 0.0f, 0.6f
    );
    public static final AshfordLocation TEMPLE = location(
        AshfordConstants.Location.TEMPLE, "The Temple",
        AshfordConstants.Faction.NEUTRAL, true, 0.00f, 0.30f, 0.0f, 0.0f
    );
    public static final AshfordLocation PHYSIC_HOUSE = location(
        AshfordConstants.Location.PHYSIC_HOUSE, "The Physic's House",
        AshfordConstants.Faction.NEUTRAL, false, 0.10f, 0.20f, 0.0f, 0.1f
    );
    public static final AshfordLocation MILL = location(
        AshfordConstants.Location.MILL, "The Mill",
        AshfordConstants.Faction.CREST, false, 0.40f, 0.30f, 0.2f, 0.3f
    );
    public static final AshfordLocation WAREHOUSE_DISTRICT = location(
        AshfordConstants.Location.WAREHOUSE_DISTRICT, "The Warehouse District",
        AshfordConstants.Faction.SCALES, false, 0.20f, 0.40f, 0.4f, 0.5f
    );
    public static final AshfordLocation WORKERS_QUARTER = location(
        AshfordConstants.Location.WORKERS_QUARTER, "The Workers' Quarter",
        AshfordConstants.Faction.NEUTRAL, false, 0.10f, 0.70f, 0.0f, 0.2f
    );
    public static final AshfordLocation ROAD_OUT = location(
        AshfordConstants.Location.ROAD_OUT, "The Road Out of Town",
        AshfordConstants.Faction.NEUTRAL, false, 0.25f, 0.80f, 0.0f, 0.1f
    );

    public static final List<AshfordLocation> ALL = List.of(
        MARKET_SQUARE,
        CROSSED_KEYS,
        MERCHANT_HALL,
        MANOR_HOUSE,
        WATCH_HOUSE,
        RIVER_DOCKS,
        TEMPLE,
        PHYSIC_HOUSE,
        MILL,
        WAREHOUSE_DISTRICT,
        WORKERS_QUARTER,
        ROAD_OUT
    );

    private AshfordLocations() {
    }

    public static List<AshfordLocation> byController(AshfordConstants.Faction faction) {
        return ALL.filter(l -> l.controller() == faction);
    }

    public static List<AshfordLocation> neutralGrounds() {
        return ALL.filter(AshfordLocation::isNeutralGround);
    }

    private static AshfordLocation location(AshfordConstants.Location id,
                                            String name,
                                            AshfordConstants.Faction controller,
                                            boolean isNeutralGround,
                                            float initialThreat,
                                            float initialMovement,
                                            float initialTerritorial,
                                            float initialCover) {
        return new AshfordLocation(
            id,
            name,
            controller,
            isNeutralGround,
            initialThreat,
            initialMovement,
            initialTerritorial,
            initialCover
        );
    }
}
