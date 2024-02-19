package org.dicio.sentences_compiler.compiler;

import org.dicio.sentences_compiler.construct.Section;
import org.dicio.sentences_compiler.util.CompilerError;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

public class CompilerToDataset extends CompilerBase {

    @Override
    public void compile(final OutputStreamWriter output, final OutputStreamWriter sectionIdsOutput)
            throws IOException, CompilerError {
        super.compile(output, sectionIdsOutput);

        final Map<String, JSONObject> results = new HashMap<>();
        for (final Section section : getSections()) {
            results.putAll(section.compileToDataset());
        }

        System.out.println("HERE" + results);
        new JSONObject(results)
                .write(output).close();
    }
}
