# Sentences compiler for Dicio assistant
This tool provides a simple way to generate sentences to be matched for the Dicio assistant. It compiles files formatted with the Dicio-sentences-language to Java code that can be easily imported in projects using the interpreter of the Dicio assistant. It allows to pack together similar sentences while preserving readability.

## Dicio sentences language
Every file contains many sections, starting with section information and followed by a list of sentences. The section information is formatted like `SECTION_ID:SPECIFICITY`, where SPECIFICITY can be `low`, `medium` and `high`. Every sentence is made of an optional sentence id (formatted like `[SENTENCE_ID]`) and a list of constructs followed by a `;`. Constructs can be:
- word (e.g. `hello`). A simple word: it can contain uppercase and lowercase unicode letters.
- or-red constructs (e.g. `hello|hi`). Any of the or-red construct could match. E.g. `hello|hi|hey assistant` matches `hello assistant`, `hi assistant` and `hey assistant`.
- optional construct (e.g. `hello?`). This construct can be skipped during parsing. E.g. `bye bye?` matches both `bye` and `bye bye`
- parenthesized construct (e.g. `(hello)`). This lets you pack constructs toghether and, for example, "or" them all. Just as math parenthesis do. E.g. `how (are you doing?)|(is it going)` matches `how are you`, `how are you doing` and `how is it going`.
- capturing group (`.NAME.`). This tells the interpreter to match a variable-length list of any word to that part of the sentence. NAME is the name of the capturing group. E.g. `how are you .person.` matches `how are you Tom` with `Tom` in the "person" capturing group.

## Compilation process
When issuing a compilation, `dicio-sentences-compiler` will first parse the provided file and build a syntax tree. Then every sentence is analyzed and converted to a format which allows running a `O(number of words)` depth-first search on it with as little runtime overhead as possible. Every word in the sentence is assigned a unique index, a list of indices of all words that could come next, and the minimum number of words to skip to get to the end of the sentence. The index is used *(you guessed it!)* just for indexing. The list of next word indices is needed to instantly determine the possible next words during a depth-first search. The number of words to get to the end allows lowering the score accordingly while doing the search, without having to recalculate it at runtime. When a section is put together, besides the list of compiled and analyzed sentences, it has the [specificity value](https://github.com/Stypox/dicio-assistance-component/#input-recognizer) and (if applicable) the list of all capturing group names, to allow compiling them to language variables, for convenience's sake and to prevent typos, much like with Android's `R` class.

### Java
The compilation to Java relies on the [`dicio-assistance-component`](https://github.com/Stypox/dicio-assistance-component) library, so sections will be compiled in this format:
```java
StandardRecognizerData SECTION_NAME = new StandardRecognizerData(
        InputRecognizer.Specificity.SPECIFICITY,
        new Sentence(SENTENCE_ID, LIST_OF_STARTING_WORD_INDICES,
                new Word(VALUE, IS_CAPTURING_GROUP, MINIMUM_SKIPPED_WORDS_TO_END, NEXT_WORD_INDEX_1, NEXT_WORD_INDEX_2, ...),
                new Word(...), ...),
        new Sentence(...), ...);
```
If a section collected the capturing group names, they will be compiled to variables accessible as a field of the section, by `extend`ing `StandardRecognizerData`, that is:
```java
class SectionClass_SECTION_NAME extends StandardRecognizerData {
        SectionClass_SECTION_NAME() { super(... INITIALIZED AS ABOVE ...); }
        public String CAPTURING_GROUP_1 = "CAPTURING_GROUP_1", CAPTURING_GROUP_2 = "CAPTURING_GROUP_2", ...;
}
SectionClass_SECTION_NAME SECTION_NAME = new SectionClass_SECTION_NAME();
```

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
After clicking on the "Run" button, the Java code shown below should be inside a file called ClassName.java in the root directory of the repository. Indentation and spacing were added manually in order to improve readability.
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

    public static final class SectionClass_section_GPS_navigation extends StandardRecognizerData {
        SectionClass_section_GPS_navigation() {
            super(InputRecognizer.Specificity.medium,
                    new Sentence("question", new int[]{0, 1,},
                            new Word("take", false, 9, 2), new Word("bring", false, 11, 2), new Word("me", false, 10, 3),
                            new Word("to", false, 9, 4), new Word("place", true, 8, 5, 7, 8), new Word("by", false, 6, 6),
                            new Word("vehicle", true, 5, 7, 8), new Word("please", false, 4, 8)),
                    new Sentence("question", new int[]{0,},
                            new Word("give", false, 7, 1), new Word("me", false, 6, 2), new Word("directions", false, 5, 3),
                            new Word("to", false, 4, 4), new Word("place", true, 3, 5, 6), new Word("please", false, 1, 6)),
                    new Sentence("question", new int[]{0,},
                            new Word("how", false, 9, 1, 2), new Word("do", false, 6, 3), new Word("can", false, 8, 3),
                            new Word("i", false, 7, 4), new Word("get", false, 6, 5), new Word("to", false, 5, 6),
                            new Word("place", true, 4, 7)),
                    new Sentence("statement", new int[]{0,},
                            new Word("i", false, 10, 1), new Word("want", false, 9, 2), new Word("to", false, 8, 3),
                            new Word("go", false, 7, 4), new Word("to", false, 6, 5), new Word("place", true, 5, 6, 8),
                            new Word("by", false, 3, 7), new Word("vehicle", true, 2, 8)),
                    new Sentence("statement", new int[]{0,},
                            new Word("place", true, 10, 1), new Word("is", false, 8, 2), new Word("the", false, 7, 3),
                            new Word("place", false, 6, 4), new Word("i", false, 5, 5), new Word("want", false, 4, 6),
                            new Word("to", false, 3, 7), new Word("go", false, 2, 8), new Word("to", false, 1, 9)));
        }
        public final String place = "place", vehicle = "vehicle";
    }
    public static final SectionClass_section_GPS_navigation section_GPS_navigation = new SectionClass_section_GPS_navigation();
}
```
