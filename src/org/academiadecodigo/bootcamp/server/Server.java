package org.academiadecodigo.bootcamp.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
        while (true) {
            try {
                Socket client = serverSocket.accept();
                ChatServerHelper chatServerHelper = new ChatServerHelper(client, this);
                list.add(chatServerHelper);
                System.out.println("Connect");
                System.out.println(list.size());
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

    public String checkUser(){
        String string = "User Online now : ";
        for (int i = 0; i < list.size(); i++) {
            string = string + "\n" + list.get(i).getName();
        }
        return string;
    }

    public ChatServerHelper getUserForName(String name){
        for (int i = 0; i < list.size(); i++) {
            if(list.get(i).getName().equals(name)){
                return list.get(i);
            }
        }
        return null;
    }

    public boolean whisper(String name, String mensagem){
       ChatServerHelper chatServerHelper = getUserForName(name);
       if (chatServerHelper == null){
           return false;
       }
       list.get(list.indexOf(chatServerHelper)).sendMensagem(mensagem);
       return true;
    }

    public boolean kick(String name){
        ChatServerHelper chatServerHelper = getUserForName(name);
        if (chatServerHelper == null){
            return false;
        }
        list.get(list.indexOf(chatServerHelper)).sendMensagem("You are Kick for the Server");
        list.get(list.indexOf(chatServerHelper)).quit();
        return true;
    }

    class ChatServerHelper implements Runnable {

        private final Socket client;
        private final Server server;
        private String name;
        private Thread thread;
        private Boolean status = true;

        public ChatServerHelper(Socket client, Server server) {
            this.client = client;
            this.server = server;
            setName();
            thread = new Thread(this);
            thread.start();
            sendMensagem("Welcome for the server, use /help for see comands... ");
        }

        public void readerMensagem(BufferedReader bufferedReader) throws IOException {
            if(!client.isClosed()) {
                try {
                    String mensagem = bufferedReader.readLine();
                    if (mensagem == null){
                        return;
                    }
                    String[] strings = mensagem.split(" ");
                    if (strings[0].equals("/setname")) {
                        setName();
                    } else if (strings[0].equals("/quit")) {
                        quit();
                    } else if (strings[0].equals("/list")) {
                        checkUser();
                    } else if (strings[0].equals("/whisper")) {
                        whisper(strings);
                    } else if (strings[0].equals("/kick")) {
                        kick(strings);
                    } else if(strings[0].equals("/help")){
                        help();
                    } else {
                        server.sendMensagem(name + " : " + mensagem);
                    }
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }
        }

        public void quit(){
            try {
                client.close();
                server.remove(this);
                thread.interrupt();
                status = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void checkUser(){
            sendMensagem(server.checkUser());
        }

        public void kick(String [] strings){
            if(strings.length < 2){
                sendMensagem("This is wrong, use /kick name");
                return;
            }
            if(!server.kick(strings[1])){
                sendMensagem("Name not found, use /list for see all user online");
                return;
            }
        }

        public void whisper(String [] strings){
            String mensagem = "";
            if(strings.length < 3){
                sendMensagem("This is wrong, use /whisper name mensagem");
                return;
            }
            for (int i = 2; i < strings.length; i++) {
                mensagem = mensagem + strings[i];
            }
            if(!server.whisper(strings[1],name + " whisper for you : " + mensagem)){
                sendMensagem("Name not found, use /list for see all user online");
                return;
            }
        }

        public void setName(){
            sendMensagem("What is your name? ");
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
                name = bufferedReader.readLine();
                sendMensagem("Your name is now: " + name);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendMensagem(String string) {
            try {
                PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(client.getOutputStream(),StandardCharsets.UTF_8), true);
                printWriter.println(string);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void help(){
            sendMensagem("/quit for quit in the chat");
            sendMensagem("/list for see all user online in the chat");
            sendMensagem("/whisper name mensagem, for send private mensagem the chat");
            sendMensagem("/kick name, for kick user of the chat");
        }

        public String getName() {
            return name;
        }

        @Override
        public void run() {
            BufferedReader bufferedReader;
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
                while (status) {
                    readerMensagem(bufferedReader);
                }
                //string = bufferedReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}