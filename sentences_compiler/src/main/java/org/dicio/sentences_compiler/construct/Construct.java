package org.dicio.sentences_compiler.construct;

import org.dicio.sentences_compiler.compiler.Alternative;
import org.dicio.sentences_compiler.compiler.RepeatedList;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Construct {
    /**
     * Build the list containing all words, assigns an incremental index to each word
     * (corresponding to its position in the list).
     * @param words the list of words to append new words to
     */
    void buildWordList(List<WordBase> words);

    /**
     * If this the last word of the current construct list, saves {@param nextIndices} in the word,
     * otherwise saves in the word the result of recursively calling itself on the following word.
     * Calculates all of the possible next words for a preceding word or construct list and returns
     * them.
     * @param nextIndices the nextIndices of the last word in the construct list.
     *                    e.g. for "A B C" this parameter would be `[3]` for all words, as the
     *                         index of the word following the last one in the construct list is
     *                         the end of input, i.e. `3`, i.e. the size of the word array.
     *                    e.g. for "A|(B C) D?" this parameter would be `[4, 5]` for B and C, since
     *                         after the end of the construct list "(B C)" the control could reach
     *                         both D and the end of input.
     * @return all of the possible next words for a preceding word
     *         e.g. for "A B?" the return list would be `[1, 2]` for B (i.e. itself and the end of
     *              input, `[0]` for A (i.e. only itself)
     */
    Set<Integer> findNextIndices(Set<Integer> nextIndices);

    /**
     * @return a set with unique strings containing all capturing group names used inside this
     *         construct. They will be added to compiled files as constant variables for easy use
     *         and access and to prevent typos, similarly to what is done inside the {@code R}
     *         class in Android. If this construct contains no capturing group, return
     *         {@link Collections#emptySet()}.
     */
    Set<String> getCapturingGroupNames();

    /**
     * @return all of the possible sentences that can originate from this construct; capturing
     * groups will appear as `.capturing_group_name.`.
     */
    List<String> buildAlternatives();
}
