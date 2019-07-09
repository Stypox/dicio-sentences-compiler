package com.stypox.sentences_compiler.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class UnfoldingUtilsTest {

    @Test
    public void testMultiplyArray() {
        ArrayList<ArrayList<String>> strings = new ArrayList<ArrayList<String>>() {{
            add(new ArrayList<String>() {{ add("a"); }});
            add(new ArrayList<String>() {{ add("b"); }});
        }};
        ArrayList<ArrayList<String>> strings2 = UnfoldingUtils.multiplyArray(strings, 2);

        // also check that arrays are all unique, i.e. there are no equal references
        strings2.get(0).add("A");
        assertSame(strings2.size()+" is not equal to 2 * "+strings.size(), strings2.size(), 2 * strings.size());
        assertArrayEquals("Cloned array are not unique: there are equal references", new String[]{"a", "A"}, strings2.get(0).toArray());
        assertArrayEquals(new String[]{"b"},      strings2.get(1).toArray());
        assertArrayEquals(new String[]{"a"},      strings2.get(2).toArray());
        assertArrayEquals(new String[]{"b"},      strings2.get(3).toArray());
    }
}