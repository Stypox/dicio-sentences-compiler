package org.dicio.sentences_compiler.compiler;

import com.beust.jcommander.ParameterException;

import org.dicio.sentences_compiler.util.CompilerError;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class CompilerToJavaTest {

    @Test
    public void testReadmeExample() throws IOException, CompilerError {
        final InputStream inputStream = new ByteArrayInputStream((
                "mood: high       # comments are supported :-D\n"
                + "how (are you doing?)|(is it going);\n"
                + "[has_place] how is it going over there;\n"
                + "[french] comment \"êtes\" vous;  # quotes make sure êtes is matched diacritics-sensitively\n"
                + "\n"
                + "GPS_navigation: medium\n"
                + "[question]  take|bring me to .place. (by .vehicle.)? please?;\n"
                + "[question]  give me directions to .place. please?;\n"
                + "[question]  how do|can i get to .place.;\n"
                + "[statement] i want to go to .place. (by .vehicle.)?;\n"
                + "[statement] .place. is the place i want to go to;\n")
                .getBytes(StandardCharsets.UTF_8));
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final ByteArrayOutputStream sectionIdsStream = new ByteArrayOutputStream();

        final CompilerToJava compilerToJava = new CompilerToJava(
                "section_", "com.hello.world", "MyClass", "sections");
        compilerToJava.addInputStream(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8), "myInput");
        compilerToJava.compile(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8),
                new OutputStreamWriter(sectionIdsStream, StandardCharsets.UTF_8));
        outputStream.close();
        sectionIdsStream.close();

        final String code = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
        assertThat(code, containsString("package com.hello.world"));
        assertThat(code, containsString("class MyClass"));
        assertThat(code, containsString("StandardRecognizerData section_mood"));
        assertThat(code, containsString("public static final class SectionClass_section_GPS_navigation extends StandardRecognizerData"));
        assertThat(code, containsString("SectionClass_section_GPS_navigation section_GPS_navigation"));
        assertThat(code, containsString("Specificity.high"));
        assertThat(code, containsString("Specificity.medium"));
        assertThat(code, not(containsString("low")));
        assertThat(code, not(containsString(",}")));
        assertThat(code, not(containsString(", }")));

        assertThat(code, containsString("new DiacriticsSensitiveWord(\""));
        assertThat(code, containsString("new DiacriticsSensitiveWord(\"êtes\","));
        assertThat(code, containsString("new DiacriticsInsensitiveWord(new byte[]"));
        assertThat(code, containsString("new DiacriticsInsensitiveWord(new byte[]{0,91,0,98,0,106,0,0,0,0},"));
        assertThat(code, not(containsString("new DiacriticsInsensitiveWord(\"")));
        assertThat(code, not(containsString("new DiacriticsSensitiveWord(new")));
        assertThat(code, not(containsString("how")));
        assertThat(code, not(containsString("get")));

        assertThat(code, not(containsString("comments are supported")));
        assertThat(code, not(containsString("quotes")));

        assertEquals("mood GPS_navigation",
                new String(sectionIdsStream.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    public void checkAcceptsEmptyParameters() {
        new CompilerToJava("", "a.b.c", "MyClass", "");
    }

    @Test(expected = ParameterException.class)
    public void crashInvalidVariablePrefix() {
        new CompilerToJava("9abc", "a.b.c", "MyClass", "ha");
    }

    @Test(expected = ParameterException.class)
    public void crashInvalidClassName() {
        new CompilerToJava("ha", "a.b.c", "è", "ha");
    }

    @Test(expected = ParameterException.class)
    public void crashInvalidSectionMapName() {
        new CompilerToJava("ha", "a.b.c", "MyClass", "class");
    }
}