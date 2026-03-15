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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AshfordCharactersTest {

    @Test
    void characterRegistryMatchesSpec() {
        assertEquals(41, AshfordCharacters.ALL.size());
        assertEquals(20, AshfordCharacters.byFaction(AshfordConstants.Faction.SCALES).size());
        assertEquals(20, AshfordCharacters.byFaction(AshfordConstants.Faction.CREST).size());
        assertEquals(1, AshfordCharacters.byFaction(AshfordConstants.Faction.NEUTRAL).size());
        assertEquals(4, AshfordCharacters.witnesses().size());
        assertTrue(AshfordCharacters.stranger().isStranger());
        assertEquals(AshfordConstants.Faction.NEUTRAL, AshfordCharacters.stranger().faction());
        assertEquals(41, AshfordCharacters.ALL.map(AshfordCharacter::id).toSet().size());
        assertTrue(AshfordCharacters.ALL.forAll(c -> c.name() != null && !c.name().isBlank()));
        assertTrue(AshfordCharacters.witnesses().forAll(AshfordCharacter::isWitness));
        assertEquals(1, AshfordCharacters.ALL.count(AshfordCharacter::isStranger));
        assertTrue(AshfordCharacters.ALL.forAll(c -> c.tier() >= 1 && c.tier() <= 3));
        assertEquals(8, AshfordCharacters.ALL.count(c -> c.tier() == 1));
    }
}
