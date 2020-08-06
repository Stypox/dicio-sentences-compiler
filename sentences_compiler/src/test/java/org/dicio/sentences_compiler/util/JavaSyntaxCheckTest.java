package org.dicio.sentences_compiler.util;

import org.dicio.sentences_compiler.lexer.Token;

import org.junit.Test;

import static org.dicio.sentences_compiler.util.CompilerError.Type.invalidSectionId;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class JavaSyntaxCheckTest {

    private void checkValidVariableName(final String name) throws CompilerError {
        JavaSyntaxCheck.checkValidJavaVariableName(name,
                new Token(Token.Type.lettersPlusOther, name, "nAmE", 51, 13),
                invalidSectionId);
    }

    private void assertInvalid(final String name, final String errorMustContain) {
        try {
            checkValidVariableName(name);
            fail("No error thrown with invalid java variable name");
        } catch (final CompilerError compilerError) {
            String message = compilerError.getMessage();
            assertTrue("\""+message+"\" is not of type \""+invalidSectionId.toString()+"\"",  message.contains(invalidSectionId.toString()));
            assertTrue("\""+message+"\" does not contain input stream name \"nAmE\"", message.contains("nAmE"));
            assertTrue("\""+message+"\" does not contain line number "+51, message.contains(String.valueOf(51)));
            assertTrue("\""+message+"\" does not contain column number "+13, message.contains(String.valueOf(13)));
            assertTrue("\""+message+"\" does not contain \""+errorMustContain+"\"", message.contains(errorMustContain));
        }
    }

    @Test
    public void emptyNameTest() {
        assertInvalid("", "Empty");
    }

    @Test
    public void digitAtBeginningTest() {
        assertInvalid("7p", "The first character cannot be a digit: 7");
        assertInvalid("5",  "The first character cannot be a digit: 5");
    }

    @Test
    public void specialCharactersTest() {
        assertInvalid("aè", "Not in the english alphabet, not a digit and not \"_\": è");
        assertInvalid(";|", "Not in the english alphabet, not a digit and not \"_\": ;");
    }

    @Test
    public void javaKeywordTest() {
        assertInvalid("void",     "Java keyword");
        assertInvalid("class",    "Java keyword");
        assertInvalid("volatile", "Java keyword");
        assertInvalid("public",   "Java keyword");
        assertInvalid("false",    "Java keyword");
        assertInvalid("boolean",  "Java keyword");
    }

    @Test
    public void importedClassTest() {
        assertInvalid("Map",                       "Equal to the name of one of the imported classes");
        assertInvalid("HashMap",                   "Equal to the name of one of the imported classes");
        assertInvalid("Specificity",               "Equal to the name of one of the imported classes");
        assertInvalid("Sentence",                  "Equal to the name of one of the imported classes");
        assertInvalid("StandardRecognizerData",    "Equal to the name of one of the imported classes");
        assertInvalid("DiacriticsInsensitiveWord", "Equal to the name of one of the imported classes");
        assertInvalid("DiacriticsSensitiveWord",   "Equal to the name of one of the imported classes");
        assertInvalid("CapturingGroup",            "Equal to the name of one of the imported classes");
    }

    @Test
    public void validNameTest() throws CompilerError {
        checkValidVariableName("A");
        checkValidVariableName("j8");
        checkValidVariableName("java");
        checkValidVariableName("this_is_7th_MySectioniD");
        checkValidVariableName("afalse");
        checkValidVariableName("Int");
        checkValidVariableName("privatE_9");
        checkValidVariableName("interfaceUser");
    }
}
