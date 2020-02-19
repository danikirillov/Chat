package ru.yandex.danikirillov.client;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;

public class ClientGuiView {
    private final ClientGuiController controller;

    private final JFrame frame = new JFrame("Чат");
    private final JTextField textField = new JTextField(50);
    private final JTextArea messages = new JTextArea(10, 50);
    private final JTextArea users = new JTextArea(10, 10);

    public ClientGuiView(ClientGuiController controller) {
        this.controller = controller;
        initView();
    }

    private void initView() {
        initTextField();
        initMessages();
        initUsers();
        initFrame();
    }

    private void initTextField() {
        textField.setEditable(false);
        textField.setBackground(Color.BLACK);
        textField.setForeground(Color.RED);
        frame.getContentPane().add(textField, BorderLayout.SOUTH);

        textField.addActionListener(e -> {
            controller.sendTextMessage(textField.getText());
            textField.setText("");
        });
    }

    private void initMessages() {
        messages.setEditable(false);
        messages.setBackground(Color.BLACK);
        messages.setForeground(Color.RED);
        ((DefaultCaret)(messages.getCaret())).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane jScrollPane = new JScrollPane(messages);
        jScrollPane.setBackground(Color.BLACK);
        jScrollPane.setForeground(Color.DARK_GRAY);
        frame.getContentPane().add(jScrollPane, BorderLayout.WEST);
    }

    private void initUsers() {
        users.setEditable(false);
        users.setBackground(Color.BLACK);
        users.setForeground(Color.RED);
        JScrollPane jScrollPane = new JScrollPane(users);
        jScrollPane.setBackground(Color.BLACK);
        jScrollPane.setForeground(Color.DARK_GRAY);
        frame.getContentPane().add(jScrollPane, BorderLayout.EAST);
    }

    private void initFrame() {
        frame.getContentPane().setBackground(new Color(35,33,28));
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public String getServerAddress() {
        return JOptionPane.showInputDialog(frame, "Введите адрес сервера:", "Конфигурация клиента", JOptionPane.QUESTION_MESSAGE);
    }

    public int getServerPort() {
        while (true) {
            String port = JOptionPane.showInputDialog(frame, "Введите порт сервера:", "Конфигурация клиента", JOptionPane.QUESTION_MESSAGE);
            if (port.matches("\\d{1,5}"))
                return Integer.parseInt(port.trim());
            else
                JOptionPane.showMessageDialog(frame, "Был введен некорректный порт сервера. Попробуйте еще раз.", "Конфигурация клиента", JOptionPane.ERROR_MESSAGE);
        }
    }

    public String getUserName() {
        return JOptionPane.showInputDialog(frame, "Введите ваше имя:", "Конфигурация клиента", JOptionPane.QUESTION_MESSAGE);
    }

    public void notifyConnectionStatusChanged(boolean clientConnected) {
        textField.setEditable(clientConnected);
        if (clientConnected)
            JOptionPane.showMessageDialog(frame, "Соединение с сервером установлено", "Чат", JOptionPane.INFORMATION_MESSAGE);
        else
            JOptionPane.showMessageDialog(frame, "Клиент не подключен к серверу", "Чат", JOptionPane.ERROR_MESSAGE);
    }

    public void refreshMessages() {
        messages.append(controller.getModel().getNewMessage() + "\n");
    }

    public void refreshUsers() {
        users.setText(String.join("\n", controller.getModel().getAllUserNames()));
    }
}
