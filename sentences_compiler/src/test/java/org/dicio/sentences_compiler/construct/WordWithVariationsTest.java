package org.dicio.sentences_compiler.construct;

import org.junit.Test;

import java.util.Arrays;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WordWithVariationsTest {

    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    @Test
    public void testToJavaRegex() {
        final WordWithVariations word = new WordWithVariations(
                Arrays.asList(
                        Arrays.asList("à", "bcd", ""),
                        Arrays.asList("èf", "ghì"),
                        Arrays.asList("jk"),
                        Arrays.asList("l", "mno", "p", ""),
                        Arrays.asList("qr", "")),
                false);
        final String actualRegex = word.toJavaRegex();
        assertEquals("(?:a|bcd|)(?:ef|ghi)jk(?:l|mno|p|)(?:qr|)", word.toJavaRegex());
        final Pattern pattern = Pattern.compile(actualRegex);
        assertTrue(pattern.matcher("efjkmnoqr").matches());
        assertFalse(pattern.matcher("ajk").matches());
        assertTrue(pattern.matcher("aefjk").matches());
        assertFalse(pattern.matcher("àefjk").matches());
    }
}
