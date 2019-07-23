package com.dicio.sentences_compiler.construct;

import com.dicio.sentences_compiler.parser.UnfoldableConstruct;

import java.util.ArrayList;

public class ConstructOptional implements UnfoldableConstruct {
    @Override
    public ArrayList<ArrayList<String>> unfold() {
        return new ArrayList<ArrayList<String>>() {{ add(new ArrayList<String>()); }};
    }

    @Override
    public boolean isOptional() {
        return true;
    }
}
