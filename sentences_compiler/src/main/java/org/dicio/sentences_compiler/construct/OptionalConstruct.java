package org.dicio.sentences_compiler.construct;

import org.dicio.sentences_compiler.compiler.Alternative;
import org.dicio.sentences_compiler.compiler.RepeatedList;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class OptionalConstruct implements Construct {
    @Override
    public void buildWordList(final List<WordBase> words) {
        // do nothing, this is not a word
    }

    @Override
    public Set<Integer> findNextIndices(final Set<Integer> nextIndices) {
        return nextIndices;
    }

    @Override
    public Set<String> getCapturingGroupNames() {
        // do nothing, this is not a word
        return Collections.emptySet();
    }

    @Override
    public List<Alternative> buildAlternatives(
            final Map<String, RepeatedList> capturingGroupSubstitutions) {
        return Collections.singletonList(new Alternative("", Collections.emptyMap()));
    }
}
