package model_readers_only_java_console;

import constants.IConst;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class TcpReader implements Runnable, IConst {
    private final Scanner scanner;
    private final String host;
    private final int port;
    private final int timeout;
    private ArrayList<String> strings;
    private OnEndReadListener onEndReadListener;
    private Socket socket;

    public TcpReader(String host, int port, int timeout) {
        this.scanner = new Scanner(System.in);
        this.port = port;
        this.host= host;
        this.timeout = timeout;
    }

    @Override
    public void run() {
        strings = new ArrayList<>();
        try {
            InetSocketAddress socketAddress = new InetSocketAddress(host, port);
            String text = String.format("connecting to server: %s, port %d, timeout %d ... ", host, port, timeout);
            System.out.println(text);

            socket = new Socket();
            socket.connect(socketAddress, timeout);
            System.out.println("connected");

            sendQuery();
            readServer();
            System.out.println("loaded strings from server: " + strings.size());

        } catch (Exception ex) {
            System.out.println("connecting failed");
        }

        if(onEndReadListener != null) {
            onEndReadListener.action(strings);
        }

    }

    private void sendQuery() throws IOException {
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
        printWriter.println(QUERY);
        printWriter.flush();
        System.out.println("send to server: " + QUERY);
    }

    private void readServer() throws IOException {
        Scanner scanner = new Scanner(socket.getInputStream());
        int i = 0;
        while (scanner.hasNextLine()) {
            strings.add(scanner.nextLine());
            if (i++ >= 32) {
                break;
            }
        }
    }

    public ArrayList<String> getStrings() {
        return strings;
    }

    public void setOnEndReadListener(OnEndReadListener onEndReadListener) {
        this.onEndReadListener = onEndReadListener;
    }

    interface OnEndReadListener {
        void action(ArrayList<String> strings);
    }

}
