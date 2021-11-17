package model_readers_only_java_console;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class FileReader {
    private FileReader() {
    }

    public static ArrayList<String> read(String filename) {
        ArrayList<String> strings = new ArrayList<>();
        System.out.println(filename);
        try {
            java.io.FileReader fr = new java.io.FileReader(filename);
            Scanner scan = new Scanner(fr);
            while (scan.hasNextLine()) {
                strings.add(scan.nextLine());
            }
            fr.close();
        } catch (IOException ex) {
            throw new FileReaderException("filed read file: " + filename);
        }

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
