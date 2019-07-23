package com.dicio.sentences_compiler.construct;

import com.dicio.sentences_compiler.parser.UnfoldableConstruct;

import java.util.ArrayList;

public final class OrList implements UnfoldableConstruct {
    private ArrayList<UnfoldableConstruct> constructs; // could contain one item

    public OrList() {
        constructs = new ArrayList<>();
    }
    public void addConstruct(UnfoldableConstruct construct) {
        constructs.add(construct);
    }

    @Override
    public ArrayList<ArrayList<String>> unfold() {
        ArrayList<ArrayList<String>> combinations = new ArrayList<>();
        for (UnfoldableConstruct construct : constructs) {
            combinations.addAll(construct.unfold());
        }
        return combinations;
    }

    public UnfoldableConstruct shrink() {
        if (constructs.size() == 1) {
            return constructs.get(0);
        } else {
            return this;
        }
    }

    @Override
    public boolean isOptional() {
        for (UnfoldableConstruct construct : constructs) {
            if (construct.isOptional()) {
                return true;
            }
        }
        return false;
    }
}
