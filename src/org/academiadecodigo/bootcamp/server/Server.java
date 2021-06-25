package org.academiadecodigo.bootcamp.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server {

    public static final int DEFAULT_PORT = 8080;
    private List<ChatServerHelper> list;

    public Server() {
        listen();
    }

    public void listen() {
        try {
            ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT);
            System.out.println("Server ON");
            clientServer(serverSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clientServer(ServerSocket serverSocket) {
        list = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                Socket client = serverSocket.accept();
                //System.out.println("What is your nickname? ");
                //String nickName = scanner.nextLine();
                ChatServerHelper chatServerHelper = new ChatServerHelper(client, this);
                Thread thread = new Thread(chatServerHelper);
                list.add(chatServerHelper);
                System.out.println("Connect");
                thread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void remove(ChatServerHelper chatServerHelper) {
        list.remove(chatServerHelper);
    }

    public void sendMensagem(String mensagem) {
        for (int i = 0; i < list.size(); i++) {
            list.get(i).sendMensagem(mensagem);
        }
    }

    class ChatServerHelper implements Runnable {

        private final Socket client;
        private final Server server;
        //private final String name;

        public ChatServerHelper(Socket client, Server server) {
            this.client = client;
            this.server = server;
            //this.name = name;
        }

        public void readerMensagem(BufferedReader bufferedReader) {
            try {
                server.sendMensagem(bufferedReader.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //public String getName() {
        //return name;
        //}

        public void sendMensagem(String string) {
            try {
                PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(client.getOutputStream()), true);
                printWriter.println(string);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            BufferedReader bufferedReader = null;
            String string = "";
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                BufferedReader bufferedReader1 = bufferedReader;
                string = bufferedReader1.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (true) {
                if(string.equals("/quit") || string.equals(null)){
                    try {
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    server.remove(this);
                    break;
                }
                readerMensagem(bufferedReader);
            }
        }
    }

}