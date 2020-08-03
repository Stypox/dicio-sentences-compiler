package org.dicio.sentences_compiler.util;

import com.beust.jcommander.ParameterException;

import org.dicio.sentences_compiler.lexer.Token;

import javax.lang.model.SourceVersion;

public class JavaSyntaxCheck {

    private static final int minNum = "0".codePointAt(0);
    private static final int maxNum = "9".codePointAt(0);
    private static final int minLow = "a".codePointAt(0);
    private static final int maxLow = "z".codePointAt(0);
    private static final int minUp = "A".codePointAt(0);
    private static final int maxUp = "Z".codePointAt(0);
    private static final int underscore = "_".codePointAt(0);

    private JavaSyntaxCheck() {
    }

    /**
     * @throws ParameterException if parameter {@code name} is not a valid java variable name
     */
    public static void checkValidJavaVariableName(final String name, final String parameterName)
            throws ParameterException {
        final String errorMessage = getErrorMessageForJavaVariableName(name);
        if (errorMessage != null) {
            throw new ParameterException("Value \"" + name + "\" for parameter " + parameterName
                    + " is not a valid Java variable name: " + errorMessage);
        }
    }

    /**
     * @throws CompilerError if parameter {@code name} is not a valid java variable name
     */
    public static void checkValidJavaVariableName(final String name,
                                                  final Token errorToken,
                                                  final CompilerError.Type type)
            throws CompilerError {
        final String errorMessage = getErrorMessageForJavaVariableName(name);
        if (errorMessage != null) {
            throw new CompilerError(type, errorToken, errorMessage);
        }
    }

    private static String getErrorMessageForJavaVariableName(final String name) {

        if (name.length() == 0) {
            return "Empty";
        }

        if (Character.isDigit(name.codePointAt(0))) {
            return "The first character cannot be a digit: "
                    + Character.toChars(name.codePointAt(0))[0];
        }

        for (int i = 0; i < name.codePointCount(0, name.length()); ++i) {
            final int val = name.codePointAt(i);
            if (!((minNum <= val && val <= maxNum) ||
                    (minLow <= val && val <= maxLow) ||
                    (minUp <= val && val <= maxUp) ||
                    (val == underscore))) {
                return "Not in the english alphabet, not a digit and not \"_\": "
                        + Character.toChars(val)[0];
            }
        }

        if (SourceVersion.isKeyword(name)) {
            return "Java keyword";
        }
        return null;
    }
}
