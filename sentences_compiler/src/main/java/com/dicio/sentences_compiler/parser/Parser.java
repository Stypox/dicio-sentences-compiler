package com.dicio.sentences_compiler.parser;

import com.dicio.sentences_compiler.construct.Construct;
import com.dicio.sentences_compiler.construct.OptionalConstruct;
import com.dicio.sentences_compiler.construct.OrList;
import com.dicio.sentences_compiler.construct.Section;
import com.dicio.sentences_compiler.construct.Sentence;
import com.dicio.sentences_compiler.construct.SentenceConstructList;
import com.dicio.sentences_compiler.construct.Word;
import com.dicio.sentences_compiler.lexer.Token;
import com.dicio.sentences_compiler.lexer.TokenStream;
import com.dicio.sentences_compiler.util.CompilerError;
import com.dicio.sentences_compiler.util.JavaSyntaxCheck;

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
                final String sectionId = ts.get(0).getValue();
                JavaSyntaxCheck.checkValidJavaVariableName(sectionId, ts.get(0));
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
                case "low":
                    specificity = Section.Specificity.low;
                    break;
                case "medium":
                    specificity = Section.Specificity.medium;
                    break;
                case "high":
                    specificity = Section.Specificity.high;
                    break;
                default:
                    throw new CompilerError(CompilerError.Type.invalidSpecificity, ts.get(0), "Accepted values are \"low\", \"medium\" and \"high\"");
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

        return readSentenceConstructList();
    }

    private SentenceConstructList readSentenceConstructList() throws CompilerError {
        SentenceConstructList sentenceConstructList = new SentenceConstructList();

        boolean foundSentenceConstruct = false;
        while (true) {
            final OrList orList = readOrList();

            if (orList == null) {
                break;
            } else {
                foundSentenceConstruct = true;
                sentenceConstructList.addConstruct(orList.shrink());
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
            Construct sentenceConstruct;
            sentenceConstruct = readWord();
            if (sentenceConstruct == null) {
                sentenceConstruct = readCapturingGroup();
            }
            if (sentenceConstruct == null) {
                sentenceConstruct = readSentenceConstructListInsideParenthesis();
            }

            if (sentenceConstruct == null) {
                if (foundSentenceConstruct) { // there is a | at the end of the OrList
                    throw new CompilerError(CompilerError.Type.invalidToken, ts.get(0), "Expected sentence construct after \"|\" token");
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
                orList.addConstruct(new OptionalConstruct());
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
            Word word = new Word(ts.get(0).getValue().toLowerCase(), false);
            ts.movePositionForwardBy(1);
            return word;
        } else {
            return null;
        }
    }

    private SentenceConstructList readSentenceConstructListInsideParenthesis() throws CompilerError {
        if (ts.get(0).equals(Token.Type.grammar, "(")) {
            ts.movePositionForwardBy(1);

            SentenceConstructList sentenceConstructList = readSentenceConstructList();
            if (sentenceConstructList == null) {
                throw new CompilerError(CompilerError.Type.expectedSentenceConstructList, ts.get(0), "");
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

    private Word readCapturingGroup() throws CompilerError {
        if (ts.get(0).equals(Token.Type.grammar, ".")) {
            ts.movePositionForwardBy(1);
            if (ts.get(0).isType(Token.Type.lettersPlusOther)) {
                final String capturingGroupName = ts.get(0).getValue();
                if (!ts.get(1).equals(Token.Type.grammar, ".")) {
                    throw new CompilerError(CompilerError.Type.expectedPoint, ts.get(1), "");
                }

                ts.movePositionForwardBy(2);
                return new Word(capturingGroupName, true);

            } else {
                throw new CompilerError(CompilerError.Type.expectedCapturingGroupName, ts.get(0), "");
            }
        } else {
            return null;
        }
    }


    private void validate(ArrayList<Section> sections) throws CompilerError {
        ArrayList<String> sectionIds = new ArrayList<>();
        for(Section section : sections) {
            section.compileSentenceWordLists();

            String sectionId = section.getSectionId();
            if (sectionIds.contains(sectionId)) {
                throw new CompilerError(CompilerError.Type.duplicateSectionId, sectionId, section.getInputStreamName(), section.getLine(), "");
            } else {
                sectionIds.add(sectionId);
            }
        }
    }
}
