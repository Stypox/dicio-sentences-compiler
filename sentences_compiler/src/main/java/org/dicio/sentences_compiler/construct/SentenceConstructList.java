package org.dicio.sentences_compiler.construct;

import java.util.List;
import java.util.Set;

public final class SentenceConstructList extends AggregateConstruct {
    @Override
    public void buildWordList(final List<WordBase> words) {
        for (final Construct construct : constructs) {
            construct.buildWordList(words);
        }
    }

    @Override
    public Set<Integer> findNextIndices(Set<Integer> nextIndices) {
        for (int i = constructs.size() - 1; i >= 0; --i) {
            nextIndices = constructs.get(i).findNextIndices(nextIndices);
        }
        return nextIndices;
    }
}
