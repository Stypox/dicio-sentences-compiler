package org.dicio.sentences_compiler.construct;

import org.dicio.sentences_compiler.compiler.CompilableToJava;
import org.dicio.sentences_compiler.util.CompilerError;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Sentence implements CompilableToJava {
    private String sentenceId;
    private SentenceConstructList sentenceConstructs;
    private List<WordBase> compiledWords;
    private Set<Integer> entryPointWordIndices;

    private String inputStreamName;
    private int line;

    public void setSentenceId(final String sentenceId,
                              final String inputStreamName,
                              final int line) {
        this.sentenceId = sentenceId;
        this.inputStreamName = inputStreamName;
        this.line = line;
    }

    public void setSentenceConstructs(final SentenceConstructList sentenceConstructs) {
        this.sentenceConstructs = sentenceConstructs;
    }

    public String getSentenceId() {
        return sentenceId;
    }

    public String getInputStreamName() {
        return inputStreamName;
    }

    public int getLine() {
        return line;
    }

    public Set<String> getCapturingGroupNames() {
        return sentenceConstructs.getCapturingGroupNames();
    }


    public WordBase compileMinimumSkippedWordsToEnd(final int wordIndex) {
        if (wordIndex >= compiledWords.size()) {
            final WordBase word = new Word("", false);
            word.setMinimumSkippedWordsToEnd(0);
            return word;
        }

        final WordBase word = compiledWords.get(wordIndex);
        for (final int nextIndex : word.getNextIndices()) {
            final WordBase next = compileMinimumSkippedWordsToEnd(nextIndex);
            if (next.getMinimumSkippedWordsToEnd() > word.getMinimumSkippedWordsToEnd()) {
                word.setMinimumSkippedWordsToEnd(next.getMinimumSkippedWordsToEnd());
            }
        }

        if (word instanceof CapturingGroup) {
            word.setMinimumSkippedWordsToEnd(word.getMinimumSkippedWordsToEnd() + 2);
        } else {
            word.setMinimumSkippedWordsToEnd(word.getMinimumSkippedWordsToEnd() + 1);
        }
        return word;
    }

    public void compileWordList() throws CompilerError {
        compiledWords = new ArrayList<>();
        sentenceConstructs.buildWordList(compiledWords);
        entryPointWordIndices = sentenceConstructs.findNextIndices(
                Collections.singleton(compiledWords.size()));

        if (entryPointWordIndices.contains(compiledWords.size())) {
            throw new CompilerError(CompilerError.Type.sentenceCanBeEmpty, sentenceId, inputStreamName, line, "");
        }
        for (final int entryPointWordIndex : entryPointWordIndices) {
            compileMinimumSkippedWordsToEnd(entryPointWordIndex);
        }
    }

    public Set<Integer> getEntryPointWordIndices() {
        return entryPointWordIndices;
    }

    public List<WordBase> getCompiledWords() {
        return compiledWords;
    }

    @Override
    public void compileToJava(final OutputStreamWriter output, final String variableName)
            throws IOException {
        output.write("new Sentence(\"");
        output.write(sentenceId);
        output.write("\",new int[]{");
        for (final int entryPointWordIndex : entryPointWordIndices) {
            output.write(String.valueOf(entryPointWordIndex));
            output.write(",");
        }
        output.write("}");

        for (final WordBase word : compiledWords) {
            output.write(",");
            word.compileToJava(output, variableName);
        }
        output.write(")");
    }
}
