# Sentences compiler for Dicio assistant
This tool provides a simple way to generate sentences to be matched for the Dicio assistant. It compiles files formatted with the Dicio-sentences-language to Java code that can be easily imported in projects using the interpreter of the Dicio assistant. It allows to pack toghether similar sentences while preserving readability.

## Dicio sentences language
Every file contains many sections, starting with a section id (formatted like `SECTION_ID:`) and followed by a list of sentences. Every sentence is made of an optional sentence id (formatted like `[SENTENCE_ID]`) and a list of constructs followed by a `;`. Constructs can be:
- word (e.g. `hello`). A simple word: it can contain uppercase and lowercase unicode letters.
- or-red constructs (e.g. `hello|hi`). Every time the compiler encounters n or-red constructs, it will generate n sentence variants: the first one contains the first or-red construct, ..., the nth one contains the nth or-red construct. E.g. `hello|hi|hey assistant` generates `hello assistant`, `hi assistant` and `hey assistant`.
- optional construct (e.g. `hello?`). This tells the compiler to generate 2 sentence variants: one with the construct and another without it.
- parenthesized construct (e.g. `(hello)`). This lets you pack constructs toghether and, for example, "or" them all. Just as math parenthesis do. E.g. `how (are you doing?)|(is it going)` generates `how are you`, `how are you doing` and `how is it going`.
- capturing group (`..`). This tells the interpreter to match a variable-length list of any word to that part of the sentence. They cannot be or-red or made optional since it would hurt readability. The maximum number of capturing groups supported by the interpreter is 2. E.g. `how are you ..` matches `how are you Tom`.

## Example
```
mood:
how (are you doing?)|(is it going);
[has_place] how is it going over there;

GPS-navigation:
[question]  take|bring me to .. please?;
[question]  give me directions to .. please?;
[question]  how do|can i get to ..;
[statement] i want to go to ..;
[statement] .. is the place i want to go to;
```
