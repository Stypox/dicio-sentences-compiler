package org.dicio.sentences_compiler.lexer;

import org.dicio.sentences_compiler.util.CompilerError;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class TokenizerTest {

    private static TokenStream getTokens(String s, String inputStreamName) throws IOException, CompilerError {
        Charset charset = StandardCharsets.UTF_8;
        InputStream stream = new ByteArrayInputStream(s.getBytes(charset));
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenize(new InputStreamReader(stream, charset), inputStreamName);
        return tokenizer.getTokenStream();
    }
    private static TokenStream getTokens(String s) throws IOException, CompilerError {
        return getTokens(s, "");
    }

    private static void assertTokenEqualTo(Token token, Token.Type type, String value, int line, int column) {
        assertTrue("Token type is not of type "+type, token.isType(type));
        assertTrue("Token's value \""+token.getValue()+"\" is not \""+value+"\"", token.isValue(value));
        assertEquals(line, token.getLine());
        assertEquals(column, token.getColumn());
    }
    private static void assertInvalidCharacter(String input, int errorLine, int errorColumn, String errorMustContain) throws IOException {
        final String inputStreamName = "MyGoodFILE!";
        try {
            getTokens(input, inputStreamName);
            fail("No error thrown with invalid input");
        } catch (CompilerError compilerError) {
            String message = compilerError.getMessage();
            assertTrue("\""+message+"\" is not of type \""+ CompilerError.Type.invalidCharacter.toString()+"\"", message.contains(CompilerError.Type.invalidCharacter.toString()));
            assertTrue("\""+message+"\" does not contain input stream name \""+inputStreamName+"\"", message.contains(inputStreamName));
            assertTrue("\""+message+"\" does not contain line number "+errorLine, message.contains(String.valueOf(errorLine)));
            assertTrue("\""+message+"\" does not contain column number "+errorColumn, message.contains(String.valueOf(errorColumn)));
            assertTrue("\""+message+"\" does not contain \""+errorMustContain+"\"", message.contains(errorMustContain));
        }
    }

    @Test
    public void testEmptyInput() throws IOException, CompilerError {
        assertTrue(getTokens("").get(0).isType(Token.Type.endOfFile));
        assertTrue(getTokens("\n").get(0).isType(Token.Type.endOfFile));
        assertTrue(getTokens("  \t  \n\t\t\n  ").get(0).isType(Token.Type.endOfFile));
        assertTrue(getTokens("    # hello: # ## | | ; world   \n#").get(0).isType(Token.Type.endOfFile));
    }

    @Test
    public void testValidInput() throws IOException, CompilerError {
        TokenStream tokens = getTokens("cat_name1:     # hello world  \n" +
                "  [sent_name1] hel<l|lo>?      hi|(bye bye);       \n" +
                "..;");
        assertTrue(tokens.get(-1).isEmpty());
        assertTokenEqualTo(tokens.get(0),  Token.Type.lettersPlusOther, "cat_name1",  1,  1);
        assertTokenEqualTo(tokens.get(1),  Token.Type.grammar,          ":",          1, 10);
        assertTokenEqualTo(tokens.get(2),  Token.Type.grammar,          "[",          2,  3);
        assertTokenEqualTo(tokens.get(3),  Token.Type.lettersPlusOther, "sent_name1", 2,  4);
        assertTokenEqualTo(tokens.get(4),  Token.Type.grammar,          "]",          2, 14);
        assertTokenEqualTo(tokens.get(5),  Token.Type.letters,          "hel",        2, 16);
        assertTokenEqualTo(tokens.get(6),  Token.Type.grammar,          "<",          2, 19);
        assertTokenEqualTo(tokens.get(7),  Token.Type.letters,          "l",          2, 20);
        assertTokenEqualTo(tokens.get(8),  Token.Type.grammar,          "|",          2, 21);
        assertTokenEqualTo(tokens.get(9),  Token.Type.letters,          "lo",         2, 22);
        assertTokenEqualTo(tokens.get(10), Token.Type.grammar,          "> ",         2, 24);
        assertTokenEqualTo(tokens.get(11), Token.Type.grammar,          "?",          2, 25);
        assertTokenEqualTo(tokens.get(12), Token.Type.letters,          "hi",         2, 32);
        assertTokenEqualTo(tokens.get(13), Token.Type.grammar,          "|",          2, 34);
        assertTokenEqualTo(tokens.get(14), Token.Type.grammar,          "(",          2, 35);
        assertTokenEqualTo(tokens.get(15), Token.Type.letters,          "bye",        2, 36);
        assertTokenEqualTo(tokens.get(16), Token.Type.letters,          "bye",        2, 40);
        assertTokenEqualTo(tokens.get(17), Token.Type.grammar,          ")",          2, 43);
        assertTokenEqualTo(tokens.get(18), Token.Type.grammar,          ";",          2, 44);
        assertTokenEqualTo(tokens.get(19), Token.Type.grammar,          ".",          3,  1);
        assertTokenEqualTo(tokens.get(20), Token.Type.grammar,          ".",          3,  2);
        assertTokenEqualTo(tokens.get(21), Token.Type.grammar,          ";",          3,  3);
        assertTrue(tokens.get(22).isType(Token.Type.endOfFile));
        assertTrue(tokens.get(23).isEmpty());
    }

    @Test
    public void testInvalidInput() throws IOException {
        assertInvalidCharacter("\n\n\n+\n+",              4, 1, "+");
        assertInvalidCharacter("\n[[[]{",                 2, 5, "{");
        assertInvalidCharacter("   \n  *\n\n+",           2, 3, "*");
        assertInvalidCharacter("[[][\n\t]](\n(%)):;)):(", 3, 2, "%");
    }

    @Test
    public void testAngleBracketsInInput() throws IOException, CompilerError {
        TokenStream tokens = getTokens(">a\n< > <<<>>><<< <bcd>\n>efg<\th<i>j<?<k>?>l m<");
        assertTokenEqualTo(tokens.get(0),  Token.Type.grammar, ">",   1,  1);
        assertTokenEqualTo(tokens.get(1),  Token.Type.letters, "a",   1,  2);
        assertTokenEqualTo(tokens.get(2),  Token.Type.grammar, " <",  2,  1);
        assertTokenEqualTo(tokens.get(3),  Token.Type.grammar, "> ",  2,  3);
        assertTokenEqualTo(tokens.get(4),  Token.Type.grammar, " <",  2,  5);
        assertTokenEqualTo(tokens.get(5),  Token.Type.grammar, " <",  2,  6);
        assertTokenEqualTo(tokens.get(6),  Token.Type.grammar, " <",  2,  7);
        assertTokenEqualTo(tokens.get(7),  Token.Type.grammar, "> ",  2,  8);
        assertTokenEqualTo(tokens.get(8),  Token.Type.grammar, "> ",  2,  9);
        assertTokenEqualTo(tokens.get(9),  Token.Type.grammar, ">",   2, 10);
        assertTokenEqualTo(tokens.get(10), Token.Type.grammar, "<",   2, 11);
        assertTokenEqualTo(tokens.get(11), Token.Type.grammar, " <",  2, 12);
        assertTokenEqualTo(tokens.get(12), Token.Type.grammar, " <",  2, 13);
        assertTokenEqualTo(tokens.get(13), Token.Type.grammar, " <",  2, 15);
        assertTokenEqualTo(tokens.get(14), Token.Type.letters, "bcd", 2, 16);
        assertTokenEqualTo(tokens.get(15), Token.Type.grammar, "> ",  2, 19);
        assertTokenEqualTo(tokens.get(16), Token.Type.grammar, ">",   3,  1);
        assertTokenEqualTo(tokens.get(17), Token.Type.letters, "efg", 3,  2);
        assertTokenEqualTo(tokens.get(18), Token.Type.grammar, "<",   3,  5);
        assertTokenEqualTo(tokens.get(19), Token.Type.letters, "h",   3,  7);
        assertTokenEqualTo(tokens.get(20), Token.Type.grammar, "<",   3,  8);
        assertTokenEqualTo(tokens.get(21), Token.Type.letters, "i",   3,  9);
        assertTokenEqualTo(tokens.get(22), Token.Type.grammar, ">",   3, 10);
        assertTokenEqualTo(tokens.get(23), Token.Type.letters, "j",   3, 11);
        assertTokenEqualTo(tokens.get(24), Token.Type.grammar, "<",   3, 12);
        assertTokenEqualTo(tokens.get(25), Token.Type.grammar, "?",   3, 13);
        assertTokenEqualTo(tokens.get(26), Token.Type.grammar, " <",  3, 14);
        assertTokenEqualTo(tokens.get(27), Token.Type.letters, "k",   3, 15);
        assertTokenEqualTo(tokens.get(28), Token.Type.grammar, "> ",  3, 16);
        assertTokenEqualTo(tokens.get(29), Token.Type.grammar, "?",   3, 17);
        assertTokenEqualTo(tokens.get(30), Token.Type.grammar, ">",   3, 18);
        assertTokenEqualTo(tokens.get(31), Token.Type.letters, "l",   3, 19);
        assertTokenEqualTo(tokens.get(32), Token.Type.letters, "m",   3, 21);
        assertTokenEqualTo(tokens.get(33), Token.Type.grammar, "<",   3, 22);

        tokens = getTokens("<");
        assertTokenEqualTo(tokens.get(0), Token.Type.grammar, " <",  1, 1);

        tokens = getTokens(">");
        assertTokenEqualTo(tokens.get(0), Token.Type.grammar, "> ",  1, 1);
    }
}