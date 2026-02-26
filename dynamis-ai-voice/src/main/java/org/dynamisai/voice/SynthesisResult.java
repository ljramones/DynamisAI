package org.dynamisai.voice;

import java.util.List;

/**
 * Primary synthesis output package for one utterance.
 */
public record SynthesisResult(
    AudioStream audio,
    List<VisemeTimestamp> visemes,
    List<BlendshapeFrame> blendshapeFrames
) {}
