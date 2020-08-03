package org.dicio.sentences_compiler.construct;

import org.dicio.sentences_compiler.compiler.CompilableToJava;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class Word implements Construct, CompilableToJava {
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

    @Override
    public Set<String> getCapturingGroupNames() {
        if (isCapturingGroup()) {
            return Collections.singleton(value);
        } else {
            return Collections.emptySet();
        }
    }


    @Override
    public void compileToJava(final OutputStreamWriter output, final String variableName)
            throws IOException {
        output.write("new Word(\"");
        output.write(value);
        output.write("\",");
        output.write(isCapturingGroup ? "true" : "false");
        output.write(",");
        output.write(String.valueOf(minimumSkippedWordsToEnd));
        for (final int nextIndex : nextIndices) {
            output.write(",");
            output.write(String.valueOf(nextIndex));
        }
        output.write(")");
    }
}
