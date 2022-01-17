package org.dicio.sentences_compiler.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringNormalizer {

    private static final Pattern diacriticalMarksRemover =
            Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    public static String nfkdNormalize(final String string) {
        final String normalized = Normalizer.normalize(string, Normalizer.Form.NFKD);
        return diacriticalMarksRemover.matcher(normalized).replaceAll("");
    }
}
