package com.stypox.sentences_compiler.parser;

import com.stypox.sentences_compiler.parser.construct.Section;
import com.stypox.sentences_compiler.util.CompilerError;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;

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
            if (errorLine != -1 && errorColumn != -1) {
                assertTrue("\""+message+"\" does not contain line number "+errorLine, message.contains(String.valueOf(errorLine)));
                assertTrue("\""+message+"\" does not contain column number "+errorColumn, message.contains(String.valueOf(errorColumn)));
            }
            assertTrue("\""+message+"\" does not contain \""+errorMustContain+"\"", message.contains(errorMustContain));
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
                "a|b?;\n" +
                "[B_](c|d)|e f g?;\n" +
                "5_C :\n" +
                "[D] (h|i) (j) (k)?    ;\n" +
                "l ((m)|n) (o((p((q(((r)))|(s))))t));" +
                "[E7] u ..v .. w;\n" +
                ".. x y..;" +
                "..;\n"); // TODO is this valid?
        assertEquals(2, sections.size());
    }

    @Test
    public void testInvalidInput() throws IOException {
        assertInvalid("a b",           CompilerError.Type.invalidToken,                     1,  3,  "b");
        assertInvalid("a: b ..;;",     CompilerError.Type.expectedSectionOrEndOfFile,       1,  9,  ";");
        assertInvalid("a:\n|b;",       CompilerError.Type.expectedSentence,                 2,  1,  "|");
        assertInvalid("a: .. b| |c;",  CompilerError.Type.invalidToken,                     1, 10,  "|");
        assertInvalid("a: b|;",        CompilerError.Type.invalidToken,                     1,  6,  ";");
        assertInvalid("a: b|? (c);",   CompilerError.Type.invalidToken,                     1,  6,  "?");
        assertInvalid("a: b? (c?)??;", CompilerError.Type.invalidToken,                     1, 12,  "?");
        assertInvalid("a:\n[] b|c;",   CompilerError.Type.invalidToken,                     2,  2,  "]");
        assertInvalid("a: [|] b|c;",   CompilerError.Type.invalidToken,                     1,  5,  "|");
        assertInvalid("a: [*] b|c;",   CompilerError.Type.invalidCharacter,                 1,  5,  "*");
        assertInvalid("a: [A];",       CompilerError.Type.expectedSentenceContent,          1,  7,  ";");
        assertInvalid("a: ();",        CompilerError.Type.expectedSentenceConstructList,    1,  5,  ")");
        assertInvalid("a:\n(());",     CompilerError.Type.expectedSentenceConstructList,    2,  3,  ")");
        assertInvalid("a: [[]] a;",    CompilerError.Type.invalidToken,                     1,  5,  "[");
        assertInvalid("a",             CompilerError.Type.invalidToken,                    -1, -1,  "END OF FILE");
        assertInvalid("a: .;",         CompilerError.Type.capturingGroupInvalidLength,      1,  5,  ";");
        assertInvalid("a: .. ..;",     CompilerError.Type.capturingGroupInvalidLength,      1,  7,  ".");
        assertInvalid("a: (..);",      CompilerError.Type.capturingGroupInsideParenthesis,  1,  5,  ".");
        assertInvalid("a: ..?;",       CompilerError.Type.optionalCapturingGroup,           1,  6,  "?");
        assertInvalid("a: ..|b;",      CompilerError.Type.optionalCapturingGroup,           1,  6,  "|");
        assertInvalid("a: b|..;",      CompilerError.Type.optionalCapturingGroup,           1,  6,  ".");
    }
}