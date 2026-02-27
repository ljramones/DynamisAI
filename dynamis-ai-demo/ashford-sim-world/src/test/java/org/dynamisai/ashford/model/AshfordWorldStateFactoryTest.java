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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.IntStream;
import org.dynamisai.core.WorldSnapshot;
import org.dynamisai.planning.WorldState;
import org.junit.jupiter.api.Test;

class AshfordWorldStateFactoryTest {

    @Test
    void initialWorldStateHasExpectedFlags() {
        WorldState ws = AshfordWorldStateFactory.initialWorldState();

        assertEquals("false", ws.get("dole.alive"));
        assertEquals("false", ws.get("cael.in_town"));
        assertEquals("false", ws.get("harro.confessed"));
        assertEquals("NONE", ws.get("oona.disclosure"));
        assertEquals("true", ws.get("suki.button.held"));
        assertEquals("false", ws.get("suki.button.delivered"));
        assertEquals("HIGH", ws.get("rowan.affect.guilt"));

        IntStream.rangeClosed(1, 14).forEach(i ->
            assertEquals("false", ws.get("beat" + i + ".fired")));
    }

    @Test
    void withFlagReturnsNewWorldState() {
        WorldState ws = AshfordWorldStateFactory.initialWorldState();
        WorldState updated = AshfordWorldStateFactory.withFlag(ws, "cael.in_town", "true");

        assertEquals("true", updated.get("cael.in_town"));
        assertEquals("false", ws.get("cael.in_town"));
    }

    @Test
    void initialSnapshotHasAllEntitiesAndLocations() {
        WorldSnapshot snap = AshfordWorldStateFactory.initialWorldSnapshot();

        assertEquals(41, snap.entities().size());
        assertEquals(AshfordConstants.Location.ROAD_OUT.name(), locationName(snap, AshfordCharacters.CAEL_MOURNE.id()));
        assertEquals(AshfordConstants.Location.WATCH_HOUSE.name(), locationName(snap, AshfordCharacters.WULF_DANE.id()));
        assertEquals(AshfordConstants.Location.MANOR_HOUSE.name(), locationName(snap, AshfordCharacters.HARRO_VANE.id()));
        assertTrue(snap.entities().values().forAll(e -> e.position() != null));
        assertFalse(snap.entities().isEmpty());
    }

    private static String locationName(WorldSnapshot snapshot, org.dynamisai.core.EntityId id) {
        var state = snapshot.entities().get(id).getOrNull();
        assertNotNull(state);
        Object location = state.properties().get("location");
        assertNotNull(location);
        return location.toString();
    }
}
