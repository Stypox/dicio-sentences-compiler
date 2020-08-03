package org.dicio.sentences_compiler.main;

import com.beust.jcommander.Parameter;

import java.util.List;

public class Arguments {

    @Parameter(names = {"-h", "--help"}, description = "Shows this screen")
    public boolean help = false;

    @Parameter(names = {"-i", "--input"}, description = "An input file containing dicio-sentences-language code (stdin is accepted), and its charset, formatted like `--input CHARSET:FILE` (`CHARSET:` can be omitted, defaulting to utf8)", required = true)
    public List<String> inputFiles;

    @Parameter(names = {"-o", "--output"}, description = "The output file (stdout is accepted), and its charset, formatted like `--output CHARSET:FILE` (`CHARSET:` can be omitted, defaulting to utf8)", required = true)
    public String outputFile;

    @Parameter(names = {"-s", "--sections-file"}, description = "The file in which to output space-separated section ids (stdout is accepted), and its charset, formatted like `--output CHARSET:FILE` (`CHARSET:` can be omitted, defaulting to utf8)")
    public String sectionIdsFile = "";
}
