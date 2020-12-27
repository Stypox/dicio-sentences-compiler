package org.dicio.sentences_compiler.construct;

import org.dicio.sentences_compiler.compiler.CompilableToJava;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class WordBase implements Construct, CompilableToJava {
    private int index;
    private Set<Integer> nextIndices;
    private int minimumSkippedWordsToEnd = -1; // see dicio-skill library

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
    public void buildWordList(final List<WordBase> words) {
        index = words.size();
        words.add(this);
    }

    @Override
    public Set<Integer> findNextIndices(final Set<Integer> nextIndices) {
        this.nextIndices = nextIndices;
        return Collections.singleton(index);
    }

    @Override
    public void compileToJava(final OutputStreamWriter output,
                              final String variableName) throws IOException {
        output.write(String.valueOf(minimumSkippedWordsToEnd));
        for (final int nextIndex : nextIndices) {
            output.write(",");
            output.write(String.valueOf(nextIndex));
        }
    }
}
