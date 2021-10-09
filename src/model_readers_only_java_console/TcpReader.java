package model_readers_only_java_console;

import constants.IConst;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TcpReader implements IConst {
    private final String host;
    private final int port;
    private final int timeout;
    private ArrayList<String> strings;
    private Socket socket;

    public TcpReader(String host, int port, int timeout) {
        Scanner scanner = new Scanner(System.in);
        this.port = port;
        this.host= host;
        this.timeout = timeout;
    }

    public void read() {
        strings = new ArrayList<>();
        try {
            String text = String.format("connecting to server: %s, port %d, timeout %d... ", host, port, timeout);
            System.out.println(text);

            InetSocketAddress socketAddress = new InetSocketAddress(host, port);

            socket = new Socket();
            socket.connect(socketAddress, timeout);
            System.out.println("connected");
        } catch (Exception ex) {
            throw new TcpReaderException("connect failed");
        }

        try {
            sendQuery();
            readServer();
        } catch (Exception e) {
            throw new TcpReaderException("read data failed");
        }
    }

    private void sendQuery() throws IOException {
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
        printWriter.println(QUERY);
        printWriter.flush();
        System.out.println("send to server: " + QUERY);
    }

    private void readServer() throws IOException, ClassNotFoundException {
        ObjectInputStream objectInput = new ObjectInputStream(socket.getInputStream());
        strings = (ArrayList<String>)objectInput.readObject();
    }

    public ArrayList<String> getStrings() {
        return strings;
    }


}
