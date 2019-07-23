package com.dicio.sentences_compiler.construct;

import com.dicio.sentences_compiler.parser.UnfoldableConstruct;
import com.dicio.sentences_compiler.util.UnfoldingUtils;

import java.util.ArrayList;

public final class SentenceConstructList implements UnfoldableConstruct {
    private ArrayList<UnfoldableConstruct> constructs;

    public SentenceConstructList() {
        constructs = new ArrayList<>();
    }
    public void addConstruct(UnfoldableConstruct construct) {
        constructs.add(construct);
    }


    @Override
    public ArrayList<ArrayList<String>> unfold() {
        ArrayList<ArrayList<String>> combinations = new ArrayList<>();
        combinations.add(new ArrayList<String>());

        for (UnfoldableConstruct construct : constructs) {
            ArrayList<ArrayList<String>> currComb = construct.unfold();
            int initialSize = combinations.size();
            combinations = UnfoldingUtils.multiplyArray(combinations, currComb.size());

            for (int i = 0; i < currComb.size(); ++i) {
                for (int j = 0; j < initialSize; ++j) {
                    combinations.get(j + i*initialSize).addAll(currComb.get(i));
                }
            }
        }
        return combinations;
    }

    @Override
    public boolean isOptional() {
        for (UnfoldableConstruct construct : constructs) {
            if (!construct.isOptional()) return false;
        }
        return true;
    }

    public ArrayList<UnfoldableConstruct> getConstructs() {
        return constructs;
    }
}
