package org.dicio.sentences_compiler.construct;

import org.dicio.sentences_compiler.compiler.Alternative;
import org.dicio.sentences_compiler.compiler.RepeatedList;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CapturingGroup extends WordBase {

    private final String name;

    /**
     * @param name the capturing group name, used for identification purposes
     */
    public CapturingGroup(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    @Override
    public void compileToJava(final OutputStreamWriter output,
                              final String variableName) throws IOException {
        output.write("new CapturingGroup(\"");
        output.write(name);
        output.write("\",");

        super.compileToJava(output, variableName);
        output.write(")");
    }

    @Override
    public Set<String> getCapturingGroupNames() {
        return Collections.singleton(name);
    }

    @Override
    public List<Alternative> buildAlternatives(
            final Map<String, RepeatedList> capturingGroupSubstitutions) {
        final String alternative = capturingGroupSubstitutions.get(name).get();
        return Collections.singletonList(
                new Alternative(alternative, Collections.singletonMap(name, alternative)));
    }
}
