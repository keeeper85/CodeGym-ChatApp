package com.codegym.task.task30.task3008.client;

import com.codegym.task.task30.task3008.ConsoleHelper;
import com.codegym.task.task30.task3008.Message;
import com.codegym.task.task30.task3008.MessageType;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class BotClient extends Client{

    @Override
    protected SocketThread getSocketThread(){
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole(){
        return false;
    }

    @Override
    protected String getUserName(){
        double random = (Math.random() * 100);
        int number = (int) random;
        String name = "date_bot_" + number;
        return name;
    }

    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }

    public class BotSocketThread extends SocketThread{

        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException{

            sendTextMessage("Hello, there. I'm a bot. I understand the following commands: date, day, month, year, time, hour, minutes, seconds.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);

            if (message.contains(":")) {
                String[] data = message.split(": ");
                if (data.length != 2) {
                    return;
                }

                String senderName = data[0];
                String senderMessage = data[1];

                Date date = new GregorianCalendar().getTime();

                SimpleDateFormat sdf = null;
                String requiredTime;
                String ourMessage;

                switch (senderMessage) {
                    case "date":
                        sdf = new SimpleDateFormat("d.MM.YYYY");
                        break;
                    case "day":
                        sdf = new SimpleDateFormat("d");
                        break;
                    case "month":
                        sdf = new SimpleDateFormat("MMMM");
                        break;
                    case "year":
                        sdf = new SimpleDateFormat("YYYY");
                        break;
                    case "time":
                        sdf = new SimpleDateFormat("H:mm:ss");
                        break;
                    case "hour":
                        sdf = new SimpleDateFormat("H");
                        break;
                    case "minutes":
                        sdf = new SimpleDateFormat("m");
                        break;
                    case "seconds":
                        sdf = new SimpleDateFormat("s");
                        break;
                    default:
                        return;
                }

                requiredTime = sdf.format(date);
                ourMessage = String.format("Information for %s: %s", senderName, requiredTime);
                sendTextMessage(ourMessage);
            }

        }

    }
}
