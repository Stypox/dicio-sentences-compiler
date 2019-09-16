# Sentences compiler for Dicio assistant
This tool provides a simple way to generate sentences to be matched for the Dicio assistant. It compiles files formatted with the Dicio-sentences-language to Java code that can be easily imported in projects using the interpreter of the Dicio assistant. It allows to pack toghether similar sentences while preserving readability.

## Dicio sentences language
Every file contains many sections, starting with section information and followed by a list of sentences. The section information is formatted like `SECTION_ID:SPECIFICITY`, where SPECIFICITY can be `low`, `1`, `medium`, `2`, `high`, `3`. Every sentence is made of an optional sentence id (formatted like `[SENTENCE_ID]`) and a list of constructs followed by a `;`. Constructs can be:
- word (e.g. `hello`). A simple word: it can contain uppercase and lowercase unicode letters.
- or-red constructs (e.g. `hello|hi`). Every time the compiler encounters n or-red constructs, it will generate n sentence variants: the first one contains the first or-red construct, ..., the nth one contains the nth or-red construct. E.g. `hello|hi|hey assistant` generates `hello assistant`, `hi assistant` and `hey assistant`.
- optional construct (e.g. `hello?`). This tells the compiler to generate 2 sentence variants: one with the construct and another without it. E.g. `hello? assistant` generates `hello assistant` and `assistant`
- parenthesized construct (e.g. `(hello)`). This lets you pack constructs toghether and, for example, "or" them all. Just as math parenthesis do. E.g. `how (are you doing?)|(is it going)` generates `how are you`, `how are you doing` and `how is it going`.
- capturing group (`..`). This tells the interpreter to match a variable-length list of any word to that part of the sentence. They cannot be or-red or made optional since it would hurt readability. The maximum number of capturing groups supported by the interpreter is 2. E.g. `how are you ..` matches `how are you Tom`.

## Build and run
To build the project open it in Android Studio (IntelliJ Idea probably works, too) and create an Application configuration in the "Run/Debug Configurations" menu, set "Main class" to `com.dicio.sentences_compiler.main.SentencesCompiler` and "Program arguments" to the arguments for the compiler. Then run the newly created configuration with the "Run" button. Set `--help` as "Program arguments" to get an help screen explaining the options.


## Example
The file below is [`example.dslf`](example.dslf). "dslf" means "Dicio-Sentences-Language File".
```
mood: high       # comments are supported :-D
how (are you doing?)|(is it going);
[has_place] how is it going over there;

GPS_navigation: 2
[question]  take|bring me to .. please?;
[question]  give me directions to .. please?;
[question]  how do|can i get to ..;
[statement] i want to go to ..;
[statement] .. is the place i want to go to;
[vehicle]   take|bring me to .. by .. please?;
[vehicle]   i want to go to .. by ..;
```
The above Dicio-sentences-language file is compiled to Java code by running the sentences-compiler as explained [above](#build-and-run), and setting the line below as "Program arguments".
```sh
--input example.dslf --output ClassName.java java --variable-prefix "section_" --package "com.pkg.name" --class "ClassName" 
```
After clicking on the "Run" button, the Java code shown below should be inside a file called ClassName.java in the root directory of the repository. Indentation was manually added to improve readability.
```java
package com.pkg.name;
import com.dicio.component.input.standard.Sentence;
import com.dicio.component.input.standard.StandardRecognizer;
import com.dicio.component.input.InputRecognizer;
public class ClassName {
    final StandardRecognizer section_mood = new StandardRecognizer(
        InputRecognizer.Specificity.high,
        new Sentence[]{
            new Sentence("", new String[]{"how","are","you","doing",}),
            new Sentence("", new String[]{"how","are","you",}),
            new Sentence("", new String[]{"how","is","it","going",}),
            new Sentence("has_place", new String[]{"how","is","it","going","over","there",}),
        }
    );
    final StandardRecognizer section_GPS_navigation = new StandardRecognizer(
        InputRecognizer.Specificity.medium,
        new Sentence[]{
            new Sentence("question", new String[]{"take","me","to",}, new String[]{"please",}),
            new Sentence("question", new String[]{"bring","me","to",}, new String[]{"please",}),
            new Sentence("question", new String[]{"take","me","to",}, new String[]{}),
            new Sentence("question", new String[]{"bring","me","to",}, new String[]{}),
            new Sentence("question", new String[]{"give","me","directions","to",}, new String[]{"please",}),
            new Sentence("question", new String[]{"give","me","directions","to",}, new String[]{}),
            new Sentence("question", new String[]{"how","do","i","get","to",}, new String[]{}),
            new Sentence("question", new String[]{"how","can","i","get","to",}, new String[]{}),
            new Sentence("statement", new String[]{"i","want","to","go","to",}, new String[]{}),
            new Sentence("statement", new String[]{}, new String[]{"is","the","place","i","want","to","go","to",}),
            new Sentence("vehicle", new String[]{"take","me","to",}, new String[]{"by",}, new String[]{"please",}),
            new Sentence("vehicle", new String[]{"bring","me","to",}, new String[]{"by",}, new String[]{"please",}),
            new Sentence("vehicle", new String[]{"take","me","to",}, new String[]{"by",}, new String[]{}),
            new Sentence("vehicle", new String[]{"bring","me","to",}, new String[]{"by",}, new String[]{}),
            new Sentence("vehicle", new String[]{"i","want","to","go","to",}, new String[]{"by",}, new String[]{}),
        }
    );
}
```
