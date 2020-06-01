package com.dicio.sentences_compiler.construct;

import com.dicio.sentences_compiler.compiler.CompilableToJava;
import com.dicio.sentences_compiler.util.CompilerError;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Sentence implements CompilableToJava {
    private String sentenceId;
    private SentenceConstructList sentenceConstructs;
    private List<Word> compiledWords;
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


    public void compileWordList() throws CompilerError {
        compiledWords = new ArrayList<>();
        sentenceConstructs.buildWordList(compiledWords);
        entryPointWordIndices = sentenceConstructs.findNextIndices(
                Collections.singleton(compiledWords.size()));

        if (entryPointWordIndices.contains(compiledWords.size())) {
            throw new CompilerError(CompilerError.Type.sentenceCanBeEmpty, sentenceId, inputStreamName, line, "");
        }
    }

    public Set<Integer> getEntryPointWordIndices() {
        return entryPointWordIndices;
    }

    public List<Word> getCompiledWords() {
        return compiledWords;
    }

    @Override
    public void compileToJava(final OutputStreamWriter output, final String variableName)
            throws IOException {
        /*
        for (ArrayList<String> unfoldedSentence : sentenceConstructs.unfold()) {
            if (!variableName.isEmpty()) {
                output.write("final Sentence ");
                output.write(variableName);
                output.write(" = ");
            }

            output.write("new Sentence(\"");
            output.write(sentenceId);
            output.write("\", new String[]{");

            for (String word : unfoldedSentence) {
                if (word.equals(".")) {
                    output.write("}, new String[]{");
                } else {
                    output.write("\"");
                    output.write(word);
                    output.write("\",");
                }
            }

            output.write("}),\n");
        }
        */
    }
}
