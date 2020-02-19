package ru.yandex.danikirillov.client;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.concurrent.ThreadLocalRandom;

public class BotClient extends Client {

    public class BotSocketThread extends SocketThread {
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            super.processIncomingMessage(message);
            SimpleDateFormat dateFormat;
            String[] spl = message.split(": ");
            if (spl.length != 2)
                return;
            StringBuilder result = new StringBuilder("Информация для " + spl[0] + ": ");
            switch (spl[1]) {
                case "дата":
                    dateFormat = new SimpleDateFormat("d.MM.YYYY");
                    break;
                case "день":
                    dateFormat = new SimpleDateFormat("d");
                    break;
                case "месяц":
                    dateFormat = new SimpleDateFormat("MMMM");
                    break;
                case "год":
                    dateFormat = new SimpleDateFormat("YYYY");
                    break;
                case "время":
                    dateFormat = new SimpleDateFormat("H:mm:ss");
                    break;
                case "час":
                    dateFormat = new SimpleDateFormat("H");
                    break;
                case "минуты":
                    dateFormat = new SimpleDateFormat("m");
                    break;
                case "секунды":
                    dateFormat = new SimpleDateFormat("s");
                    break;
                default: return;
            }
            result.append(dateFormat.format(new GregorianCalendar().getTime()));
            sendTextMessage(result.toString());
        }
    }

    @Override
    protected String getUserName() {
        String thirdName = " Даниловна";
        String[] names = {"Валентина", "Анна", "Елена", "Кирилл", "Зинаида", "Галина", "София", "Антонина"};
        int pos = ThreadLocalRandom.current().nextInt() % 8;
        return pos == 3 ? names[pos] : names[pos] + thirdName;
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    public static void main(String[] args) {
        new BotClient().run();
    }
}
