package com.dicio.sentences_compiler.compiler;

import com.dicio.sentences_compiler.util.CompilerError;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import static org.junit.Assert.assertThat;

public class CompilerToJavaTest {

    @Test
    public void testReadmeExample() throws IOException, CompilerError {
        InputStream inputStream = new ByteArrayInputStream((
                "mood: high       # comments are supported :-D\n" +
                "how (are you doing?)|(is it going);\n" +
                "[has_place] how is it going over there;\n" +
                "\n" +
                "GPS_navigation: 2\n" +
                "[question]  take|bring me to .place. (by .vehicle.)? please?;\n" +
                "[question]  give me directions to .place. please?;\n" +
                "[question]  how do|can i get to .place.;\n" +
                "[statement] i want to go to .place. (by .vehicle.)?;\n" +
                "[statement] .place. is the place i want to go to;\n").getBytes("unicode"));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        CompilerToJava compilerToJava = new CompilerToJava("section_", "com.hello.world", "MyClass");
        compilerToJava.addInputStream(new InputStreamReader(inputStream, Charset.forName("unicode")), "myInput");
        compilerToJava.compile(new OutputStreamWriter(outputStream, Charset.forName("unicode")));
        outputStream.close();

        String code = outputStream.toString("unicode");
        assertThat(code, CoreMatchers.containsString("StandardRecognizerData section_mood"));
        assertThat(code, CoreMatchers.containsString("StandardRecognizerData section_GPS_navigation"));
        assertThat(code, CoreMatchers.containsString("package com.hello.world"));
        assertThat(code, CoreMatchers.containsString("class MyClass"));
    }
}