package com.controller;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class FileReader {
    private FileReader() {
    }

    public static ArrayList<String> read(String filename) throws IOException {
        ArrayList<String> strings = new ArrayList<>();

        java.io.FileReader fr = new java.io.FileReader(filename);
        Scanner scan = new Scanner(fr);
        while (scan.hasNextLine()) {
            strings.add(scan.nextLine());
        }
        fr.close();
        return strings;
    }

    public static String getFilenameWithAbsolutePatch(String localPatch, String fileName) {
        String path = new File(".").getAbsolutePath();
        return path + localPatch + fileName;
    }

    //файл существует?
    public static boolean isFileExists(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

}
