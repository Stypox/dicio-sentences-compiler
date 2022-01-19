package org.dicio.sentences_compiler.lexer;

import org.dicio.sentences_compiler.util.CompilerError;

import java.io.IOException;
import java.io.InputStreamReader;

public class Tokenizer {
    private final TokenStream ts;
    private InputStreamReader input;
    private int line, column;
    private String ch, prevCh;

    public Tokenizer() {
        ts = new TokenStream();
    }

    private void setInputStream(final InputStreamReader input) {
        this.input = input;
        line = 1;
        column = 0;
        ch = "";
        prevCh = "";
    }

    private void getCh() throws IOException {
        prevCh = ch;

        int intCh = input.read();
        if (intCh == -1) {
            ch = "";
        } else {
            ++column;
            ch = new String(Character.toChars(intCh));
        }
    }

    private Token.Type updateWordType(final Token.Type currentWordType, final String ch) {
        if (currentWordType == Token.Type.lettersPlusOther || isOtherValid(ch)) {
            return Token.Type.lettersPlusOther;
        } else {
            return Token.Type.letters;
        }
    }


    private boolean isSpace(String ch) {
        return !ch.isEmpty() && !ch.equals("\n") && Character.isWhitespace(ch.codePointAt(0));
    }

    private boolean isLetter(String ch) {
        return !ch.isEmpty() && Character.isLetter(ch.codePointAt(0));
    }

    private boolean isOtherValid(String ch) {
        return !ch.isEmpty() && (Character.isDigit(ch.codePointAt(0)) || ch.equals("_"));
    }

    private boolean isGrammar(String ch) {
        switch (ch) {
            case ":": case ";": case "|": case "?": case ".":
            case "(": case ")": case "[": case "]": case "\"":
                return true;
            default:
                return false;
        }
    }


    public void tokenize(final InputStreamReader inputStreamReader,
                         final String inputStreamName) throws IOException, CompilerError {
        try {
            setInputStream(inputStreamReader);
            getCh();

            while (!ch.isEmpty()) {
                if (isLetter(ch) || isOtherValid(ch)) {
                    StringBuilder word = new StringBuilder(ch);
                    Token.Type wordType = Token.Type.letters;
                    int firstCharCol = column;

                    while (true) {
                        wordType = updateWordType(wordType, ch);
                        getCh();
                        if (!ch.isEmpty() && (isLetter(ch) || isOtherValid(ch))) {
                            word.append(ch);
                        } else {
                            break;
                        }
                    }

                    ts.push(new Token(
                            wordType, word.toString(), inputStreamName, line, firstCharCol));

                } else if (isGrammar(ch)) {
                    ts.push(new Token(Token.Type.grammar, ch, inputStreamName, line, column));
                    getCh();

                } else if (ch.equals("<")) {
                    final String tokenValue;
                    if (isLetter(prevCh) || prevCh.equals(">")) {
                        tokenValue = "<"; // connected to the word before it
                    } else {
                        tokenValue = " <"; // not connected to a previous word
                    }
                    ts.push(new Token(
                            Token.Type.grammar, tokenValue, inputStreamName, line, column));
                    getCh();

                } else if (ch.equals(">")) {
                    final int originalColumn = column;
                    getCh();
                    final String tokenValue;
                    if (isLetter(ch) || ch.equals("<")) {
                        tokenValue = ">"; // connected to the word after it
                    } else {
                        tokenValue = "> "; // not connected to a next word
                    }
                    ts.push(new Token(
                            Token.Type.grammar, tokenValue, inputStreamName, line, originalColumn));

                } else if (ch.equals("\n")) {
                    ++line;
                    column = 0;
                    getCh();

                } else if (ch.equals("#")) {
                    while (!ch.equals("\n") && !ch.isEmpty()) {
                        getCh();
                    }

                } else if (isSpace(ch)) {
                    getCh();

                } else {
                    throw new CompilerError(CompilerError.Type.invalidCharacter, ch,
                            inputStreamName, line, column, "");
                }
            }

            ts.push(new Token(Token.Type.endOfFile, "", inputStreamName, line, column+1));

        } finally {
            // allow garbage collector to kick in
            this.input = null;
            ch = null;
            prevCh = null;
        }
    }

    public TokenStream getTokenStream() {
        return ts;
    }
}
