package org.dicio.sentences_compiler.construct;

import org.dicio.sentences_compiler.compiler.Alternative;
import org.dicio.sentences_compiler.compiler.RepeatedList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class OrList extends AggregateConstruct {

    public Construct shrink() {
        if (constructs.size() == 1) {
            return constructs.get(0);
        } else {
            return this;
        }
    }


    @Override
    public void buildWordList(final List<WordBase> words) {
        for (final Construct construct : constructs) {
            construct.buildWordList(words);
        }
    }

    @Override
    public Set<Integer> findNextIndices(final Set<Integer> nextIndices) {
        final Set<Integer> merged = new HashSet<>();
        for (final Construct construct : constructs) {
            merged.addAll(construct.findNextIndices(nextIndices));
        }
        return merged;
    }

    @Override
    public List<String> buildAlternatives() {
        final List<String> merged = new ArrayList<>();
        for (final Construct construct : constructs) {
            merged.addAll(construct.buildAlternatives());
        }
        return merged;
    }
}
