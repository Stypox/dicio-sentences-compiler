package com.dicio.sentences_compiler.parser;

import com.dicio.sentences_compiler.parser.construct.Section;
import com.dicio.sentences_compiler.parser.construct.Sentence;
import com.dicio.sentences_compiler.util.CompilerError;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class ParserTest {
    private static Parser fromString(String s) throws IOException, CompilerError {
        InputStream stream = new ByteArrayInputStream(s.getBytes(Charset.forName("unicode")));
        return new Parser(stream);
    }
    private static ArrayList<Section> getSections(String s) throws IOException, CompilerError {
        return fromString(s).parse();
    }
    private static void assertInvalid(String input, CompilerError.Type errorType, int errorLine, int errorColumn, String errorMustContain) throws IOException {
        try {
            getSections(input);
            fail("No error thrown with invalid input");
        } catch (CompilerError compilerError) {
            String message = compilerError.getMessage();
            assertTrue("\""+message+"\" is not of type \""+errorType.toString()+"\"",  message.contains(errorType.toString()));
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
                "A:\n" +
                "a|b? G;\n" +
                "[B_](c|d)|e FF g?;\n" +
                "5_C :\n" +
                "[D] (h|i) (j) (k)?    ;\n" +
                "l ((M)|n) (o((p((Q(((r)))|(S))))t));\n" +
                "[E7] u ..v .. w;\n" +
                "Ff:\n" +
                "x..y;\n" +
                "z..;\n");
        assertEquals(3, sections.size());

        assertEquals("A", sections.get(0).getSectionId());
        assertEquals(1, sections.get(0).getLine());
        assertEquals(2, sections.get(0).getSentences().size());
        assertSentenceUnfoldsTo(sections.get(0).getSentences().get(0), "", 2, 0, new String[]{
                "a G","b G","G",
        });
        assertSentenceUnfoldsTo(sections.get(0).getSentences().get(1), "B_", 3, 0, new String[]{
                "c FF g","d FF g","e FF g","c FF","d FF","e FF",
        });

        assertEquals("5_C", sections.get(1).getSectionId());
        assertEquals(4, sections.get(1).getLine());
        assertEquals(3, sections.get(1).getSentences().size());
        assertSentenceUnfoldsTo(sections.get(1).getSentences().get(0), "D", 5, 0, new String[]{
                "h j k","i j k","h j","i j",
        });
        assertSentenceUnfoldsTo(sections.get(1).getSentences().get(1), "", 6, 0, new String[]{
                "l M o p Q r t","l n o p Q r t","l M o p Q S t","l n o p Q S t",
        });
        assertSentenceUnfoldsTo(sections.get(1).getSentences().get(2), "E7", 7, 2, new String[]{
                "u . v . w",
        });

        assertEquals("Ff", sections.get(2).getSectionId());
        assertEquals(8, sections.get(2).getLine());
        assertEquals(2, sections.get(2).getSentences().size());
        assertSentenceUnfoldsTo(sections.get(2).getSentences().get(0), "", 9, 1, new String[]{
                "x . y",
        });
        assertSentenceUnfoldsTo(sections.get(2).getSentences().get(1), "", 10, 1, new String[]{
                "z .",
        });
    }

    @Test
    public void testInvalidInput() throws IOException {
        assertInvalid("a bB",                     CompilerError.Type.invalidToken,                     1,  3,  "bB");
        assertInvalid("a: b ..;;",                CompilerError.Type.expectedSectionOrEndOfFile,       1,  9,  ";");
        assertInvalid("a:\n|b;",                  CompilerError.Type.expectedSentence,                 2,  1,  "|");
        assertInvalid("a: .. b| |c;",             CompilerError.Type.invalidToken,                     1, 10,  "|");
        assertInvalid("a: b|;",                   CompilerError.Type.invalidToken,                     1,  6,  ";");
        assertInvalid("a: b|? (c);",              CompilerError.Type.invalidToken,                     1,  6,  "?");
        assertInvalid("a: b? (c?)??;",            CompilerError.Type.invalidToken,                     1, 12,  "?");
        assertInvalid("a:\n[] b|c;",              CompilerError.Type.invalidToken,                     2,  2,  "]");
        assertInvalid("a: [|] b|c;",              CompilerError.Type.invalidToken,                     1,  5,  "|");
        assertInvalid("a: [*] b|c;",              CompilerError.Type.invalidCharacter,                 1,  5,  "*");
        assertInvalid("a: [A];",                  CompilerError.Type.expectedSentenceContent,          1,  7,  ";");
        assertInvalid("a: ();",                   CompilerError.Type.expectedSentenceConstructList,    1,  5,  ")");
        assertInvalid("a:\n(());",                CompilerError.Type.expectedSentenceConstructList,    2,  3,  ")");
        assertInvalid("a: [[]] a;",               CompilerError.Type.invalidToken,                     1,  5,  "[");
        assertInvalid("a",                        CompilerError.Type.invalidToken,                    -1, -1,  "END OF FILE");
        assertInvalid("a: .;",                    CompilerError.Type.capturingGroupInvalidLength,      1,  5,  ";");
        assertInvalid("a: .. ..;",                CompilerError.Type.capturingGroupInvalidLength,      1,  7,  ".");
        assertInvalid("a: (..);",                 CompilerError.Type.capturingGroupInsideParenthesis,  1,  5,  ".");
        assertInvalid("a: ..?;",                  CompilerError.Type.optionalCapturingGroup,           1,  6,  "?");
        assertInvalid("a: ..|b;",                 CompilerError.Type.optionalCapturingGroup,           1,  6,  "|");
        assertInvalid("a: b|..;",                 CompilerError.Type.optionalCapturingGroup,           1,  6,  ".");
        assertInvalid("a:\n[bB]c..;\n[bB]..d..;", CompilerError.Type.differentNrOfCapturingGroups,     3, -1,  "bB");
        assertInvalid("a:\n\n..b..;\n\n\nc;",     CompilerError.Type.differentNrOfCapturingGroups,     6, -1,  "");
        assertInvalid("\na:\n\n[bB]..c..d..;",    CompilerError.Type.tooManyCapturingGroups,           4, -1,  "bB");
        assertInvalid("a:\n[bB]..;",              CompilerError.Type.sentenceCanBeEmpty,               2, -1,  "bB");
        assertInvalid("\n\na:\n\n..;",            CompilerError.Type.sentenceCanBeEmpty,               5, -1,  "");
        assertInvalid("a: b?;",                   CompilerError.Type.sentenceCanBeEmpty,               1, -1,  "");
        assertInvalid("a:\nb|c? ..((d|e)|f)?..;", CompilerError.Type.sentenceCanBeEmpty,               2, -1,  "");
        assertInvalid("Aa: a;\nAa: b;",           CompilerError.Type.duplicateSectionId,               2, -1,  "Aa");
        assertInvalid("\nAa:a;\n\nB:b;\nAa:c;",   CompilerError.Type.duplicateSectionId,               5, -1,  "Aa");
    }
}