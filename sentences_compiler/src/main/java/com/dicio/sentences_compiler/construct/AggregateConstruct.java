package com.dicio.sentences_compiler.construct;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AggregateConstruct implements Construct {
    protected final List<Construct> constructs; // could contain one item
    protected final Set<String> capturingGroupNames;

    public AggregateConstruct() {
        constructs = new ArrayList<>();
        capturingGroupNames = new HashSet<>();
    }

    public final void addConstruct(final Construct construct) {
        constructs.add(construct);
        capturingGroupNames.addAll(construct.getCapturingGroupNames());
    }

    @Override
    public final Set<String> getCapturingGroupNames() {
        return capturingGroupNames;
    }
}
