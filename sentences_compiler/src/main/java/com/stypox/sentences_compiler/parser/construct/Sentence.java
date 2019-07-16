package com.stypox.sentences_compiler.parser.construct;

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

    public String getSentenceId() {
        return sentenceId;
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

    public int getLine() {
        return line;
    }
    public SentenceConstructList getSentenceConstructs() {
        return sentenceConstructs;
    }
}
