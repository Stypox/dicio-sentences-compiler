package com.stypox.sentences_compiler.parser.construct;

import com.stypox.sentences_compiler.util.CompilerError;

import java.util.ArrayList;

public class Sentence {
    private String sentenceId;
    private int line;
    private SentenceConstructList sentenceConstructs;

    public void setSentenceId(String sentenceId, int line) {
        this.sentenceId = sentenceId;
        this.line = line;
    }
    public void setSentenceConstructs(SentenceConstructList sentenceConstructs) {
        this.sentenceConstructs = sentenceConstructs;
    }

    public int numberOfCapturingGroups() {
        int count = 0;
        for (BaseSentenceConstruct sentenceConstruct : sentenceConstructs.getConstructs()) {
            if (sentenceConstruct instanceof CapturingGroup) {
                ++count;
            }
        }
        return count;
    }
    public void validate() throws CompilerError {
        int capturingGroups = numberOfCapturingGroups();
        if (capturingGroups > 2) {
            throw new CompilerError(CompilerError.Type.tooManyCapturingGroups, sentenceId, line, "");
        }

        if (sentenceConstructs.isOptional()) {
            throw new CompilerError(CompilerError.Type.sentenceCanBeEmpty, sentenceId, line, "");
        }
    }

    public String getSentenceId() {
        return sentenceId;
    }
    public int getLine() {
        return line;
    }
    public SentenceConstructList getSentenceConstructs() {
        return sentenceConstructs;
    }
}
