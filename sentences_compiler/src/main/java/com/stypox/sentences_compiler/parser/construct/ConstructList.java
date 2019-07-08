package com.stypox.sentences_compiler.parser.construct;

import com.stypox.sentences_compiler.util.UnfoldingUtils;

import java.util.ArrayList;

public final class ConstructList implements BaseConstruct {
    ArrayList<BaseConstruct> constructs;

    ConstructList() {
        constructs = new ArrayList<>();
    }
    void addConstruct(BaseConstruct construct) {
        constructs.add(construct);
    }


    @Override
    public ArrayList<ArrayList<String>> unfold() {
        ArrayList<ArrayList<String>> combinations = new ArrayList<>();
        combinations.add(new ArrayList<String>());

        for (BaseConstruct construct : constructs) {
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
