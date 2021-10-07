package com.controller;

import java.util.Scanner;

public class Util {
    private Util() {
    }

    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    //ввод цифры
    public static int nextInt(String text, int min, int max, int defaultValue){
        while(true) {
            Scanner sc = new Scanner(System.in);
            System.out.print(text);
            String cmd = sc.nextLine();

            if(cmd.isEmpty() && defaultValue != Integer.MIN_VALUE) {
                System.out.println(defaultValue);
                return defaultValue;
            }

            if(isInteger(cmd)) {
                int num = Integer.parseInt(cmd);
                if(num >= min && num <= max) {
                    return num;
                }
            }
        }
    }


    public static int nextInt(String text, int min, int max){
        Scanner sc = new Scanner(System.in);
        while(true) {
            System.out.print(text);
            String cmd = sc.next();

            if(isInteger(cmd)) {
                int num = Integer.parseInt(cmd);
                if(num >= min && num <= max) {
                    return num;
                }
            }
        }
    }

    public static int nextInt(String text, int max){
        return nextInt(text, 0, max);
    }

    public static char nextChar(String text, char min, char max) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print(text);
            String string = sc.next().toUpperCase();
            if(string.length() != 1) {
                continue;
            }
            char cmd = string.charAt(0);
            if (cmd >= min && cmd <= max) {
                return cmd;
            }
        }
    }

}
