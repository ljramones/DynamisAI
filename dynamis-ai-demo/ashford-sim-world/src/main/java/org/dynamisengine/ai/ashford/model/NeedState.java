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

public record NeedState(
    float safety,
    float sustenance,
    float rest,
    float social,
    float purpose
) {

    public NeedState {
        validate(safety, "safety");
        validate(sustenance, "sustenance");
        validate(rest, "rest");
        validate(social, "social");
        validate(purpose, "purpose");
    }

    public AshfordConstants.NeedType dominantNeed() {
        if (safety > 0.5f) {
            return AshfordConstants.NeedType.SAFETY;
        }
        if (sustenance > 0.5f) {
            return AshfordConstants.NeedType.SUSTENANCE;
        }
        if (rest > 0.5f) {
            return AshfordConstants.NeedType.REST;
        }
        if (social > 0.5f) {
            return AshfordConstants.NeedType.SOCIAL;
        }
        if (purpose > 0.5f) {
            return AshfordConstants.NeedType.PURPOSE;
        }
        return AshfordConstants.NeedType.PURPOSE;
    }

    public boolean safetyOverride() {
        return safety >= 0.9f;
    }

    public NeedState withSafety(float value) {
        return new NeedState(value, sustenance, rest, social, purpose);
    }

    public NeedState withSustenance(float value) {
        return new NeedState(safety, value, rest, social, purpose);
    }

    public NeedState withRest(float value) {
        return new NeedState(safety, sustenance, value, social, purpose);
    }

    public NeedState withSocial(float value) {
        return new NeedState(safety, sustenance, rest, value, purpose);
    }

    public NeedState withPurpose(float value) {
        return new NeedState(safety, sustenance, rest, social, value);
    }

    private static void validate(float value, String field) {
        if (value < 0.0f || value > 1.0f) {
            throw new IllegalArgumentException(field + " must be in [0.0, 1.0]");
        }
    }
}
