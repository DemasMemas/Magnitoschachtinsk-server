import java.util.ArrayList;

public class CommandHandler {
    private static int lastID = 0;

    public static void handCommand(ArrayList<String> commandList, TCPConnection tcpConnection){
        switch (commandList.get(0)) {
            case "getGames" -> tcpConnection.sendString(MainServer.getSessions());
            case "createGame" -> {
                Game tempGame = new Game(lastID, tcpConnection, commandList.get(1), commandList.get(2));
                MainServer.addNewSession(tempGame);
                tempGame.setCommand("sendMsg," + tempGame.getSessionID() + ","
                        + tempGame.getFirstPlayer() + ",Game created with ID: " + lastID++);
            }
            case "joinGame" -> {
                Game tempGame = MainServer.getGameByID(Integer.parseInt(commandList.get(3)));
                if (tempGame.getPassword().equals(commandList.get(2)) && !tempGame.isStarted()){
                    tempGame.setSecondPlayerConnection(tcpConnection);
                    tempGame.setSecondPlayer(commandList.get(1));
                    tempGame.setStarted(true);
                    tempGame.setCommand("sendMsg," + tempGame.getSessionID() + ","
                            + tempGame.getSecondPlayer() + ",Game joined with ID: " + tempGame.getSessionID());
                    tcpConnection.sendString("acceptJoin");
                }
            }
        }
    }

    // структура команды в игре
    // название команды, номер игры, имя отдавшего команду игрока, прочие параметры
    public static void handGameCommand(ArrayList<String> commandList){
        switch (commandList.get(0)) {
            case "dropGame" -> MainServer.dropSession(MainServer.getGameByID(Integer.parseInt(commandList.get(1))));
            case "setNewCommand" -> MainServer.getGameByID(Integer.parseInt(commandList.get(1))).setCommand(String.join(",", commandList));
            case "sendMsg" -> {
                try {
                    MainServer.getGameByID(Integer.parseInt(commandList.get(1))).getFirstPlayerConnection().sendString("writeChatMsg," + commandList.get(2) + "," + commandList.get(3));
                    MainServer.getGameByID(Integer.parseInt(commandList.get(1))).getSecondPlayerConnection().sendString("writeChatMsg," + commandList.get(2) + "," + commandList.get(3));
                } catch (NullPointerException ignored) { }
            }
        }
    }
}
