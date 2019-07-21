package com.dicio.sentences_compiler.parser.construct;

import java.util.ArrayList;

public final class OrList implements BaseSentenceConstruct {
    private ArrayList<BaseSentenceConstruct> constructs; // could contain one item

    public OrList() {
        constructs = new ArrayList<>();
    }
    public void addConstruct(BaseSentenceConstruct construct) {
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

    public BaseSentenceConstruct shrink() {
        if (constructs.size() == 1) {
            return constructs.get(0);
        } else {
            return this;
        }
    }

    @Override
    public boolean isOptional() {
        for (BaseSentenceConstruct construct : constructs) {
            if (construct.isOptional()) {
                return true;
            }
        }
        return false;
    }
}
