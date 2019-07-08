package com.stypox.sentences_compiler.parser.construct;

import java.util.ArrayList;

public final class OrList implements BaseConstruct {
    private ArrayList<BaseConstruct> constructs;

    OrList() {
        constructs = new ArrayList<>();
    }
    void addConstruct(BaseConstruct construct) {
        constructs.add(construct);
    }

    @Override
    public ArrayList<ArrayList<String>> unfold() {
        ArrayList<ArrayList<String>> combinations = new ArrayList<>();
        for (BaseConstruct construct : constructs) {
            combinations.addAll(construct.unfold());
        }
        return combinations;
    }
}
