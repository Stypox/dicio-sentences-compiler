package com.dicio.sentences_compiler.parser;

import com.dicio.sentences_compiler.construct.Section;
import com.dicio.sentences_compiler.construct.Sentence;
import com.dicio.sentences_compiler.lexer.Tokenizer;
import com.dicio.sentences_compiler.util.CompilerError;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class ParserTest {
    private static ArrayList<Section> getSections(String s, String inputStreamName) throws IOException, CompilerError {
        Charset charset = Charset.forName("unicode");
        InputStream stream = new ByteArrayInputStream(s.getBytes(charset));
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize(new InputStreamReader(stream, charset), inputStreamName);
        Parser parser = new Parser(tokenizer.getTokenStream());
        return parser.parse();
    }
    private static ArrayList<Section> getSections(String s) throws IOException, CompilerError {
        return getSections(s, "");
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

    private static void assertSentenceUnfoldsTo(Sentence sentence, String sentenceId, int line, int capturingGroups, String[] unfoldedStrings) {
        assertEquals(sentenceId, sentence.getSentenceId());
        assertEquals(line, sentence.getLine());
        assertEquals(capturingGroups, sentence.numberOfCapturingGroups());

        ArrayList<ArrayList<String>> unfoldedWords = sentence.getSentenceConstructs().unfold();
        for (String unfoldedString : unfoldedStrings) {
            ArrayList<String> sentenceWords = new ArrayList<>(Arrays.asList(unfoldedString.split(" ")));
            sentenceWords.removeAll(Arrays.asList(""));

            assertTrue("Unfolded sentence does not contain \"" + unfoldedString + "\"", unfoldedWords.contains(sentenceWords));
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
                "C_5 : 2\n" +
                "[D] (h|i) (j) (k)?    ;\n" +
                "l ((M)|n) (o((p((Q(((r)))|(S))))t));\n" +
                "[E7] u ..v .. w;\n" +
                "Ff:\n" +
                "low\n" +
                "x..y;\n" +
                "z..;\n");
        assertEquals(3, sections.size());

        assertEquals("A", sections.get(0).getSectionId());
        assertEquals(Section.Specificity.high, sections.get(0).getSpecificity());
        assertEquals(1, sections.get(0).getLine());
        assertEquals(2, sections.get(0).getSentences().size());
        assertSentenceUnfoldsTo(sections.get(0).getSentences().get(0), "", 2, 0, new String[]{
                "a g","b g","g",
        });
        assertSentenceUnfoldsTo(sections.get(0).getSentences().get(1), "B_", 3, 0, new String[]{
                "c ff g","d ff g","e ff g","c ff","d ff","e ff",
        });

        assertEquals("C_5", sections.get(1).getSectionId());
        assertEquals(Section.Specificity.medium, sections.get(1).getSpecificity());
        assertEquals(4, sections.get(1).getLine());
        assertEquals(3, sections.get(1).getSentences().size());
        assertSentenceUnfoldsTo(sections.get(1).getSentences().get(0), "D", 5, 0, new String[]{
                "h j k","i j k","h j","i j",
        });
        assertSentenceUnfoldsTo(sections.get(1).getSentences().get(1), "", 6, 0, new String[]{
                "l m o p q r t","l n o p q r t","l m o p q s t","l n o p q s t",
        });
        assertSentenceUnfoldsTo(sections.get(1).getSentences().get(2), "E7", 7, 2, new String[]{
                "u . v . w",
        });

        assertEquals("Ff", sections.get(2).getSectionId());
        assertEquals(Section.Specificity.low, sections.get(2).getSpecificity());
        assertEquals(8, sections.get(2).getLine());
        assertEquals(2, sections.get(2).getSentences().size());
        assertSentenceUnfoldsTo(sections.get(2).getSentences().get(0), "", 10, 1, new String[]{
                "x . y",
        });
        assertSentenceUnfoldsTo(sections.get(2).getSentences().get(1), "", 11, 1, new String[]{
                "z .",
        });
    }

    @Test
    public void testInvalidInput() throws IOException {
        assertInvalid("a bB",                         CompilerError.Type.invalidToken,                     1,  3,  "bB");
        assertInvalid("a:1 b ..;;",                   CompilerError.Type.expectedSectionOrEndOfFile,       1, 10,  ";");
        assertInvalid("a:1\n|b;",                     CompilerError.Type.expectedSentence,                 2,  2,  "|");
        assertInvalid("a:1 .. b| |c;",                CompilerError.Type.invalidToken,                     1, 11,  "|");
        assertInvalid("a:low b|;",                    CompilerError.Type.invalidToken,                     1,  9,  ";");
        assertInvalid("a:1 b|? (c);",                 CompilerError.Type.invalidToken,                     1,  7,  "?");
        assertInvalid("a:1 b? (c?)??;",               CompilerError.Type.invalidToken,                     1, 13,  "?");
        assertInvalid("a:1\n[] b|c;",                 CompilerError.Type.invalidToken,                     2,  2,  "]");
        assertInvalid("a:1 [|] b|c;",                 CompilerError.Type.invalidToken,                     1,  6,  "|");
        assertInvalid("a:1 [*] b|c;",                 CompilerError.Type.invalidCharacter,                 1,  6,  "*");
        assertInvalid("a:1 [A];",                     CompilerError.Type.expectedSentenceContent,          1,  8,  ";");
        assertInvalid("a:medium ();",                 CompilerError.Type.expectedSentenceConstructList,    1, 11,  ")");
        assertInvalid("a:1\n(());",                   CompilerError.Type.expectedSentenceConstructList,    2,  3,  ")");
        assertInvalid("a:1 [[]] a;",                  CompilerError.Type.invalidToken,                     1,  6,  "[");
        assertInvalid("a",                            CompilerError.Type.invalidToken,                     1,  2,  "");
        assertInvalid("a:\n",                         CompilerError.Type.invalidToken,                     2,  1,  "");
        assertInvalid("a:\nhig",                      CompilerError.Type.invalidSpecificity,               2,  1,  "hig");
        assertInvalid("a:high .;",                    CompilerError.Type.capturingGroupInvalidLength,      1,  9,  ";");
        assertInvalid("a:1 .. ..;",                   CompilerError.Type.capturingGroupInvalidLength,      1,  8,  ".");
        assertInvalid("a:1 (..);",                    CompilerError.Type.capturingGroupInsideParenthesis,  1,  6,  ".");
        assertInvalid("a:1 ..?;",                     CompilerError.Type.optionalCapturingGroup,           1,  7,  "?");
        assertInvalid("a:1 ..|b;",                    CompilerError.Type.optionalCapturingGroup,           1,  7,  "|");
        assertInvalid("a:1 b|..;",                    CompilerError.Type.optionalCapturingGroup,           1,  7,  ".");
        assertInvalid("a:1\n[bB]c..;\n[bB]..d..;",    CompilerError.Type.differentNrOfCapturingGroups,     3, -1,  "bB");
        assertInvalid("a:1\n\n..b..;\n\n\nc;",        CompilerError.Type.differentNrOfCapturingGroups,     6, -1,  "");
        assertInvalid("\na:1\n\n[bB]..c..d..;",       CompilerError.Type.tooManyCapturingGroups,           4, -1,  "bB");
        assertInvalid("a:1\n[bB]..;",                 CompilerError.Type.sentenceCanBeEmpty,               2, -1,  "bB");
        assertInvalid("\n\na:1\n\n..;",               CompilerError.Type.sentenceCanBeEmpty,               5, -1,  "");
        assertInvalid("a:1 b?;",                      CompilerError.Type.sentenceCanBeEmpty,               1, -1,  "");
        assertInvalid("a:1\nb|c? ..((d|e)|f)?..;",    CompilerError.Type.sentenceCanBeEmpty,               2, -1,  "");
        assertInvalid("Aa:1 a;\nAa:1 b;",             CompilerError.Type.duplicateSectionId,               2, -1,  "Aa");
        assertInvalid("\nAa:1 a;\n\nB:1 b;\nAa:1 c;", CompilerError.Type.duplicateSectionId,               5, -1,  "Aa");
    }


    @Test
    public void testNoCaseSensitivity() throws IOException, CompilerError {
        ArrayList<Section> sections = getSections("A:3\nHello HOW are yOu;\n");

        assertSentenceUnfoldsTo(sections.get(0).getSentences().get(0), "", 2, 0, new String[]{
                "hello how are you",
        });
    }
}