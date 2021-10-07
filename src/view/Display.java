package view;

public class Display {
    private static Display display;

    private Display() {
    }

    public static Display getInstance() {
        if(display == null) {
            display = new Display();
        }
        return display;
    }

    public void print(String text) {
        System.out.print(text);
    }


    public void println(String text) {
        System.out.println(text);
    }

    public void println() {
        System.out.println();
    }


    public void setColor(String color) {
        System.out.print(color);
    }

    public void resetColor() {
        System.out.print(Color.ANSI_RESET);
    }

    public void printColor(String color, String text) {
        System.out.print(color);
        System.out.print(text);
        System.out.print(Color.ANSI_RESET);
        System.out.println();
    }

    public void printlGreen(String text) {
        printColor(Color.ANSI_GREEN, text);
    }

    public void printlRed(String text) {
        printColor(Color.ANSI_RED, text);
    }

    public void printlBlue(String text) {
        printColor(Color.ANSI_BLUE, text);
    }

    public void printlYellow(String text) {
        printColor(Color.ANSI_YELLOW, text);
    }

    public void printlCyan(String text) {
        printColor(Color.ANSI_CYAN, text);
    }

    public void printlPurple(String text) {
        printColor(Color.ANSI_PURPLE, text);
    }


}
