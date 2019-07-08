package com.stypox.sentences_compiler.parser.construct;

import java.util.ArrayList;

public final class OrList implements BaseSentenceConstruct {
    private ArrayList<BaseSentenceConstruct> constructs;

    OrList() {
        constructs = new ArrayList<>();
    }
    void addConstruct(BaseSentenceConstruct construct) {
        constructs.add(construct);
    }

    @Override
    public ArrayList<ArrayList<String>> unfold() {
        ArrayList<ArrayList<String>> combinations = new ArrayList<>();
        for (BaseSentenceConstruct construct : constructs) {
            combinations.addAll(construct.unfold());
        }
        return combinations;
    }
}
