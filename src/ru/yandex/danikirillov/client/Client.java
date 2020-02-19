package ru.yandex.danikirillov.client;

import ru.yandex.danikirillov.Connection;
import ru.yandex.danikirillov.ConsoleHelper;
import ru.yandex.danikirillov.Message;
import ru.yandex.danikirillov.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client {
    protected Connection connection;
    private volatile boolean clientConnected = false;

    protected String getServerAddress() {
        ConsoleHelper.writeMessage("Введите адрес сервера: ");
        return ConsoleHelper.readString();
    }

    protected int getServerPort() {
        ConsoleHelper.writeMessage("Введите порт сервера: ");
        return ConsoleHelper.readInt();
    }

    protected String getUserName() {
        ConsoleHelper.writeMessage("Введите имя пользователя: ");
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole() {
        return true;
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    protected void sendTextMessage(String text) {
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Отправка сорвалась, ээээээээх");
            clientConnected = false;
        }
    }

    public void run() {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        synchronized (this) {
            try {
                wait();
                if (clientConnected) {
                    ConsoleHelper.writeMessage("Соединение установлено.\nДля выхода наберите команду 'exit'.");
                    while (clientConnected) {
                        String msg = ConsoleHelper.readString();
                        if (msg.equals("exit"))
                            clientConnected = false;
                        else if (shouldSendTextFromConsole())
                            sendTextMessage(msg);
                    }
                } else
                    ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
            } catch (InterruptedException e) {
                ConsoleHelper.writeMessage("Что-то пошло не так.");
                Thread.currentThread().interrupt();
            }
        }

    }

    public static void main(String[] args) {
        new Client().run();
    }

    public class SocketThread extends Thread {

        @Override
        public void run() {
            try {
                connection = new Connection(new Socket(getServerAddress(), getServerPort()));
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }

        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage("Участник с именем " + userName + " присоединился к чату.");
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage("Участник с именем " + userName + " покинул чат.");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            synchronized (Client.this) {
                Client.this.clientConnected = clientConnected;
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (!clientConnected) {
                Message msg = connection.receive();
                if (msg.getType() == MessageType.NAME_REQUEST) {
                    connection.send(new Message(MessageType.USER_NAME, getUserName()));
                } else if (msg.getType() == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
                Message msg = connection.receive();
                if (msg.getType() != MessageType.TEXT && msg.getType() != MessageType.USER_ADDED && msg.getType() != MessageType.USER_REMOVED)
                    throw new IOException("Unexpected MessageType");
                switch (msg.getType()) {
                    case TEXT:
                        processIncomingMessage(msg.getData());
                        break;
                    case USER_ADDED:
                        informAboutAddingNewUser(msg.getData());
                        break;
                    case USER_REMOVED:
                        informAboutDeletingNewUser(msg.getData());
                        break;
                    default:
                        throw new IOException("Unexpected MessageType");
                }
            }
        }
    }

}
