import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;

public class MainServer implements TCPConnectionListener {
    static ArrayList<Game> sessionList = new ArrayList<>();
    public static void main(String[] args) {
        new MainServer();
    }

    private final ArrayList<TCPConnection> connections = new ArrayList<>();

    private MainServer() {
        System.out.println("Server running...");
        Runnable task = () -> {
            while (true) gameLoop();
        };
        new Thread(task).start();
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            while (true) {
                try {
                    new TCPConnection(this, serverSocket.accept());
                } catch (IOException e) {
                    System.out.println("TCPConnection exception: " + e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        connections.add(tcpConnection);
        System.out.println("New connection: " + tcpConnection.toString());
    }

    @Override
    public synchronized void onReceiveString(TCPConnection tcpConnection, String value) {
        System.out.println(value);
        // обработать входящее сообщение
        ArrayList<String> commandList = new ArrayList<>();
        Collections.addAll(commandList, value.split(","));
        CommandHandler.handCommand(commandList, tcpConnection);
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        System.out.println(tcpConnection.toString() + " disconnected");
        connections.remove(tcpConnection);
    }

    @Override
    public void onException(TCPConnection tcpConnection, IOException e) { System.out.println("TCPConnection exception: " + e); }

    public synchronized void gameLoop(){
        for(Game currentGame:sessionList){
            ArrayList<String> commandList = new ArrayList<>();
            Collections.addAll(commandList, currentGame.getCommand().split(","));
            CommandHandler.handGameCommand(commandList);
        }
    }

    public static void addNewSession(Game tempGame){
        sessionList.add(tempGame);
    }

    public static void dropSession(Game tempGame){
        sessionList.remove(tempGame);
    }

    public static Game getGameByID(int ID){
        for (Game tempGame:sessionList) {
            if (tempGame.getSessionID() == ID) return tempGame;
        }
        return null;
    }

    public static String getSessions(){
        StringBuilder tempSB = new StringBuilder();
        tempSB.append("games,");
        for (Game tempGame:sessionList)
            tempSB.append(tempGame.toString());
        tempSB.deleteCharAt(tempSB.length() - 1);
        return tempSB.toString();
    }
}