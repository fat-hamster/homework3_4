package Server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyServer {
    public static final int PORT = 8081;
    public static final int TIMEOUT = 120;
    private List<ClientHandler> clients;
    private AuthService authService;
    private ExecutorService executorService;
    private static final Logger LOG = LogManager.getLogger(MyServer.class);

    public MyServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            LOG.info("Сервер запущен");
            authService = new BaseAuthService();
            authService.start();
            clients = new ArrayList<>();
            executorService = Executors.newCachedThreadPool();
            System.out.println("Сервер запущен");

            while (true) {
                System.out.println("Ожидаем подключение клиентов");
                Socket socket = serverSocket.accept();
                System.out.println("Клиент подключился");
                LOG.info("Клиент подключился");
                executorService.execute(new Thread(() -> new ClientHandler(this, socket, LOG)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeMyServer();
        }
    }

    public synchronized void sendMessageToClient(ClientHandler from, String nickTo, String msg) {
        for (ClientHandler client : clients) {
            if (client.getNick().equals(nickTo)) {
                //System.out.printf("Отправляем личное сообщение от %s, кому %s", from.getNick(), nickTo);
                String infoStr = "Отправляем личное сообщение от " + from.getNick() + ", кому " + nickTo;
                LOG.info(infoStr);
                Message message = new Message();
                message.setNick(from.getNick());
                message.setMessage(msg);
                client.sendMessage(message);
                return;
            }
        }
        String infoStr = "Клиент с ником " + nickTo +  "не подключен к чату";
        LOG.warn(infoStr);
        //System.out.printf("Клиент с ником %s не подключен к чату", nickTo);
        Message message = new Message();
        message.setMessage(infoStr);
        from.sendMessage(message);
    }

    public synchronized void broadcastMessage(Message message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public synchronized boolean isNickBusy(String nick) {
        for (ClientHandler client : clients) {
            if(nick.equals(client.getNick())) {
                return true;
            }
        }
        return false;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastClientList();
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientList();
    }

    private synchronized void broadcastClientList() {
        StringBuilder sb = new StringBuilder("/clients ");
        for (ClientHandler client : clients) {
            sb.append(client.getNick()).append(" ");
        }
        Message message = new Message();
        message.setMessage(sb.toString());
        broadcastMessage(message);
    }

    private void closeMyServer() {
        if(authService != null) {
            authService.stop();
        }
        executorService.shutdown();
    }
}