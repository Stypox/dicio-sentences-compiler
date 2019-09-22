package com.dicio.sentences_compiler.parser;

import com.dicio.sentences_compiler.lexer.Token;
import com.dicio.sentences_compiler.construct.CapturingGroup;
import com.dicio.sentences_compiler.construct.ConstructOptional;
import com.dicio.sentences_compiler.construct.OrList;
import com.dicio.sentences_compiler.construct.Sentence;
import com.dicio.sentences_compiler.construct.SentenceConstructList;
import com.dicio.sentences_compiler.construct.Word;
import com.dicio.sentences_compiler.util.CompilerError;
import com.dicio.sentences_compiler.lexer.TokenStream;
import com.dicio.sentences_compiler.lexer.Tokenizer;
import com.dicio.sentences_compiler.construct.Section;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class Parser {
    private TokenStream ts;

    public Parser(TokenStream tokenStream) {
        this.ts = tokenStream;
    }

    public ArrayList<Section> parse() throws CompilerError {
        ArrayList<Section> sections = new ArrayList<>();
        while (true) {
            Section section = readSection();
            if (section == null) {
                if (ts.get(0).isType(Token.Type.endOfFile)) {
                    ts.movePositionForwardBy(1);
                } else {
                    throw new CompilerError(CompilerError.Type.expectedSectionOrEndOfFile, ts.get(0), "");
                }

                if (ts.get(0).isEmpty()) {
                    break;
                }
            } else {
                sections.add(section);
            }
        }

        validate(sections);
        return sections;
    }


    private Section readSection() throws CompilerError {
        Section section = new Section();

        String sectionId = readSectionId();
        if (sectionId == null) {
            return null;
        }
        String inputStreamName = ts.get(-2).getInputStreamName();
        int sectionIdLine = ts.get(-2).getLine();

        Section.Specificity specificity = readSpecificity();
        section.setSectionInfo(sectionId, specificity, inputStreamName, sectionIdLine);

        boolean foundSentences = false;
        while (true) {
            Sentence sentence = readSentence();
            if (sentence == null) {
                break;
            }

            foundSentences = true;
            section.addSentence(sentence);
        }

        if (!foundSentences) {
            throw new CompilerError(CompilerError.Type.expectedSentence, ts.get(0), "");
        }
        return section;
    }

    private String readSectionId() throws CompilerError {
        if (ts.get(0).isType(Token.Type.lettersPlusOther)) {
            if (ts.get(1).equals(Token.Type.grammar, ":")) {
                String sectionId = ts.get(0).getValue();
                if (Character.isDigit(sectionId.codePointAt(0))) {
                    throw new CompilerError(CompilerError.Type.invalidSectionId, ts.get(0),
                            "The first character cannot be a digit: " + Character.toChars(sectionId.codePointAt(0))[0]);
                }

                for (int i = 0; i < sectionId.codePointCount(0, sectionId.length()); ++i) {
                    final int val = sectionId.codePointAt(i),
                            minNum = "0".codePointAt(0), maxNum = "9".codePointAt(0),
                            minLow = "a".codePointAt(0), maxLow = "z".codePointAt(0),
                            minUp = "A".codePointAt(0), maxUp = "Z".codePointAt(0);
                    if (!((minNum <= val && val <= maxNum) ||
                            (minLow <= val && val <= maxLow) ||
                            (minUp <= val && val <= maxUp) ||
                            (val == "_".codePointAt(0)))) {
                        throw new CompilerError(CompilerError.Type.invalidSectionId, ts.get(0),
                                "Not in the english alphabet, not a number and not \"_\": " + Character.toChars(val)[0]);
                    }
                }

                ts.movePositionForwardBy(2);
                return sectionId;
            } else {
                throw new CompilerError(CompilerError.Type.invalidToken, ts.get(1), "Expected \":\" after section id");
            }
        } else {
            return null;
        }
    }

    private Section.Specificity readSpecificity() throws CompilerError {
        if (ts.get(0).isType(Token.Type.lettersPlusOther)) {
            String specificityStr = ts.get(0).getValue();
            Section.Specificity specificity;

            switch (specificityStr) {
                case "1": case "low":
                    specificity = Section.Specificity.low;
                    break;
                case "2": case "medium":
                    specificity = Section.Specificity.medium;
                    break;
                case "3": case "high":
                    specificity = Section.Specificity.high;
                    break;
                default:
                    throw new CompilerError(CompilerError.Type.invalidSpecificity, ts.get(0), "Accepted values are 1/\"low\" 2/\"medium\" 3/\"high\"");
            }

            ts.movePositionForwardBy(1);
            return specificity;
        } else {
            throw new CompilerError(CompilerError.Type.invalidToken, ts.get(0), "Expected specificity after section id");
        }
    }

    private Sentence readSentence() throws CompilerError {
        Sentence sentence = new Sentence();

        String sentenceId = readSentenceId();
        boolean foundId = (sentenceId != null);
        if (foundId) {
            sentence.setSentenceId(sentenceId, ts.get(-2).getInputStreamName(), ts.get(-2).getLine());
        } else {
            sentence.setSentenceId("", ts.get(0).getInputStreamName(), ts.get(0).getLine());
        }

        SentenceConstructList sentenceContent = readSentenceContent();
        if (sentenceContent == null) {
            if (foundId) {
                throw new CompilerError(CompilerError.Type.expectedSentenceContent, ts.get(0), "");
            } else {
                return null;
            }
        }

        if (ts.get(0).equals(Token.Type.grammar, ";")) {
            sentence.setSentenceConstructs(sentenceContent);
            ts.movePositionForwardBy(1);
            return sentence;
        } else {
            throw new CompilerError(CompilerError.Type.invalidToken, ts.get(0), "Expected \";\" at the end of sentence");
        }
    }

    private String readSentenceId() throws CompilerError {
        if (ts.get(0).equals(Token.Type.grammar, "[")) {
            if (ts.get(1).isType(Token.Type.lettersPlusOther)) {
                if (ts.get(2).equals(Token.Type.grammar, "]")) {
                    String sentenceId = ts.get(1).getValue();
                    ts.movePositionForwardBy(3);
                    return sentenceId;
                } else {
                    throw new CompilerError(CompilerError.Type.invalidToken, ts.get(2), "Expected \"]\" after sentence id");
                }
            } else {
                throw new CompilerError(CompilerError.Type.invalidToken, ts.get(1), "Expected sentence id after token \"[\"");
            }
        } else {
            return null;
        }
    }

    private SentenceConstructList readSentenceContent() throws CompilerError {
        if (ts.get(0).isType(Token.Type.lettersPlusOther) && ts.get(1).equals(Token.Type.grammar, ":")) {
            // found section id, skip to next section
            return null;
        }

        return readSentenceConstructList(true);
    }

    private SentenceConstructList readSentenceConstructList(boolean capturingGroupsAllowed) throws CompilerError {
        SentenceConstructList sentenceConstructList = new SentenceConstructList();

        boolean foundSentenceConstruct = false;
        while (true) {
            UnfoldableConstruct sentenceConstruct = null;
            OrList orList = readOrList();
            if (orList == null) {
                if (capturingGroupsAllowed) {
                    sentenceConstruct = readCapturingGroup();
                }
            } else {
                sentenceConstruct = orList.shrink();
            }

            if (sentenceConstruct == null) {
                break;
            } else {
                foundSentenceConstruct = true;
                sentenceConstructList.addConstruct(sentenceConstruct);
            }
        }

        if (foundSentenceConstruct) {
            return sentenceConstructList;
        } else {
            return null;
        }
    }

    private OrList readOrList() throws CompilerError {
        OrList orList = new OrList();

        boolean foundSentenceConstruct = false;
        while (true) {
            UnfoldableConstruct sentenceConstruct;
            sentenceConstruct = readWord();
            if (sentenceConstruct == null) {
                sentenceConstruct = readSentenceConstructListInsideParenthesis();
            }

            if (sentenceConstruct == null) {
                if (foundSentenceConstruct) { // there is a | at the end of the OrList
                    if (ts.get(0).equals(Token.Type.grammar, ".")) {
                        throw new CompilerError(CompilerError.Type.optionalCapturingGroup, ts.get(0), "");
                    } else {
                        throw new CompilerError(CompilerError.Type.invalidToken, ts.get(0), "Expected sentence construct after \"|\" token");
                    }
                } else {
                    break;
                }
            } else {
                foundSentenceConstruct = true;
                orList.addConstruct(sentenceConstruct);
            }

            if (ts.get(0).equals(Token.Type.grammar, "|")) {
                ts.movePositionForwardBy(1);
            } else if (ts.get(0).equals(Token.Type.grammar, "?")) {
                ts.movePositionForwardBy(1);
                orList.addConstruct(new ConstructOptional());
                break;
            } else {
                break;
            }
        }

        if (foundSentenceConstruct) {
            return orList;
        } else {
            return null;
        }
    }

    private Word readWord() {
        if (ts.get(0).isType(Token.Type.letters)) {
            Word word = new Word(ts.get(0).getValue().toLowerCase());
            ts.movePositionForwardBy(1);
            return word;
        } else {
            return null;
        }
    }

    private SentenceConstructList readSentenceConstructListInsideParenthesis() throws CompilerError {
        if (ts.get(0).equals(Token.Type.grammar, "(")) {
            ts.movePositionForwardBy(1);

            SentenceConstructList sentenceConstructList = readSentenceConstructList(false);
            if (sentenceConstructList == null) {
                if (ts.get(0).equals(Token.Type.grammar, ".")) {
                    throw new CompilerError(CompilerError.Type.capturingGroupInsideParenthesis, ts.get(0), "");
                } else {
                    throw new CompilerError(CompilerError.Type.expectedSentenceConstructList, ts.get(0), "");
                }
            } else {
                if (ts.get(0).equals(Token.Type.grammar, ")")) {
                    ts.movePositionForwardBy(1);
                    return sentenceConstructList;
                } else {
                    throw new CompilerError(CompilerError.Type.invalidToken, ts.get(0), "Expected \")\" after list of sentence constructs");
                }
            }
        } else {
            return null;
        }
    }

    private CapturingGroup readCapturingGroup() throws CompilerError {
        if (ts.get(0).equals(Token.Type.grammar, ".")) {
            ts.movePositionForwardBy(1);
            if (ts.get(0).equals(Token.Type.grammar, ".")) {
                ts.movePositionForwardBy(1);
                if (ts.get(0).equals(Token.Type.grammar, ".")) {
                    // this also prevents two subsequent capturing groups
                    throw new CompilerError(CompilerError.Type.capturingGroupInvalidLength, ts.get(0), "Found more than two points \".\"");
                } else if (ts.get(0).equals(Token.Type.grammar, "?") || ts.get(0).equals(Token.Type.grammar, "|")) {
                    throw new CompilerError(CompilerError.Type.optionalCapturingGroup, ts.get(0), "");
                }
                return new CapturingGroup();
            } else {
                throw new CompilerError(CompilerError.Type.capturingGroupInvalidLength, ts.get(0), "Found only one point \".\"");
            }
        } else {
            return null;
        }
    }


    private void validate(ArrayList<Section> sections) throws CompilerError {
        ArrayList<String> sectionIds = new ArrayList<>();
        for(Section section : sections) {
            section.validate();

            String sectionId = section.getSectionId();
            if (sectionIds.contains(sectionId)) {
                throw new CompilerError(CompilerError.Type.duplicateSectionId, sectionId, section.getInputStreamName(), section.getLine(), "");
            } else {
                sectionIds.add(sectionId);
            }
        }
    }
}
