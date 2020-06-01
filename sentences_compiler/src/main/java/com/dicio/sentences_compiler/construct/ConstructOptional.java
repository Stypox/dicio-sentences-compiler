package com.dicio.sentences_compiler.construct;

import java.util.List;
import java.util.Set;

public class ConstructOptional implements Construct {
    @Override
    public void buildWordList(final List<Word> words) {
        // do nothing, this is not a word
    }

    @Override
    public Set<Integer> findNextIndices(final Set<Integer> nextIndices) {
        return nextIndices;
    }
}
