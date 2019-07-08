package com.stypox.sentences_compiler.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import static java.util.stream.Collectors.toCollection;

public class UnfoldingUtils {
    @SuppressWarnings("unchecked")
    public static ArrayList<ArrayList<String>> cloneArray(ArrayList<ArrayList<String>> arr) {
        ArrayList<ArrayList<String>> clonedArr = new ArrayList<>(arr.size());
        for (ArrayList<String> elArr : arr) {
            clonedArr.add(new ArrayList<String>(elArr.size()));
            for (String el : elArr) {
                clonedArr.get(clonedArr.size()-1).add(new String(el));
            }
        }
        return clonedArr;
    }

    public static ArrayList<ArrayList<String>> multiplyArray(ArrayList<ArrayList<String>> arr, int times) {
        ArrayList<ArrayList<String>> multipliedArr = new ArrayList<>();
        for (int i = 0; i < times; ++i) {
            multipliedArr.addAll(cloneArray(arr));
        }
        return multipliedArr;
    }
}
