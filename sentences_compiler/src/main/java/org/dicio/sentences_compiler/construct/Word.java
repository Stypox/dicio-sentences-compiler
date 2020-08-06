package org.dicio.sentences_compiler.construct;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.Collator;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

public final class Word extends WordBase {

    private static Collator getCollator() {
        final Collator collator = Collator.getInstance(Locale.ENGLISH);
        collator.setStrength(Collator.PRIMARY);
        // note: this is not FULL_COMPOSITION, some accented characters could not be considered the same
        collator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
        return collator;
    }

    private static final Collator collator = getCollator();


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
            output.write("\",");
        } else {
            output.write("new DiacriticsInsensitiveWord(new byte[]{");
            final byte[] collationKey = collator.getCollationKey(value).toByteArray();

            for (int i = 0; i < collationKey.length; i++) {
                if (i != 0) {
                    output.write(",");
                }
                output.write(String.valueOf(collationKey[i]));
            }

            output.write("},");
        }

        super.compileToJava(output, variableName);
        output.write(")");
    }

    @Override
    public Set<String> getCapturingGroupNames() {
        return Collections.emptySet();
    }
}
