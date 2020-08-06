# Sentences compiler for Dicio assistant
This tool provides a simple way to generate sentences to be matched for the Dicio assistant. It compiles files formatted with the Dicio-sentences-language to Java code that can be easily imported in projects using the interpreter of the Dicio assistant. It allows to pack together similar sentences while preserving readability.

## Dicio sentences language
Every file contains many sections, starting with section information and followed by a list of sentences. The section information is formatted like `SECTION_ID:SPECIFICITY`, where SPECIFICITY can be `low`, `medium` and `high`. Every sentence is made of an optional sentence id (formatted like `[SENTENCE_ID]`) and a list of constructs followed by a `;`. Constructs can be:
- diacritics-insensitive word (e.g. `hello`). A simple word: it can contain uppercase and lowercase unicode letters. Diacritics and accents will be ignored while matching. E.g. `hello` matches `hèllo`, `héllò`, ...
- diacritics-sensitive word (e.g. `"hello"`). Just like the diacritics-insensitive word, but in this case diacritics count. E.g. `"hello"` matches only the exact `hello`.
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
                new DiacriticsSensitiveWord(VALUE, MINIMUM_SKIPPED_WORDS_TO_END, NEXT_WORD_INDICES...),
                new DiacriticsInsensitiveWord(new byte[] {VALUES...}, MINIMUM_SKIPPED_WORDS_TO_END, NEXT_WORD_INDICES...),
                new CapturingGroup(NAME, MINIMUM_SKIPPED_WORDS_TO_END, NEXT_WORD_INDICES...),
                new ...(...), ...),
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
If a section map name is provided via the `--create-section-map` parameter, a `Map<String, StandardRecognizerData>` will be created containing a mapping between section ids and their corresponding `StandardRecognizerData` instance. This can be useful for autogeneration code (like that found in [`dicio-android`'s `build.gradle`](https://github.com/Stypox/dicio-android/blob/master/app/build.gradle)) in combination with the `--sections-file` parameter.

## Build and run
To build the project open it in Android Studio (IntelliJ Idea probably works, too) and create an Application configuration in the "Run/Debug Configurations" menu, set "Main class" to `org.dicio.sentences_compiler.main.SentencesCompiler`, "Use classpath of module" to `sentences_compiler` and "Program arguments" to the arguments for the compiler. Then run the newly created configuration with the "Run" button. Set `--help` as "Program arguments" to get an help screen explaining the options.

This project can be also used as a library. In that case, add `'com.github.Stypox:dicio-sentences-compiler:VERSION'` to your Gradle dependencies, replacing `VERSION` with the latest release or commit. Then use the `org.dicio.sentences_compiler.main.SentencesCompiler#compile()` function to compile using input files and output streams (take a look at the `javadoc` documentation provided there).

## Example
The file below is [`example.dslf`](example.dslf). "dslf" means "Dicio-Sentences-Language File".
```
mood: high       # comments are supported :-D
how (are you doing?)|(is it going);
[has_place] how is it going over there;
[french] comment "êtes" vous;  # quotes make sure êtes is matched diacritics-sensitively

GPS_navigation: medium
[question]  take|bring me to .place. (by .vehicle.)? please?;
[question]  give me directions to .place. please?;
[question]  how do|can i get to .place.;
[statement] i want to go to .place. (by .vehicle.)?;
[statement] .place. is the place i want to go to;
```
The above Dicio-sentences-language file is compiled to Java code by running the sentences-compiler as explained [above](#build-and-run), and setting the line below as "Program arguments".
```sh
--input "example.dslf" --output "ClassName.java" --sections-file "stdout" java --variable-prefix "section_" --package "com.pkg.name" --class "ClassName" --create-section-map "sections"
```
After clicking on the "Run" button, `mood GPS_navigation` should be outputted and the Java code shown below should be inside a file called ClassName.java in the root directory of the repository. Indentation and spacing were added manually in order to improve readability.
```java
/*
 * FILE AUTO-GENERATED BY dicio-sentences-compiler. DO NOT MODIFY.
 */

package com.pkg.name;

import java.util.Map;
import java.util.HashMap;
import org.dicio.component.InputRecognizer.Specificity;
import org.dicio.component.standard.Sentence;
import org.dicio.component.standard.StandardRecognizerData;
import org.dicio.component.standard.word.DiacriticsInsensitiveWord;
import org.dicio.component.standard.word.DiacriticsSensitiveWord;
import org.dicio.component.standard.word.CapturingGroup;

public class ClassName {
    public static final StandardRecognizerData section_mood =
        new StandardRecognizerData(Specificity.high,
            new Sentence("", new int[]{0},
                new DiacriticsInsensitiveWord(new byte[]{0, 91, 0, 98, 0, 106, 0, 0, 0, 0}, 4, 1, 4),
                new DiacriticsInsensitiveWord(new byte[]{0, 83, 0, 101, 0, 88, 0, 0, 0, 0}, 3, 2),
                new DiacriticsInsensitiveWord(new byte[]{0, 108, 0, 98, 0, 104, 0, 0, 0, 0}, 2, 3, 7),
                new DiacriticsInsensitiveWord(new byte[]{0, 86, 0, 98, 0, 92, 0, 97, 0, 90, 0, 0, 0, 0}, 1, 7),
                new DiacriticsInsensitiveWord(new byte[]{0, 92, 0, 102, 0, 0, 0, 0}, 3, 5),
                new DiacriticsInsensitiveWord(new byte[]{0, 92, 0, 103, 0, 0, 0, 0}, 2, 6),
                new DiacriticsInsensitiveWord(new byte[]{0, 90, 0, 98, 0, 92, 0, 97, 0, 90, 0, 0, 0, 0}, 1, 7)),
            new Sentence("has_place", new int[]{0},
                new DiacriticsInsensitiveWord(new byte[]{0, 91, 0, 98, 0, 106, 0, 0, 0, 0}, 6, 1),
                new DiacriticsInsensitiveWord(new byte[]{0, 92, 0, 102, 0, 0, 0, 0}, 5, 2),
                new DiacriticsInsensitiveWord(new byte[]{0, 92, 0, 103, 0, 0, 0, 0}, 4, 3),
                new DiacriticsInsensitiveWord(new byte[]{0, 90, 0, 98, 0, 92, 0, 97, 0, 90, 0, 0, 0, 0}, 3, 4),
                new DiacriticsInsensitiveWord(new byte[]{0, 98, 0, 105, 0, 88, 0, 101, 0, 0, 0, 0}, 2, 5),
                new DiacriticsInsensitiveWord(new byte[]{0, 103, 0, 91, 0, 88, 0, 101, 0, 88, 0, 0, 0, 0}, 1, 6)),
            new Sentence("french", new int[]{0},
                new DiacriticsInsensitiveWord(new byte[]{0, 85, 0, 98, 0, 96, 0, 96, 0, 88, 0, 97, 0, 103, 0, 0, 0, 0}, 3, 1),
                new DiacriticsSensitiveWord("êtes", 2, 2),
                new DiacriticsInsensitiveWord(new byte[]{0, 105, 0, 98, 0, 104, 0, 102, 0, 0, 0, 0}, 1, 3)));

    public static final class SectionClass_section_GPS_navigation extends StandardRecognizerData {
        SectionClass_section_GPS_navigation() {
            super(Specificity.medium,
                new Sentence("question", new int[]{0, 1},
                    new DiacriticsInsensitiveWord(new byte[]{0, 103, 0, 83, 0, 94, 0, 88, 0, 0, 0, 0}, 9, 2),
                    new DiacriticsInsensitiveWord(new byte[]{0, 84, 0, 101, 0, 92, 0, 97, 0, 90, 0, 0, 0, 0}, 11, 2),
                    new DiacriticsInsensitiveWord(new byte[]{0, 96, 0, 88, 0, 0, 0, 0}, 10, 3),
                    new DiacriticsInsensitiveWord(new byte[]{0, 103, 0, 98, 0, 0, 0, 0}, 9, 4),
                    new CapturingGroup("place", 8, 5, 7, 8),
                    new DiacriticsInsensitiveWord(new byte[]{0, 84, 0, 108, 0, 0, 0, 0}, 6, 6),
                    new CapturingGroup("vehicle", 5, 7, 8),
                    new DiacriticsInsensitiveWord(new byte[]{0, 99, 0, 95, 0, 88, 0, 83, 0, 102, 0, 88, 0, 0, 0, 0}, 4, 8)),
                new Sentence("question", new int[]{0},
                    new DiacriticsInsensitiveWord(new byte[]{0, 90, 0, 92, 0, 105, 0, 88, 0, 0, 0, 0}, 7, 1),
                    new DiacriticsInsensitiveWord(new byte[]{0, 96, 0, 88, 0, 0, 0, 0}, 6, 2),
                    new DiacriticsInsensitiveWord(new byte[]{0, 86, 0, 92, 0, 101, 0, 88, 0, 85, 0, 103, 0, 92, 0, 98, 0, 97, 0, 102, 0, 0, 0, 0}, 5, 3),
                    new DiacriticsInsensitiveWord(new byte[]{0, 103, 0, 98, 0, 0, 0, 0}, 4, 4),
                    new CapturingGroup("place", 3, 5, 6),
                    new DiacriticsInsensitiveWord(new byte[]{0, 99, 0, 95, 0, 88, 0, 83, 0, 102, 0, 88, 0, 0, 0, 0}, 1, 6)),
                new Sentence("question", new int[]{0},
                    new DiacriticsInsensitiveWord(new byte[]{0, 91, 0, 98, 0, 106, 0, 0, 0, 0}, 9, 1, 2),
                    new DiacriticsInsensitiveWord(new byte[]{0, 86, 0, 98, 0, 0, 0, 0}, 6, 3),
                    new DiacriticsInsensitiveWord(new byte[]{0, 85, 0, 83, 0, 97, 0, 0, 0, 0}, 8, 3),
                    new DiacriticsInsensitiveWord(new byte[]{0, 92, 0, 0, 0, 0}, 7, 4),
                    new DiacriticsInsensitiveWord(new byte[]{0, 90, 0, 88, 0, 103, 0, 0, 0, 0}, 6, 5),
                    new DiacriticsInsensitiveWord(new byte[]{0, 103, 0, 98, 0, 0, 0, 0}, 5, 6),
                    new CapturingGroup("place", 4, 7)),
                new Sentence("statement", new int[]{0},
                    new DiacriticsInsensitiveWord(new byte[]{0, 92, 0, 0, 0, 0}, 10, 1),
                    new DiacriticsInsensitiveWord(new byte[]{0, 106, 0, 83, 0, 97, 0, 103, 0, 0, 0, 0}, 9, 2),
                    new DiacriticsInsensitiveWord(new byte[]{0, 103, 0, 98, 0, 0, 0, 0}, 8, 3),
                    new DiacriticsInsensitiveWord(new byte[]{0, 90, 0, 98, 0, 0, 0, 0}, 7, 4),
                    new DiacriticsInsensitiveWord(new byte[]{0, 103, 0, 98, 0, 0, 0, 0}, 6, 5),
                    new CapturingGroup("place", 5, 6, 8),
                    new DiacriticsInsensitiveWord(new byte[]{0, 84, 0, 108, 0, 0, 0, 0}, 3, 7),
                    new CapturingGroup("vehicle", 2, 8)),
                new Sentence("statement", new int[]{0},
                    new CapturingGroup("place", 10, 1),
                    new DiacriticsInsensitiveWord(new byte[]{0, 92, 0, 102, 0, 0, 0, 0}, 8, 2),
                    new DiacriticsInsensitiveWord(new byte[]{0, 103, 0, 91, 0, 88, 0, 0, 0, 0}, 7, 3),
                    new DiacriticsInsensitiveWord(new byte[]{0, 99, 0, 95, 0, 83, 0, 85, 0, 88, 0, 0, 0, 0}, 6, 4),
                    new DiacriticsInsensitiveWord(new byte[]{0, 92, 0, 0, 0, 0}, 5, 5),
                    new DiacriticsInsensitiveWord(new byte[]{0, 106, 0, 83, 0, 97, 0, 103, 0, 0, 0, 0}, 4, 6),
                    new DiacriticsInsensitiveWord(new byte[]{0, 103, 0, 98, 0, 0, 0, 0}, 3, 7),
                    new DiacriticsInsensitiveWord(new byte[]{0, 90, 0, 98, 0, 0, 0, 0}, 2, 8),
                    new DiacriticsInsensitiveWord(new byte[]{0, 103, 0, 98, 0, 0, 0, 0}, 1, 9)));
        }
        public final String place = "place", vehicle = "vehicle";
    }
    public static final SectionClass_section_GPS_navigation section_GPS_navigation = new SectionClass_section_GPS_navigation();

    public static final Map<String, StandardRecognizerData>sections = new HashMap<String, StandardRecognizerData>() {{
        put("mood", section_mood);
        put("GPS_navigation", section_GPS_navigation);
    }};
}

```
