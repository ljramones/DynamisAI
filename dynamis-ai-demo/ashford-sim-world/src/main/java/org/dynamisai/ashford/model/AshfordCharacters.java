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

import io.vavr.collection.List;
import org.dynamis.core.entity.EntityId;

public final class AshfordCharacters {

    public static final AshfordCharacter VESPERA_HOLT = character(1, "Vespera Holt",
        AshfordConstants.Faction.SCALES, "Guild Master", 2, false, false);
    public static final AshfordCharacter DRAVEN_COLE = character(2, "Draven Cole",
        AshfordConstants.Faction.SCALES, "Guild Fixer", 2, false, false);
    public static final AshfordCharacter MIRA_ASHBY = character(3, "Mira Ashby",
        AshfordConstants.Faction.SCALES, "Guild Treasurer", 3, false, false);
    public static final AshfordCharacter PELL_WYNNE = character(4, "Pell Wynne",
        AshfordConstants.Faction.SCALES, "Guild Legal Voice", 3, false, false);
    public static final AshfordCharacter TOBIAS_FARR = character(5, "Tobias Farr",
        AshfordConstants.Faction.SCALES, "Cloth Merchant", 3, false, false);
    public static final AshfordCharacter CATRIN_LUCE = character(6, "Catrin Luce",
        AshfordConstants.Faction.SCALES, "Spice Merchant", 3, false, false);
    public static final AshfordCharacter OSWIN_SALT = character(7, "Oswin Salt",
        AshfordConstants.Faction.SCALES, "Provisions Merchant", 3, false, false);
    public static final AshfordCharacter BRINA_MAST = character(8, "Brina Mast",
        AshfordConstants.Faction.SCALES, "Innkeeper, Crossed Keys", 2, false, false);
    public static final AshfordCharacter HARLAND_BECK = character(9, "Harland Beck",
        AshfordConstants.Faction.SCALES, "Grain Merchant", 2, false, false);
    public static final AshfordCharacter SELA_TORCH = character(10, "Sela Torch",
        AshfordConstants.Faction.SCALES, "Chandler, Courier Network", 3, false, false);
    public static final AshfordCharacter PIP_FARR = character(11, "Pip Farr",
        AshfordConstants.Faction.SCALES, "Apprentice", 1, true, false);
    public static final AshfordCharacter NESSA_COIN = character(12, "Nessa Coin",
        AshfordConstants.Faction.SCALES, "Barmaid, Crossed Keys", 3, false, false);
    public static final AshfordCharacter CURWIN_ROPE = character(13, "Curwin Rope",
        AshfordConstants.Faction.SCALES, "Dockworker", 3, false, false);
    public static final AshfordCharacter ALDOUS_INK = character(14, "Aldous Ink",
        AshfordConstants.Faction.SCALES, "Guild Scribe", 3, false, false);
    public static final AshfordCharacter MOTT_WHEEL = character(15, "Mott Wheel",
        AshfordConstants.Faction.SCALES, "Cartwright", 3, false, false);
    public static final AshfordCharacter HILDE_BASKET = character(16, "Hilde Basket",
        AshfordConstants.Faction.SCALES, "Market Vendor", 2, false, false);
    public static final AshfordCharacter BROTHER_CAEL = character(17, "Brother Cael",
        AshfordConstants.Faction.SCALES, "Temple Monk", 2, false, false);
    public static final AshfordCharacter PHYSIC_OONA = character(18, "Physic Oona",
        AshfordConstants.Faction.SCALES, "Town Healer", 1, false, false);
    public static final AshfordCharacter ASTRID_LOOM = character(19, "Astrid Loom",
        AshfordConstants.Faction.SCALES, "Weaver / Intelligence Asset", 3, false, false);
    public static final AshfordCharacter FINN_BARGE = character(20, "Finn Barge",
        AshfordConstants.Faction.SCALES, "Ferryman", 1, true, false);

