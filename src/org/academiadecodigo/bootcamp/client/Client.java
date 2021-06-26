package org.academiadecodigo.bootcamp.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client implements Runnable{

    private Socket socket;
    String mensagem;
    Thread thread;

    public Client(Integer port){
        clientConnect(port);
        thread = new Thread(this);
        thread.start();
        send();
    }

    public void clientConnect(Integer port){
        try {
            socket = new Socket("localhost", port);
            System.out.println("Connect to " + socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(){
        Scanner scanner = new Scanner(System.in);
        mensagem = scanner.nextLine();
        PrintWriter printWriter;
        try {
            printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            while (!socket.isClosed()) {
                printWriter.println(mensagem);
                mensagem = scanner.nextLine();
                if(mensagem.equals("/quit") || mensagem == null){
                    printWriter.println(mensagem);
                    close();
                    return;
                }
            }
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    public void read(BufferedReader bufferedReader){
        try {
            String mensagem = bufferedReader.readLine();
            if (mensagem == null){
                close();
                System.exit(1);
                return;
            }
            System.out.println(mensagem);
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    public void close(){
        try {
            socket.close();
            thread.interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void run() {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (!socket.isClosed()) {
            read(bufferedReader);
        }
    }
}
