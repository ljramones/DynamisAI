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
package org.dynamisai.ashford.schedule;

import io.vavr.collection.Map;
import java.util.Objects;
import org.dynamisai.ashford.model.AshfordConstants;
import org.dynamis.core.entity.EntityId;

public record ScheduleEntry(
    EntityId characterId,
    int tickStart,
    int tickEnd,
    AshfordConstants.Location location,
    String activity,
    Map<String, String> metadata
) {
    public ScheduleEntry {
        Objects.requireNonNull(characterId);
        Objects.requireNonNull(location);
        Objects.requireNonNull(activity);
        Objects.requireNonNull(metadata);
        if (tickEnd <= tickStart) {
            throw new IllegalArgumentException("tickEnd must be greater than tickStart");
        }
    }

    public AshfordConstants.Period period() {
        return AshfordConstants.periodForTick(tickStart % AshfordConstants.TICKS_PER_DAY);
    }

    public int day() {
        return AshfordConstants.dayForTick(tickStart);
    }
}
