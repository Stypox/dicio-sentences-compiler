package org.dicio.sentences_compiler.construct;

import org.dicio.sentences_compiler.compiler.Alternative;
import org.dicio.sentences_compiler.compiler.RepeatedList;
import org.dicio.sentences_compiler.util.StringNormalizer;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class WordWithVariations extends WordBase {

    private final List<List<String>> parts;
    private final boolean diacriticsSensitive;

    /**
     * @param parts contains a list of sub-lists of strings that represent a word with possible
     *              variations. E.g. if a word with variations has to match "doing", "done",
     *              "undoing", "undone", this list should be
     *              {@code [["", "un"], ["do"], ["ing", "ne"]]}
     * @param diacriticsSensitive if true match the word exactly, otherwise ignore differences in
     *                            diacritics/accents (see e.g. CTRL+F -> Match Diacritics in
     *                            Firefox)
     */
    public WordWithVariations(final List<List<String>> parts, final boolean diacriticsSensitive) {
        this.parts = parts;
        this.diacriticsSensitive = diacriticsSensitive;
    }

    public List<List<String>> getParts() {
        return parts;
    }

    public boolean isDiacriticsSensitive() {
        return diacriticsSensitive;
    }


    public String toJavaRegex() {
        final StringBuilder result = new StringBuilder();
        for (final List<String> part : parts) {
            if (part.size() == 1) {
                result.append(part.get(0));

            } else {
                result.append("(?:");
                for (int i = 0; i < part.size(); ++i) {
                    if (i != 0) {
                        result.append("|");
                    }

                    if (diacriticsSensitive) {
                        result.append(part.get(i));
                    } else {
                        result.append(StringNormalizer.nfkdNormalize(part.get(i)));
                    }
                }
                result.append(")");
            }
        }
        return result.toString();
    }

    @Override
    public void compileToJava(final OutputStreamWriter output,
                              final String variableName) throws IOException {
        if (diacriticsSensitive) {
            output.write("new DiacriticsSensitiveRegexWord(\"");
        } else {
            output.write("new DiacriticsInsensitiveRegexWord(\"");
        }
        output.write(toJavaRegex());

        output.write("\",");
        super.compileToJava(output, variableName);
        output.write(")");
    }

    @Override
    public Set<String> getCapturingGroupNames() {
        return Collections.emptySet();
    }

    @Override
    public List<Alternative> buildAlternatives(
            Map<String, RepeatedList> capturingGroupSubstitutions) {
        return Collections.singletonList(new Alternative(
                // just take the first alternative for each word, we don't want a lot of examples
                // to differ by just a word ending
                parts.stream()
                    .map(part -> part.get(0))
                    .collect(Collectors.joining()),
                Collections.emptyMap()));
    }
}
