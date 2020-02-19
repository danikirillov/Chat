package ru.yandex.danikirillov;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static final Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        startServer();
    }

    private static void startServer() {
        ConsoleHelper.writeMessage("Введите порт сервера: ");
        try (ServerSocket serverSocket = new ServerSocket(ConsoleHelper.readInt())) {
            ConsoleHelper.writeMessage("Сервер запущен");
            while (true)
                new Handler(serverSocket.accept()).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendBroadcastMessage(Message message) {
        connectionMap.forEach((k, v) -> {
            try {
                v.send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("Не получилось отправить сообщение пользователю " + k);
            }
        });
    }

    private static class Handler extends Thread {
        private final Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            String name = null;
            ConsoleHelper.writeMessage("Установлено новое соединение с удаленным адресом " + socket.getRemoteSocketAddress());

            try (Connection connection = new Connection(socket)) {

                name = serverHandshake(connection);
                startConnection(connection, name);

            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Произошла ошибка при ообмене данными с удаленым адресом");
            } finally {
                if (name != null) {
                    connectionMap.remove(name);
                    stopConnection(name);
                }
            }
        }

        private void startConnection(Connection connection, String name) throws IOException, ClassNotFoundException {
            sendBroadcastMessage(new Message(MessageType.USER_ADDED, name));
            notifyUsers(connection, name);
            serverMainLoop(connection, name);
        }

        private void stopConnection(String name) {
            sendBroadcastMessage(new Message(MessageType.USER_REMOVED, name));
            ConsoleHelper.writeMessage("Соединение с удаленным адрпесом закрыто");
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            connection.send(new Message(MessageType.NAME_REQUEST, "Введите имя пользователя: "));
            String name = connection.receive().getData();
            if (name == null || name.isBlank() || connectionMap.containsKey(name))
                return serverHandshake(connection);

            connectionMap.put(name, connection);
            connection.send(new Message(MessageType.NAME_ACCEPTED, "У вас красивое имя, " + name));

            return name;
        }

        private void notifyUsers(Connection connection, String name) {
            connectionMap.forEach((k, v) -> {
                if (!k.equals(name))
                    try {
                        connection.send(new Message(MessageType.USER_ADDED, k));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            });
        }

        private void serverMainLoop(Connection connection, String name) throws IOException, ClassNotFoundException {
            Message msg;
            while (true)
                if ((msg = connection.receive()).getType() == MessageType.TEXT)
                    sendBroadcastMessage(new Message(MessageType.TEXT, name + ": " + msg.getData()));
                else
                    ConsoleHelper.writeMessage("Ошибка, сообщение не является текстом");

        }
    }
}
