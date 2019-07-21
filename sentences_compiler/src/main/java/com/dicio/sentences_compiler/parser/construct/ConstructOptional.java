package com.dicio.sentences_compiler.parser.construct;

import java.util.ArrayList;

public class ConstructOptional implements BaseSentenceConstruct {
    @Override
    public ArrayList<ArrayList<String>> unfold() {
        return new ArrayList<ArrayList<String>>() {{ add(new ArrayList<String>()); }};
    }

    @Override
    public boolean isOptional() {
        return true;
    }
}
