package org.dicio.sentences_compiler.construct;

import org.dicio.sentences_compiler.util.CompilerError;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import static org.junit.Assert.assertThat;

public class SentenceTest {

    @Test
    public void testCompileToJava() throws IOException, CompilerError {
        final String sentenceId = "ID", word = "hello";

        final Sentence s = new Sentence();
        s.setSentenceId(sentenceId, "", 0);
        SentenceConstructList sentenceConstructList = new SentenceConstructList();
        sentenceConstructList.addConstruct(new Word(word, true));
        s.setSentenceConstructs(sentenceConstructList);

        final OutputStream outputStream = new ByteArrayOutputStream();
        final OutputStreamWriter output = new OutputStreamWriter(outputStream);
        s.compileWordList();
        s.compileToJava(output, "");
        output.close();

        final String code = outputStream.toString();
        assertThat(code, CoreMatchers.containsString("\"" + sentenceId + "\""));
        assertThat(code, CoreMatchers.containsString("\"" + word + "\""));
    }
}