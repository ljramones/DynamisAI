module org.dynamisengine.ai.tools {
    requires org.dynamisengine.core;
    requires dynamis.event;
    requires org.dynamisengine.ai.core;
    requires org.dynamisengine.ai.cognition;
    requires org.dynamisengine.ai.voice;
    requires org.dynamisengine.ai.memory;
    requires org.dynamisengine.ai.perception;
    requires org.dynamisengine.ai.planning;
    requires org.dynamisengine.ai.navigation;
    requires org.dynamisengine.ai.social;
    requires org.dynamisengine.ai.crowd;
    requires org.dynamisengine.ai.lod;
    requires org.dynamisengine.scripting.api;
    requires io.vavr;
    requires java.desktop;
    requires dynamis.audio.api;
    requires dynamis.audio.core;
    requires dynamis.audio.dsp;
    exports org.dynamisengine.ai.tools;
}
