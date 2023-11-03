package com.codegym.task.task30.task3008;

import com.codegym.task.task30.task3008.client.Client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {

        ServerSocket serverSocket;


        int port = ConsoleHelper.readInt();

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server is running.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while(true){
            try {
                Socket socket = serverSocket.accept();
                Thread thread = new Handler(socket);
                thread.start();
            } catch (IOException e) {
                System.out.println(e.getMessage());;
                try {
                    serverSocket.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                break;

            }
        }
    }

    public static void sendBroadcastMessage(Message message){
        for (Map.Entry<String, Connection> pair : connectionMap.entrySet()){
            Connection connection = pair.getValue();
            try {
                connection.send(message);
            } catch (IOException e) {
                System.out.println("Message couldn't be sent.");
            }
        }
    }

    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {

            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));

                Message response = connection.receive();

                if (response.getType().equals(MessageType.USER_NAME)) {
                    String data = response.getData();

                    if (data != null && !(data.equals(""))) {
                        if (!(connectionMap.containsKey(data))) {
                            connectionMap.put(data, connection);
                            connection.send(new Message(MessageType.NAME_ACCEPTED));
                            return data;
                        }
                    }
                }
            }

        }

        private void notifyUsers(Connection connection, String userName) throws IOException {
            for (Map.Entry<String, Connection> user : connectionMap.entrySet()){
                if (user.getKey().equals(userName)) continue;

                String name = user.getKey();
                Message message = new Message(MessageType.USER_ADDED, name);
                connection.send(message);
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException{

            while (true){
                Message message = connection.receive();
                if (message.getType() != MessageType.TEXT) ConsoleHelper.writeMessage("Error!");
                else{
                    String text = String.format("%s: %s", userName, message.getData());
                    Message toSend = new Message(MessageType.TEXT, text);
                    sendBroadcastMessage(toSend);
                }

            }
        }

        public void run(){

            SocketAddress socketAddress = socket.getRemoteSocketAddress();
            ConsoleHelper.writeMessage("A new connection was established with the remote address: " + socketAddress);

            Connection connection = null;
            try {
                connection = new Connection(socket);
                String userName = "";
                userName = serverHandshake(connection);

                Message message = new Message(MessageType.USER_ADDED, userName);
                sendBroadcastMessage(message);
                notifyUsers(connection, userName);

                serverMainLoop(connection, userName);

                connectionMap.remove(userName);
                message = new Message(MessageType.USER_REMOVED, userName);
                sendBroadcastMessage(message);



            } catch (IOException e) {
                System.out.println("An error occurred while communicating with the remote address.");
            } catch (ClassNotFoundException e) {
                System.out.println("An error occurred while communicating with the remote address.");
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                        ConsoleHelper.writeMessage("Connection with the remote address is closed");
                    }
                } catch (IOException e) {
                    ConsoleHelper.writeMessage(e.getMessage());
                }
            }

        }
    }

}
