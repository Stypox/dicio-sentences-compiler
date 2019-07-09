package com.stypox.sentences_compiler.parser.construct;

import com.stypox.sentences_compiler.util.UnfoldingUtils;

import java.util.ArrayList;

public final class SentenceConstructList implements BaseSentenceConstruct {
    private ArrayList<BaseSentenceConstruct> constructs;

    public SentenceConstructList() {
        constructs = new ArrayList<>();
    }
    public void addConstruct(BaseSentenceConstruct construct) {
        constructs.add(construct);
    }


    @Override
    public ArrayList<ArrayList<String>> unfold() {
        ArrayList<ArrayList<String>> combinations = new ArrayList<>();
        combinations.add(new ArrayList<String>());

        for (BaseSentenceConstruct construct : constructs) {
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
}
