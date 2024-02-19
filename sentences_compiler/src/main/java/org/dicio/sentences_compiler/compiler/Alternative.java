package org.dicio.sentences_compiler.compiler;

import java.util.HashMap;
import java.util.Map;

public class Alternative {
    public final String sentence;
    public final Map<String, String> capturingGroupValues;

    public Alternative(final String sentence, final Map<String, String> capturingGroupValues) {
        this.sentence = sentence;
        this.capturingGroupValues = capturingGroupValues;
    }

    public Alternative plus(final Alternative other) {
        final Map<String, String> combinedCapturingGroupValues = new HashMap<>();
        combinedCapturingGroupValues.putAll(capturingGroupValues);
        combinedCapturingGroupValues.putAll(other.capturingGroupValues);
        return new Alternative(sentence + " " + other.sentence,
                combinedCapturingGroupValues);
    }
}
