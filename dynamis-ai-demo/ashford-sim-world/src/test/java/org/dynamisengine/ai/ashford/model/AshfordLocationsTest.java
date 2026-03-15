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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AshfordLocationsTest {

    @Test
    void locationRegistryMatchesSpec() {
        assertEquals(12, AshfordLocations.ALL.size());

        assertEquals(2, AshfordLocations.neutralGrounds().size());
        assertTrue(AshfordLocations.neutralGrounds().map(AshfordLocation::id)
            .containsAll(List.of(AshfordConstants.Location.TEMPLE, AshfordConstants.Location.MARKET_SQUARE)));

        assertTrue(AshfordLocations.RIVER_DOCKS.initialThreat() > AshfordLocations.MARKET_SQUARE.initialThreat());
        assertEquals(0.0f, AshfordLocations.TEMPLE.initialThreat());

        assertTrue(AshfordLocations.ALL.forAll(l -> l.initialThreat() >= 0f && l.initialThreat() <= 1f));
        assertTrue(AshfordLocations.ALL.forAll(l -> l.initialMovement() >= 0f && l.initialMovement() <= 1f));
        assertTrue(AshfordLocations.ALL.forAll(l -> l.initialCover() >= 0f && l.initialCover() <= 1f));
        assertTrue(AshfordLocations.ALL.forAll(
            l -> l.initialTerritorial() >= -1f && l.initialTerritorial() <= 1f));

        assertEquals(3, AshfordLocations.byController(AshfordConstants.Faction.SCALES).size());
        assertEquals(3, AshfordLocations.byController(AshfordConstants.Faction.CREST).size());
        assertEquals(6, AshfordLocations.byController(AshfordConstants.Faction.NEUTRAL).size());

        assertEquals(AshfordConstants.Location.MANOR_HOUSE,
            AshfordLocations.ALL.maxBy(AshfordLocation::initialTerritorial).get().id());

        assertTrue(AshfordLocations.ALL.forAll(l -> l.name() != null && !l.name().isBlank()));
    }
}
