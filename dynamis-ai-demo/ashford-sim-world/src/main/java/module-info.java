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
module org.dynamisengine.ai.ashfordsimworld {
    requires org.dynamisengine.core;
    requires io.vavr;
    requires org.dynamisengine.ai.core;
    requires org.dynamisengine.ai.social;
    requires org.dynamisengine.ai.planning;
    requires org.dynamisengine.ai.cognition;
    requires org.dynamisengine.ai.perception;
    requires org.dynamisengine.ai.tools;
    requires javafx.controls;
    requires javafx.fxml;

    exports org.dynamisengine.ai.ashford;
    exports org.dynamisengine.ai.ashford.model;
    exports org.dynamisengine.ai.ashford.schedule;
}
