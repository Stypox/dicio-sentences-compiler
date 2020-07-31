package com.dicio.sentences_compiler.construct;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class OrList extends AggregateConstruct {

    public Construct shrink() {
        if (constructs.size() == 1) {
            return constructs.get(0);
        } else {
            return this;
        }
    }


    @Override
    public void buildWordList(final List<Word> words) {
        for (final Construct construct : constructs) {
            construct.buildWordList(words);
        }
    }

    @Override
    public Set<Integer> findNextIndices(final Set<Integer> nextIndices) {
        final Set<Integer> merged = new HashSet<>();
        for (final Construct construct : constructs) {
            merged.addAll(construct.findNextIndices(nextIndices));
        }
        return merged;
    }
}
