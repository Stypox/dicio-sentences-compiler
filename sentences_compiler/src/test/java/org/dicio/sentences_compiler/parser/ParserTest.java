package org.dicio.sentences_compiler.parser;

import static org.dicio.sentences_compiler.util.CompilerError.Type.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.dicio.sentences_compiler.construct.CapturingGroup;
import org.dicio.sentences_compiler.construct.Section;
import org.dicio.sentences_compiler.construct.Sentence;
import org.dicio.sentences_compiler.construct.Word;
import org.dicio.sentences_compiler.construct.WordBase;
import org.dicio.sentences_compiler.construct.WordWithVariations;
import org.dicio.sentences_compiler.lexer.Tokenizer;
import org.dicio.sentences_compiler.util.CompilerError;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class ParserTest {
    private static ArrayList<Section> getSections(String s, String inputStreamName) throws IOException, CompilerError {
        Charset charset = StandardCharsets.UTF_8;
        InputStream stream = new ByteArrayInputStream(s.getBytes(charset));
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize(new InputStreamReader(stream, charset), inputStreamName);
        Parser parser = new Parser(tokenizer.getTokenStream());
        return parser.parse();
    }

    private static ArrayList<Section> getSections(String s) throws IOException, CompilerError {
        return getSections(s, "");
    }

    private static Integer[] entry(final Integer... entryPointWordIndices) {
        return entryPointWordIndices;
    }

    private static List<String> part(final String... variations) {
        return Arrays.asList(variations);
    }

    @SafeVarargs
    private static List<List<String>> parts(final List<String>... parts) {
        return Arrays.asList(parts);
    }

    private static WordBase buildWord(final WordBase wordBase,
                                      final int minimumSkippedWordsToEnd,
                                      final Integer... nextIndices) {
        wordBase.setMinimumSkippedWordsToEnd(minimumSkippedWordsToEnd);
        wordBase.findNextIndices(new HashSet<>(Arrays.asList(nextIndices)));
        return wordBase;
    }

    private static WordBase w(final String value,
                              final int minimumSkippedWordsToEnd,
                              final Integer... nextIndices) {
        return buildWord(new Word(value, false), minimumSkippedWordsToEnd, nextIndices);
    }

    @SuppressWarnings("SameParameterValue")
    private static WordBase wd(final String value,
                               final int minimumSkippedWordsToEnd,
                               final Integer... nextIndices) {
        return buildWord(new Word(value, true), minimumSkippedWordsToEnd, nextIndices);
    }

    private static WordBase wv(final List<List<String>> parts,
                               final int minimumSkippedWordsToEnd,
                               final Integer... nextIndices) {
        return buildWord(new WordWithVariations(parts, false), minimumSkippedWordsToEnd, nextIndices);
    }

    @SuppressWarnings("SameParameterValue")
    private static WordBase wvd(final List<List<String>> parts,
                                final int minimumSkippedWordsToEnd,
                                final Integer... nextIndices) {
        return buildWord(new WordWithVariations(parts, true), minimumSkippedWordsToEnd, nextIndices);
    }

    private static WordBase capt(final String value,
                                 final int minimumSkippedWordsToEnd,
                                 final Integer... nextIndices) {
        return buildWord(new CapturingGroup(value), minimumSkippedWordsToEnd, nextIndices);
    }


    private static <T> void assertUniqueCollectionEquals(final Collection<T> expected, final Collection<T> actual) {
        assertEquals("Expected: " + expected + "\nActual: " + actual + "\n", expected.size(), actual.size());
        assertTrue("Expected: " + expected + "\nActual: " + actual,
                actual.containsAll(expected));
    }

    private static <T> void assertUniqueCollectionEquals(final T[] expected, final Collection<T> actual) {
        assertUniqueCollectionEquals(Arrays.asList(expected), actual);
    }

    private static void assertInvalid(String input, CompilerError.Type errorType, int errorLine, int errorColumn, String errorMustContain) throws IOException {
        String inputStreamName = "MyBeautifulFile :-D";
        try {
            getSections(input, inputStreamName);
            fail("No error thrown with invalid input");
        } catch (CompilerError compilerError) {
            String message = compilerError.getMessage();
            assertTrue("\""+message+"\" is not of type \""+errorType.toString()+"\"",  message.contains(errorType.toString()));
            assertTrue("\""+message+"\" does not contain input stream name \""+inputStreamName+"\"", message.contains(inputStreamName));
            if (errorLine != -1) {
                assertTrue("\""+message+"\" does not contain line number "+errorLine, message.contains(String.valueOf(errorLine)));
            }
            if (errorColumn != -1) {
                assertTrue("\""+message+"\" does not contain column number "+errorColumn, message.contains(String.valueOf(errorColumn)));
            }
            assertTrue("\""+message+"\" does not contain \""+errorMustContain+"\"", message.contains(errorMustContain));
        }
    }

    private static void assertSentence(final Sentence sentence,
                                       final String sentenceId,
                                       final int line,
                                       final Integer[] entryPointWordIndices,
                                       final WordBase... words) {

        assertEquals(sentenceId, sentence.getSentenceId());
        assertEquals(line, sentence.getLine());
        assertUniqueCollectionEquals(entryPointWordIndices, sentence.getEntryPointWordIndices());

        List<WordBase> actualWords = sentence.getCompiledWords();
        assertEquals(words.length, actualWords.size());
        for (int i = 0; i < words.length; ++i) {
            if (words[i] instanceof Word) {
                assertTrue(actualWords.get(i) instanceof Word);
                assertEquals(((Word) words[i]).getValue(), ((Word) actualWords.get(i)).getValue());
                assertEquals(((Word) words[i]).isDiacriticsSensitive(), ((Word) actualWords.get(i)).isDiacriticsSensitive());
            } else if (words[i] instanceof WordWithVariations) {
                final WordWithVariations word = (WordWithVariations) words[i];
                final WordWithVariations actualWord = (WordWithVariations) actualWords.get(i);
                assertTrue(actualWords.get(i) instanceof WordWithVariations);
                assertEquals(word.getParts().size(), actualWord.getParts().size());
                for (int j = 0; j < word.getParts().size(); ++j) {
                    assertUniqueCollectionEquals(word.getParts().get(j), actualWord.getParts().get(j));
                }
                assertEquals(((WordWithVariations) words[i]).isDiacriticsSensitive(), ((WordWithVariations) actualWords.get(i)).isDiacriticsSensitive());
            } else {
                assertTrue(actualWords.get(i) instanceof CapturingGroup);
                assertEquals(((CapturingGroup) words[i]).getName(), ((CapturingGroup) actualWords.get(i)).getName());
            }
            assertEquals(i, actualWords.get(i).getIndex());
            assertUniqueCollectionEquals(words[i].getNextIndices(), actualWords.get(i).getNextIndices());
        }
    }

    @Test
    public void testEmptyInput() throws IOException, CompilerError {
        assertTrue(getSections("").isEmpty());
        assertTrue(getSections("\n").isEmpty());
        assertTrue(getSections("  \t  \n\t\t\n  ").isEmpty());
        assertTrue(getSections("    # hello: # ## | | ; world   \n#").isEmpty());
    }

    @Test
    public void testValidInput() throws IOException, CompilerError {
        ArrayList<Section> sections = getSections(
                "A:high\n" +
                "a|b? G;\n" +
                "[B_](c|d)|e FF g?;\n" +
                "C_5 : medium\n" +
                "[D] (h|i) (j) (k)?    ;\n" +
                "l ((M)|n) (o((p((Q(((r)))|(S))))t));\n" +
                "[E7] u .a_b.v.c.? w;\n" +
                "Ff:\n" +
                "low\n" +
                "x?.d_.y;\n" +
                "\"zàÈ\" .C7.?;\n" +
                "<a|B>c<C|d?><è?>? <F?>g \"j<k?>o<h|Ì>\"?;\n");
        assertEquals(3, sections.size());

        assertEquals("A", sections.get(0).getSectionId());
        assertEquals(Section.Specificity.high, sections.get(0).getSpecificity());
        assertEquals(1, sections.get(0).getLine());
        assertEquals(2, sections.get(0).getSentences().size());
        assertSentence(sections.get(0).getSentences().get(0), "", 2, entry(0, 1, 2),
                w("a", 2, 2), w("b", 2, 2), w("g", 1, 3));
        assertSentence(sections.get(0).getSentences().get(1), "B_", 3, entry(0, 1, 2),
                w("c", 2, 3), w("d", 2, 3), w("e", 2, 3), w("ff", 1, 4, 5), w("g", 1, 5));

        assertEquals("C_5", sections.get(1).getSectionId());
        assertEquals(Section.Specificity.medium, sections.get(1).getSpecificity());
        assertEquals(4, sections.get(1).getLine());
        assertEquals(3, sections.get(1).getSentences().size());
        assertSentence(sections.get(1).getSentences().get(0), "D", 5, entry(0, 1),
                w("h", 2, 2), w("i", 2, 2), w("j", 1, 3, 4), w("k", 1, 4));
        assertSentence(sections.get(1).getSentences().get(1), "", 6, entry(0),
                w("l", 7, 1, 2), w("m", 6, 3), w("n", 6, 3), w("o", 5, 4), w("p", 4, 5), w("q", 3, 6, 7), w("r", 2, 8), w("s", 2, 8), w("t", 1, 9));
        assertSentence(sections.get(1).getSentences().get(2), "E7", 7, entry(0),
                w("u", 4, 1), capt("a_b", 3, 2), w("v", 2, 3, 4), capt("c", 2, 4), w("w", 1, 5));

        assertEquals("Ff", sections.get(2).getSectionId());
        assertEquals(Section.Specificity.low, sections.get(2).getSpecificity());
        assertEquals(8, sections.get(2).getLine());
        assertEquals(3, sections.get(2).getSentences().size());
        assertSentence(sections.get(2).getSentences().get(0), "", 10, entry(0, 1),
                w("x", 3, 1), capt("d_", 2, 2), w("y", 1, 3));
        assertSentence(sections.get(2).getSentences().get(1), "", 11, entry(0),
                wd("zàè", 1, 1, 2), capt("C7", 1, 2));
        assertSentence(sections.get(2).getSentences().get(2), "", 12, entry(0, 1),
                wv(parts(part("a","b"), part("c"), part("c","d",""), part("è","")), 2, 1),
                wv(parts(part("f",""), part("g")), 1, 2, 3),
                wvd(parts(part("j"), part("k",""), part("o"), part("h","ì")), 1, 3));
    }

    @Test
    public void testInvalidInput() throws IOException {
        assertInvalid("a:low [*] b|c;",       invalidCharacter,                 1,  8, "*");
        assertInvalid("a:low b .c.;;",        expectedSectionOrEndOfFile,       1, 13, ";");
        assertInvalid("a bB",                 invalidToken,                     1,  3, "bB");
        assertInvalid("a:low .b_C. d| |e;",   invalidToken,                     1, 16, "|");
        assertInvalid("a:low b|;",            invalidToken,                     1,  9, ";");
        assertInvalid("a:low b|? (c);",       invalidToken,                     1,  9, "?");
        assertInvalid("a:low b? (c?)??;",     invalidToken,                     1, 15, "?");
        assertInvalid("a:low\n[] b|c;",       invalidToken,                     2,  2, "]");
        assertInvalid("a:low [|] b|c;",       invalidToken,                     1,  8, "|");
        assertInvalid("a:low [[]] a;",        invalidToken,                     1,  8, "[");
        assertInvalid("a",                    invalidToken,                     1,  2, "");
        assertInvalid("a:\n",                 invalidToken,                     2,  1, "");
        assertInvalid("\na:high b.\nc;",      invalidToken,                     3,  2, ";");
        assertInvalid("z1:medium\n.g..\nè;",  invalidToken,                     3,  2, "\".\"");
        assertInvalid("c:low\nah|\"he ho\";", invalidToken,                     2,  8, "ho");
        assertInvalid("c:low\"par\n\n|ap c;", invalidToken,                     3,  1, "|");
        assertInvalid("a:low \"b|\";",        invalidToken,                     1,  9, "'\"'");
        assertInvalid("k:high\na r8;",        invalidToken,                     2,  3, "r8");
        assertInvalid("false:low a;",         invalidSectionId,                 1,  1, "false");
        assertInvalid("9hi:low a;",           invalidSectionId,                 1,  1, "9hi");
        assertInvalid("è9:medium a;",         invalidSectionId,                 1,  1, "è9");
        assertInvalid("a:media a;",           invalidSpecificity,               1,  1, "media");
        assertInvalid("a:\nhig;",             invalidSpecificity,               2,  1, "hig");
        assertInvalid("a:low\n|b;",           expectedSentence,                 2,  1, "|");
        assertInvalid("a_a:low;",             expectedSentence,                 1,  8, ";");
        assertInvalid("a:low h3ll0;",         expectedSentence,                 1,  7, "h3ll0");
        assertInvalid("a:low [A];",           expectedSentenceContent,          1, 10, ";");
        assertInvalid("\na:medium ();",       expectedSentenceConstructList,    2, 11, ")");
        assertInvalid("a:low\n(());",         expectedSentenceConstructList,    2,  3, ")");
        assertInvalid("a:low[x\n]\"\";",      expectedWordValue,                2,  3, "\"");
        assertInvalid("a:low\na|b\"",         expectedWordValue,                2,  5, "'\"'");
        assertInvalid("Z:high <a?|b>\n",      invalidVariationsGroup,           1, 11, "|");
        assertInvalid("Z:high\n<a(b)|c>\n",   invalidVariationsGroup,           2,  3, "(");
        assertInvalid("Z:high\n<>",           invalidVariationsGroup,           2,  2, ">");
        assertInvalid("Z:high\n<ab|>\n",      invalidVariationsGroup,           2,  5, ">");
        assertInvalid("Z:high <abcd>",        invalidVariationsGroup,           1, 13, ">");
        assertInvalid("b:low\n[a] ..;",       expectedCapturingGroupName,       2,  6, ".");
        assertInvalid("b:low [G] a .|;",      expectedCapturingGroupName,       1, 14, "|");
        assertInvalid("c:high hi\n.caè.;",    invalidCapturingGroupName,        2,  2, "caè");
        assertInvalid("c:high hi\n.9AC.;",    invalidCapturingGroupName,        2,  2, "9AC");
        assertInvalid("a:low b?;",            sentenceCanBeEmpty,               1, -1, "");
        assertInvalid("Aa:low a;\nAa:low b;", duplicateSectionId,               2, -1, "Aa");
        assertInvalid("\nAa:low a;\n\nB:low b;\nAa:low c;", duplicateSectionId, 5, -1, "Aa");
    }


    @Test
    public void testNoCaseSensitivity() throws IOException, CompilerError {
        ArrayList<Section> sections = getSections("A:high\nHello HOW are yOu .Name_Of_Person.;\n");

        assertSentence(sections.get(0).getSentences().get(0), "", 2, entry(0),
                w("hello", 5, 1), w("how", 4, 2), w("are", 3, 3), w("you", 2, 4), capt("Name_Of_Person", 1, 5));
    }

    @Test
    public void testMultipleFiles() throws IOException, CompilerError {
        String s1 = "a:medium [s1] b;";
        String s2 = "c:low [s2] d;";
        Charset charset = StandardCharsets.UTF_8;
        Tokenizer tokenizer = new Tokenizer();

        InputStream stream1 = new ByteArrayInputStream(s1.getBytes(charset));
        tokenizer.tokenize(new InputStreamReader(stream1, charset), "1");
        InputStream stream2 = new ByteArrayInputStream(s2.getBytes(charset));
        tokenizer.tokenize(new InputStreamReader(stream2, charset), "2");

        Parser parser = new Parser(tokenizer.getTokenStream());
        ArrayList<Section> sections = parser.parse();

        assertEquals("1", sections.get(0).getInputStreamName());
        assertEquals(Section.Specificity.medium, sections.get(0).getSpecificity());
        assertEquals("a", sections.get(0).getSectionId());
        assertEquals("1", sections.get(0).getSentences().get(0).getInputStreamName());
        assertEquals("s1", sections.get(0).getSentences().get(0).getSentenceId());

        assertEquals("2", sections.get(1).getInputStreamName());
        assertEquals(Section.Specificity.low, sections.get(1).getSpecificity());
        assertEquals("c", sections.get(1).getSectionId());
        assertEquals("2", sections.get(1).getSentences().get(0).getInputStreamName());
        assertEquals("s2", sections.get(1).getSentences().get(0).getSentenceId());
    }
}