    public static final AshfordCharacter EDRIC_ASHFORD = character(21, "Edric Ashford",
        AshfordConstants.Faction.CREST, "Lord of the Town", 2, false, false);
    public static final AshfordCharacter MAREN_ASHFORD = character(22, "Maren Ashford",
        AshfordConstants.Faction.CREST, "Lord's Wife", 2, false, false);
    public static final AshfordCharacter WULF_DANE = character(23, "Wulf Dane",
        AshfordConstants.Faction.CREST, "Watch Captain", 1, false, false);
    public static final AshfordCharacter HARRO_VANE = character(24, "Harro Vane",
        AshfordConstants.Faction.CREST, "Lord's Household Manager / Killer's Facilitator", 1, false, false);
    public static final AshfordCharacter ROWAN_ASHFORD = character(25, "Rowan Ashford",
        AshfordConstants.Faction.CREST, "Lord's Son", 2, false, false);
    public static final AshfordCharacter ISOLT_CREED = character(26, "Isolt Creed",
        AshfordConstants.Faction.CREST, "Knight, Lady Maren's Agent", 3, false, false);
    public static final AshfordCharacter BASTIAN_THORN = character(27, "Bastian Thorn",
        AshfordConstants.Faction.CREST, "Manages Lord's Tenant Lands", 3, false, false);
    public static final AshfordCharacter SILAS_PEN = character(28, "Silas Pen",
        AshfordConstants.Faction.CREST, "Edric's Household Scribe", 3, false, false);
    public static final AshfordCharacter ALYS_BRAND = character(29, "Alys Brand",
        AshfordConstants.Faction.CREST, "Wulf's Deputy", 2, false, false);
    public static final AshfordCharacter TOMAS_FENN = character(30, "Tomas Fenn",
        AshfordConstants.Faction.CREST, "Watch Soldier", 3, false, false);
    public static final AshfordCharacter EDDA_HELM = character(31, "Edda Helm",
        AshfordConstants.Faction.CREST, "Watch Soldier, Wulf's Loyalist", 3, false, false);
    public static final AshfordCharacter PIERS_KNOT = character(32, "Piers Knot",
        AshfordConstants.Faction.CREST, "Watch Soldier / Corrupt", 2, false, false);
    public static final AshfordCharacter BRAM_SPEAR = character(33, "Bram Spear",
        AshfordConstants.Faction.CREST, "Watch Recruit", 3, false, false);
    public static final AshfordCharacter SUKI_CROSS = character(34, "Suki Cross",
        AshfordConstants.Faction.CREST, "Watch Soldier", 2, false, false);
    public static final AshfordCharacter COOK_MARTA = character(35, "Cook Marta Dell",
        AshfordConstants.Faction.CREST, "Manor Kitchen, Lady Maren's Loyalist", 1, true, false);
    public static final AshfordCharacter GROOM_ALDRIC = character(36, "Groom Aldric Hoof",
        AshfordConstants.Faction.CREST, "Manor Stables", 1, true, false);
    public static final AshfordCharacter OSKAR_GRAIN = character(37, "Oskar Grain",
        AshfordConstants.Faction.CREST, "Mill Operator", 3, false, false);
    public static final AshfordCharacter FFION_DIRT = character(38, "Ffion Dirt",
        AshfordConstants.Faction.CREST, "Farmer", 3, false, false);
    public static final AshfordCharacter BRET_HIDE = character(39, "Bret Hide",
        AshfordConstants.Faction.CREST, "Craftsman / Obligated Informant", 3, false, false);
    public static final AshfordCharacter HEDGEWITCH_VORN = character(40, "Hedgewitch Vorn",
        AshfordConstants.Faction.CREST, "Independent Practitioner", 3, false, false);

    public static final AshfordCharacter CAEL_MOURNE = character(41, "Cael Mourne",
        AshfordConstants.Faction.NEUTRAL, "The Stranger / Former Crown Investigator", 1, false, true);

    public static final List<AshfordCharacter> ALL = List.of(
        VESPERA_HOLT,
        DRAVEN_COLE,
        MIRA_ASHBY,
        PELL_WYNNE,
        TOBIAS_FARR,
        CATRIN_LUCE,
        OSWIN_SALT,
        BRINA_MAST,
        HARLAND_BECK,
        SELA_TORCH,
        PIP_FARR,
        NESSA_COIN,
        CURWIN_ROPE,
        ALDOUS_INK,
        MOTT_WHEEL,
        HILDE_BASKET,
        BROTHER_CAEL,
        PHYSIC_OONA,
        ASTRID_LOOM,
        FINN_BARGE,
        EDRIC_ASHFORD,
        MAREN_ASHFORD,
        WULF_DANE,
        HARRO_VANE,
        ROWAN_ASHFORD,
        ISOLT_CREED,
        BASTIAN_THORN,
        SILAS_PEN,
        ALYS_BRAND,
        TOMAS_FENN,
        EDDA_HELM,
        PIERS_KNOT,
        BRAM_SPEAR,
        SUKI_CROSS,
        COOK_MARTA,
        GROOM_ALDRIC,
        OSKAR_GRAIN,
        FFION_DIRT,
        BRET_HIDE,
        HEDGEWITCH_VORN,
        CAEL_MOURNE
    );

    private AshfordCharacters() {
    }

    public static List<AshfordCharacter> byFaction(AshfordConstants.Faction faction) {
        return ALL.filter(c -> c.faction() == faction);
    }

    public static List<AshfordCharacter> witnesses() {
        return ALL.filter(AshfordCharacter::isWitness);
    }

    public static AshfordCharacter stranger() {
        return CAEL_MOURNE;
    }

    private static AshfordCharacter character(long id,
                                              String name,
                                              AshfordConstants.Faction faction,
                                              String role,
                                              int tier,
                                              boolean isWitness,
                                              boolean isStranger) {
        return new AshfordCharacter(EntityId.of(id), name, faction, role, tier, isWitness, isStranger);
    }
}
