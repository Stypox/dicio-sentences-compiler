package com.dicio.sentences_compiler.construct;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class SentenceConstructList implements Construct {
    private ArrayList<Construct> constructs;

    public SentenceConstructList() {
        constructs = new ArrayList<>();
    }

    public void addConstruct(final Construct construct) {
        constructs.add(construct);
    }

    public ArrayList<Construct> getConstructs() {
        return constructs;
    }


    @Override
    public void buildWordList(final List<Word> words) {
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
