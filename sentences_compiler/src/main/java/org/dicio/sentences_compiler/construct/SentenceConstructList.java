package org.dicio.sentences_compiler.construct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class SentenceConstructList extends AggregateConstruct {
    @Override
    public void buildWordList(final List<WordBase> words) {
        for (final Construct construct : constructs) {
            construct.buildWordList(words);
        }
    }

    @Override
    public Set<Integer> findNextIndices(Set<Integer> nextIndices) {
        for (int i = constructs.size() - 1; i >= 0; --i) {
            nextIndices = constructs.get(i).findNextIndices(nextIndices);
        }
        return nextIndices;
    }

    @Override
    public List<String> buildAlternatives() {
        List<String> res = null;
        for (final Construct construct : constructs) {
            final List<String> nextAlt = construct.buildAlternatives();
            if (res == null || res.isEmpty()) {
                res = nextAlt;
            } else {
                final List<String> prevAlt = res;
                res = new ArrayList<>();
                for (final String prev : prevAlt) {
                    for (final String next : nextAlt) {
                        res.add(prev + (prev.isEmpty() || next.isEmpty() ? "" : " ") + next);
                    }
                }
            }
        }

        if (res == null) {
            res = Collections.emptyList();
        }
        return res;
    }
}
