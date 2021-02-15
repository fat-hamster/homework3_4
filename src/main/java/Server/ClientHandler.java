package Server;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.*;

public class ClientHandler {
    private Socket socket;
    private MyServer myServer;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String nick;
    private ExecutorService executorService;

    public ClientHandler(MyServer myServer, Socket socket) {
        try {
            this.myServer = myServer;
            this.socket = socket;

            this.dataInputStream = new DataInputStream(socket.getInputStream());
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());

            executorService = Executors.newSingleThreadExecutor();
            Runnable auth = this::authentication;
            Future future = executorService.submit(auth);

            try {
                future.get(MyServer.TIMEOUT, TimeUnit.SECONDS);
                executorService.execute(this::readMessages);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                System.out.println("Connection timeout "+MyServer.TIMEOUT+" sec");
                closeConnection();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        Message message = new Message();
        myServer.unsubscribe(this);
        message.setMessage(nick + " вышел из чата");
        myServer.broadcastMessage(message);
        try {
            dataOutputStream.close();
            dataInputStream.close();
            socket.close();
            System.out.println(nick + ": сеанс завершен");
        } catch (IOException ignore) {
        }
        executorService.shutdown();
    }

    private void authentication() {
        while (true) {
            try {
                AuthMessage message = new Gson().fromJson(dataInputStream.readUTF(), AuthMessage.class);
                String nick = myServer.getAuthService().getNickByLoginAndPass(message.getLogin(), message.getPassword());
                if (nick != null && !myServer.isNickBusy(nick)) {
                    message.setAuthenticated(true);
                    message.setNick(nick);
                    this.nick = nick;

                    dataOutputStream.writeUTF(new Gson().toJson(message));
                    Message broadcastMsg = new Message();
                    broadcastMsg.setMessage(nick + " вошел в чат");
                    myServer.broadcastMessage(broadcastMsg);
                    myServer.subscribe(this);
                    return;
                } else {
                    message.setAuthenticated(false);
                    message.setNick("/wrong");
                    dataOutputStream.writeUTF(new Gson().toJson(message));
                }
            } catch (IOException ignored) {
            }
        }
    }

    private void readMessages() {
        while (true) {
            Message message = null;
            try {
                message = new Gson().fromJson(dataInputStream.readUTF(), Message.class);
            } catch (IOException e) {
                closeConnection();
            }
            if(message != null) {
                message.setNick(nick);

                System.out.println(message);
                if (!message.getMessage().startsWith("/")) {
                    myServer.broadcastMessage(message);
                    continue;
                }
                String[] command = message.getMessage().split("\\s");
                switch (command[0]) {
                    case "/end": {
                        closeConnection();
                        return;
                    }
                    case "/w": {
                        if (command.length < 3) {
                            Message msg = new Message();
                            msg.setMessage("Не хватает параметров, необходимо отправить команду следующего вида: /w <ник> <сообщение>");
                            this.sendMessage(msg);
                        }
                        String nick = command[1];
                        StringBuilder sbMessage = new StringBuilder();
                        for (int i = 2; i < command.length; i++) {
                            sbMessage.append(command[i]);
                            sbMessage.append(" ");
                        }
                        myServer.sendMessageToClient(this, nick, sbMessage.toString());
                        break;
                    }
                    case "/ch": {
                        if (command.length != 2) {
                            Message msg = new Message();
                            msg.setMessage("Необходимо отправить команду следующего вида:\n/ch <новый_ник>");
                            this.sendMessage(msg);
                            break;
                        }
                        if (!myServer.getAuthService().changeNick(nick, command[1])) {
                            Message msg = new Message();
                            msg.setMessage(myServer.getAuthService().getError());
                            this.sendMessage(msg);
                            break;
                        }
                        Message msg = new Message();
                        msg.setMessage(this.nick + " теперь известен как " + command[1]);
                        myServer.broadcastMessage(msg);
                        this.nick = command[1];
                        break;
                    }
                    case "/h": {
                        Message msg = new Message();
                        String sb = "\n/w <nick> <message> \t Отправить личное сообщение\n" +
                                "/ch <new_nick> \t Сменить ник (если свободен)\n" +
                                "/h \t\t Это сообщение\n" +
                                "/end \t\t Завершить сеанс\n";
                        msg.setMessage(sb);
                        this.sendMessage(msg);
                        break;
                    }
                }
            }
        }
    }

    public void sendMessage(Message message) {
        try {
            dataOutputStream.writeUTF(new Gson().toJson(message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNick() {
        return nick;
    }
}
