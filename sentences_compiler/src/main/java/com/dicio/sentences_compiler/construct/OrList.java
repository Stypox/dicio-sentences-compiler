package com.dicio.sentences_compiler.construct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class OrList implements Construct {
    private List<Construct> constructs; // could contain one item

    public OrList() {
        constructs = new ArrayList<>();
    }

    public void addConstruct(Construct construct) {
        constructs.add(construct);
    }

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
