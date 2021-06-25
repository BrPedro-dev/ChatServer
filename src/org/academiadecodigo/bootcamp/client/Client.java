package org.academiadecodigo.bootcamp.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client implements Runnable{

    private Socket socket;

    public Client(Integer port){
        clientConnect(port);
        Thread thread = new Thread(this);
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
        String mensagem = scanner.nextLine();
        PrintWriter printWriter;
        try {
            printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            while (true) {
                printWriter.println(mensagem);
                mensagem = scanner.nextLine();
                if(mensagem.equals("/quit")){
                    System.exit(1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void read(BufferedReader bufferedReader){
        try {
            System.out.println(bufferedReader.readLine());
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
        while (true) {
            read(bufferedReader);
        }
    }
}
