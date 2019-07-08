package com.stypox.sentences_compiler.parser.construct;

import java.util.ArrayList;

public class Sentence {
    private String sentenceId;
    private SentenceConstructList sentenceConstructs;

    public void setSentenceId(String sentenceId) {
        this.sentenceId = sentenceId;
    }
    public void setSentenceConstructs(SentenceConstructList sentenceConstructs) {
        this.sentenceConstructs = sentenceConstructs;
    }
}
