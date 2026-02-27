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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NeedStateFactoryTest {

    @Test
    void namedCharacterAssignments() {
        assertTrue(NeedStateFactory.forCharacter(AshfordCharacters.FINN_BARGE.id()).safetyOverride());
        assertEquals(AshfordConstants.NeedType.PURPOSE,
            NeedStateFactory.forCharacter(AshfordCharacters.HARRO_VANE.id()).dominantNeed());
        assertEquals(0.75f, NeedStateFactory.forCharacter(AshfordCharacters.PHYSIC_OONA.id()).safety());
        assertEquals(0.10f, NeedStateFactory.forCharacter(AshfordCharacters.GROOM_ALDRIC.id()).safety());
    }

    @Test
    void forCharacterReturnsNonNullForAllCharacters() {
        assertTrue(AshfordCharacters.ALL.forAll(c -> NeedStateFactory.forCharacter(c.id()) != null));
    }

    @Test
    void finnSafetyOverride() {
        assertTrue(NeedStateFactory.finnBarge().safetyOverride());
    }

    @Test
    void caelDominantNeed() {
        assertEquals(AshfordConstants.NeedType.PURPOSE,
            NeedStateFactory.caelMourne().dominantNeed());
    }

    @Test
    void archetypeDefaultsAreAvailable() {
        assertNotNull(NeedStateFactory.merchantDefault());
        assertNotNull(NeedStateFactory.watchSoldierDefault());
        assertNotNull(NeedStateFactory.servantDefault());
        assertNotNull(NeedStateFactory.nobleHouseDefault());
        assertNotNull(NeedStateFactory.specialistDefault());
    }
}
