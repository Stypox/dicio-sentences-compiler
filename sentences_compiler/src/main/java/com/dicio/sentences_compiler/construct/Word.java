package com.dicio.sentences_compiler.construct;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class Word implements Construct {
    private final String value;
    private final boolean isCapturingGroup;
    private int index;
    private Set<Integer> nextIndices;
    private int minimumSkippedWordsToEnd = -1; // see dicio-assistance-component library

    public Word(final String value, final boolean isCapturingGroup) {
        this.value = value;
        this.isCapturingGroup = isCapturingGroup;
    }

    public String getValue() {
        return value;
    }

    public boolean isCapturingGroup() {
        return isCapturingGroup;
    }

    public int getIndex() {
        return index;
    }

    public Set<Integer> getNextIndices() {
        return nextIndices;
    }

    public int getMinimumSkippedWordsToEnd() {
        return minimumSkippedWordsToEnd;
    }

    public void setMinimumSkippedWordsToEnd(int minimumSkippedWordsToEnd) {
        this.minimumSkippedWordsToEnd = minimumSkippedWordsToEnd;
    }


    @Override
    public void buildWordList(final List<Word> words) {
        index = words.size();
        words.add(this);
    }

    @Override
    public Set<Integer> findNextIndices(final Set<Integer> nextIndices) {
        this.nextIndices = nextIndices;
        return Collections.singleton(index);
    }
}
