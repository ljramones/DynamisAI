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

import java.util.Objects;
import org.dynamis.core.entity.EntityId;

public final class NeedStateFactory {

    private NeedStateFactory() {
    }

    public static NeedState forCharacter(EntityId id) {
        Objects.requireNonNull(id, "id");

        if (id.equals(AshfordCharacters.HARRO_VANE.id())) {
            return harroVane();
        }
        if (id.equals(AshfordCharacters.CAEL_MOURNE.id())) {
            return caelMourne();
        }
        if (id.equals(AshfordCharacters.PHYSIC_OONA.id())) {
            return oona();
        }
        if (id.equals(AshfordCharacters.WULF_DANE.id())) {
            return wulfDane();
        }
        if (id.equals(AshfordCharacters.PIP_FARR.id())) {
            return pipFarr();
        }
        if (id.equals(AshfordCharacters.FINN_BARGE.id())) {
            return finnBarge();
        }
        if (id.equals(AshfordCharacters.COOK_MARTA.id())) {
            return cookMarta();
        }
        if (id.equals(AshfordCharacters.GROOM_ALDRIC.id())) {
            return groomAldric();
        }

        AshfordCharacter character = AshfordCharacters.ALL
            .find(c -> c.id().equals(id))
            .getOrElseThrow(() -> new IllegalArgumentException("Unknown character id: " + id));
        return archetypeFor(character);
    }

    public static NeedState harroVane() {
        return new NeedState(0.30f, 0.20f, 0.30f, 0.20f, 0.80f);
    }

    public static NeedState caelMourne() {
        return new NeedState(0.40f, 0.50f, 0.45f, 0.20f, 0.90f);
    }

    public static NeedState oona() {
        return new NeedState(0.75f, 0.30f, 0.40f, 0.30f, 0.60f);
    }

    public static NeedState wulfDane() {
        return new NeedState(0.25f, 0.40f, 0.50f, 0.20f, 0.85f);
    }

    public static NeedState pipFarr() {
        return new NeedState(0.90f, 0.40f, 0.50f, 0.30f, 0.40f);
    }

    public static NeedState finnBarge() {
        return new NeedState(0.95f, 0.30f, 0.40f, 0.10f, 0.40f);
    }

    public static NeedState cookMarta() {
        return new NeedState(0.60f, 0.30f, 0.40f, 0.20f, 0.50f);
    }

    public static NeedState groomAldric() {
        return new NeedState(0.10f, 0.20f, 0.20f, 0.20f, 0.60f);
    }

    public static NeedState merchantDefault() {
        return new NeedState(0.20f, 0.30f, 0.30f, 0.50f, 0.60f);
    }

    public static NeedState watchSoldierDefault() {
        return new NeedState(0.20f, 0.30f, 0.40f, 0.30f, 0.70f);
    }

    public static NeedState servantDefault() {
        return new NeedState(0.25f, 0.30f, 0.35f, 0.30f, 0.50f);
    }

    public static NeedState nobleHouseDefault() {
        return new NeedState(0.30f, 0.20f, 0.30f, 0.40f, 0.70f);
    }

    public static NeedState specialistDefault() {
        return new NeedState(0.20f, 0.30f, 0.35f, 0.35f, 0.65f);
    }

    private static NeedState archetypeFor(AshfordCharacter character) {
        String role = character.role().toLowerCase();

        if (isWatchRole(role)) {
            return watchSoldierDefault();
        }
        if (isServantRole(role)) {
            return servantDefault();
        }
        if (isMerchantRole(role)) {
            return merchantDefault();
        }
        if (isNobleRole(role)) {
            return nobleHouseDefault();
        }
        if (character.tier() == 2) {
            return character.faction() == AshfordConstants.Faction.CREST
                ? nobleHouseDefault()
                : merchantDefault();
        }
        return character.faction() == AshfordConstants.Faction.NEUTRAL
            ? specialistDefault()
            : (character.faction() == AshfordConstants.Faction.CREST
            ? nobleHouseDefault()
            : merchantDefault());
    }

    private static boolean isMerchantRole(String role) {
        return role.contains("merchant")
            || role.contains("guild")
            || role.contains("vendor")
            || role.contains("innkeeper")
            || role.contains("factor")
            || role.contains("treasurer")
            || role.contains("chandler");
    }

    private static boolean isWatchRole(String role) {
        return role.contains("watch")
            || role.contains("soldier")
            || role.contains("captain")
            || role.contains("corporal")
            || role.contains("sergeant")
            || role.contains("guard")
            || role.contains("recruit");
    }

    private static boolean isServantRole(String role) {
        return role.contains("cook")
            || role.contains("groom")
            || role.contains("barmaid")
            || role.contains("dockworker")
            || role.contains("ferryman")
            || role.contains("farmer")
            || role.contains("cartwright")
            || role.contains("apprentice");
    }

    private static boolean isNobleRole(String role) {
        return role.contains("lord")
            || role.contains("lady")
            || role.contains("steward")
            || role.contains("knight")
            || role.contains("son")
            || role.contains("clerk")
            || role.contains("reeve");
    }
}
