# Sentences compiler for Dicio assistant
This tool provides a simple way to generate sentences to be matched for the Dicio assistant. It compiles files formatted with the Dicio-sentences-language to Java code that can be easily imported in projects using the interpreter of the Dicio assistant. It allows to pack toghether similar sentences while preserving readability.

## Dicio sentences language
Every file contains many sections, starting with section information and followed by a list of sentences. The section information is formatted like `SECTION_ID:SPECIFICITY`, where SPECIFICITY can be `low`, `medium` and `high`. Every sentence is made of an optional sentence id (formatted like `[SENTENCE_ID]`) and a list of constructs followed by a `;`. Constructs can be:
- word (e.g. `hello`). A simple word: it can contain uppercase and lowercase unicode letters.
- or-red constructs (e.g. `hello|hi`). Any of the or-red construct could match. E.g. `hello|hi|hey assistant` matches `hello assistant`, `hi assistant` and `hey assistant`.
- optional construct (e.g. `hello?`). This construct can be skipped during parsing. E.g. `bye bye?` matches both `bye` and `bye bye`
- parenthesized construct (e.g. `(hello)`). This lets you pack constructs toghether and, for example, "or" them all. Just as math parenthesis do. E.g. `how (are you doing?)|(is it going)` matches `how are you`, `how are you doing` and `how is it going`.
- capturing group (`.NAME.`). This tells the interpreter to match a variable-length list of any word to that part of the sentence. NAME is the name of the capturing group. E.g. `how are you .person.` matches `how are you Tom` with `Tom` in the "person" capturing group.

## Build and run
To build the project open it in Android Studio (IntelliJ Idea probably works, too) and create an Application configuration in the "Run/Debug Configurations" menu, set "Main class" to `com.dicio.sentences_compiler.main.SentencesCompiler` and "Program arguments" to the arguments for the compiler. Then run the newly created configuration with the "Run" button. Set `--help` as "Program arguments" to get an help screen explaining the options.


## Example
The file below is [`example.dslf`](example.dslf). "dslf" means "Dicio-Sentences-Language File".
```
mood: high       # comments are supported :-D
how (are you doing?)|(is it going);
[has_place] how is it going over there;

GPS_navigation: medium
[question]  take|bring me to .place. (by .vehicle.)? please?;
[question]  give me directions to .place. please?;
[question]  how do|can i get to .place.;
[statement] i want to go to .place. (by .vehicle.)?;
[statement] .place. is the place i want to go to;
```
The above Dicio-sentences-language file is compiled to Java code by running the sentences-compiler as explained [above](#build-and-run), and setting the line below as "Program arguments".
```sh
--input example.dslf --output ClassName.java java --variable-prefix "section_" --package "com.pkg.name" --class "ClassName" 
```
After clicking on the "Run" button, the Java code shown below should be inside a file called ClassName.java in the root directory of the repository. Indentation was manually added to improve readability.
```java
package com.pkg.name;
import com.dicio.component.InputRecognizer;
import com.dicio.component.standard.Sentence;
import com.dicio.component.standard.StandardRecognizerData;
import com.dicio.component.standard.Word;
public class ClassName {
    public static final StandardRecognizerData section_mood = new StandardRecognizerData(
        InputRecognizer.Specificity.high,
        new Sentence("", new int[] {0,},
            new Word("how", false, 4, 1, 4), new Word("are", false, 3, 2), new Word("you", false, 2, 3, 7),
            new Word("doing", false, 1, 7), new Word("is", false, 3, 5), new Word("it", false, 2, 6),
            new Word("going", false, 1, 7)),
        new Sentence("has_place", new int[] {0,},
            new Word("how", false, 6, 1), new Word("is", false, 5, 2), new Word("it", false, 4, 3),
            new Word("going", false, 3, 4), new Word("over", false, 2, 5), new Word("there", false, 1, 6)));
    public static final StandardRecognizerData section_GPS_navigation = new StandardRecognizerData(
        InputRecognizer.Specificity.medium,
        new Sentence("question", new int[] {0, 1,},
            new Word("take", false, 9, 2), new Word("bring", false, 11, 2), new Word("me", false, 10, 3),
            new Word("to", false, 9, 4), new Word("place", true, 8, 5, 7, 8), new Word("by", false, 6, 6),
            new Word("vehicle", true, 5, 7, 8), new Word("please", false, 4, 8)),
        new Sentence("question", new int[] {0,},
            new Word("give", false, 7, 1), new Word("me", false, 6, 2), new Word("directions", false, 5, 3),
            new Word("to", false, 4, 4), new Word("place", true, 3, 5, 6), new Word("please", false, 1, 6)),
        new Sentence("question", new int[] {0,},
            new Word("how", false, 9, 1, 2), new Word("do", false, 6, 3), new Word("can", false, 8, 3),
            new Word("i", false, 7, 4), new Word("get", false, 6, 5), new Word("to", false, 5, 6),
            new Word("place", true, 4, 7)),
        new Sentence("statement", new int[] {0,},
            new Word("i", false, 10, 1), new Word("want", false, 9, 2), new Word("to", false, 8, 3),
            new Word("go", false, 7, 4), new Word("to", false, 6, 5), new Word("place", true, 5, 6, 8),
            new Word("by", false, 3, 7), new Word("vehicle", true, 2, 8)),
        new Sentence("statement", new int[] {0,},
            new Word("place", true, 10, 1), new Word("is", false, 8, 2), new Word("the", false, 7, 3),
            new Word("place", false, 6, 4), new Word("i", false, 5, 5), new Word("want", false, 4, 6),
            new Word("to", false, 3, 7), new Word("go", false, 2, 8), new Word("to", false, 1, 9)));
}
```
