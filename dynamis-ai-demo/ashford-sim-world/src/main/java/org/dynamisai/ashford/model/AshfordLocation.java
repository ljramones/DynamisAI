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

public record AshfordLocation(
    AshfordConstants.Location id,
    String name,
    AshfordConstants.Faction controller,
    boolean isNeutralGround,
    float initialThreat,
    float initialMovement,
    float initialTerritorial,
    float initialCover
) {
    public AshfordLocation {
        if (initialThreat < 0f || initialThreat > 1f) {
            throw new IllegalArgumentException("initialThreat must be in [0,1]");
        }
        if (initialMovement < 0f || initialMovement > 1f) {
            throw new IllegalArgumentException("initialMovement must be in [0,1]");
        }
        if (initialCover < 0f || initialCover > 1f) {
            throw new IllegalArgumentException("initialCover must be in [0,1]");
        }
        if (initialTerritorial < -1f || initialTerritorial > 1f) {
            throw new IllegalArgumentException("initialTerritorial must be in [-1,1]");
        }
    }
}
