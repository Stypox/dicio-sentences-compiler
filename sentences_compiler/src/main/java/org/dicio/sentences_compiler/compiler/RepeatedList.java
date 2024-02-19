package org.dicio.sentences_compiler.compiler;

import java.util.List;

public class RepeatedList {
    final List<String> alternatives;
    int i = 0;

    public RepeatedList(final List<String> alternatives) {
        if (alternatives.isEmpty()) {
            throw new IndexOutOfBoundsException("alternatives is empty");
        }
        this.alternatives = alternatives;
    }

    public String get() {
        final String res = alternatives.get(i % alternatives.size());
        i += 1;
        return res;
    }
}
