package com.stypox.sentences_compiler.parser.construct;

import java.util.ArrayList;

public class Section {
    private String sectionId;
    private ArrayList<Sentence> sentences;

    Section() {
        sentences = new ArrayList<>();
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }
    public void addSentence(Sentence sentences) {
        this.sentences.add(sentences);
    }
}
