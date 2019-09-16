package com.dicio.sentences_compiler.construct;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import static org.junit.Assert.*;

public class SentenceTest {

    @Test
    public void testCompileToJava() throws IOException {
        String sentenceId = "ID", word = "hello";

        Sentence s = new Sentence();
        s.setSentenceId(sentenceId, "", 0);
        SentenceConstructList sentenceConstructList = new SentenceConstructList();
        sentenceConstructList.addConstruct(new Word("hello"));
        s.setSentenceConstructs(sentenceConstructList);

        OutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter output = new OutputStreamWriter(outputStream);
        s.compileToJava(output, "");
        output.close();

        String code = outputStream.toString();
        assertThat(code, CoreMatchers.containsString("\"" + sentenceId + "\""));
        assertThat(code, CoreMatchers.containsString("\"" + word + "\""));
    }
}