package org.dicio.sentences_compiler.construct;

import org.dicio.sentences_compiler.compiler.Alternative;
import org.dicio.sentences_compiler.compiler.RepeatedList;
import org.dicio.sentences_compiler.util.StringNormalizer;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.Normalizer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class Word extends WordBase {

    private final String value;
    private final boolean diacriticsSensitive;

    /**
     * @param value the value of the word, made of only letters
     * @param diacriticsSensitive if true match the word exactly, otherwise ignore differences in
     *                            diacritics/accents (see e.g. CTRL+F -> Match Diacritics in
     *                            Firefox)
     */
    public Word(final String value, final boolean diacriticsSensitive) {
        this.value = value;
        this.diacriticsSensitive = diacriticsSensitive;
    }

    public String getValue() {
        return value;
    }

    public boolean isDiacriticsSensitive() {
        return diacriticsSensitive;
    }


    @Override
    public void compileToJava(final OutputStreamWriter output,
                              final String variableName) throws IOException {
        if (diacriticsSensitive) {
            output.write("new DiacriticsSensitiveWord(\"");
            output.write(value);
        } else {
            output.write("new DiacriticsInsensitiveWord(\"");
            output.write(StringNormalizer.nfkdNormalize(value));
        }

        output.write("\",");
        super.compileToJava(output, variableName);
        output.write(")");
    }

    @Override
    public Set<String> getCapturingGroupNames() {
        return Collections.emptySet();
    }

    @Override
    public List<String> buildAlternatives() {
        return Collections.singletonList(value);
    }
}
