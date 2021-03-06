package org.dicio.sentences_compiler.main;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandNames = {"java"}, commandDescription = "Compiler to Java: compiles every section to a org.dicio.skill.input.standard.StandardRecognizerData (from library dicio-skill)")
public class JavaCommand {
    @Parameter(names = {"--variable-prefix"}, description = "The prefix for the name of every StandardRecognizerData instance, that will be followed by the name of the corresponding section.")
    public String variablePrefix = "";

    @Parameter(names = {"--package"}, description = "The name of the Java package for the output file", required = true)
    public String packageName;

    @Parameter(names = {"--class"}, description = "The name of the Java class for the output file", required = true)
    public String className;

    @Parameter(names = {"--create-section-map"}, description = "Creates a map with the provided name, from section names to StandardRecognizerData instances")
    public String sectionMapName = "";
}