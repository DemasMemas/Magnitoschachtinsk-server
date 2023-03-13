import java.util.ArrayList;

public class CommandHandler {
    private static int lastID = 0;

    public static void handCommand(ArrayList<String> commandList, TCPConnection tcpConnection) {
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
                if (tempGame.getPassword().equals(commandList.get(2)) && !tempGame.isStarted()) {
                    tempGame.setSecondPlayerConnection(tcpConnection);
                    tempGame.setSecondPlayer(commandList.get(1));
                    tempGame.setStarted(true);
                    tempGame.setCommand("sendMsg," + tempGame.getSessionID() + ","
                            + tempGame.getSecondPlayer() + ",Game joined with ID: " + tempGame.getSessionID());
                    tcpConnection.sendString("acceptJoin," + tempGame.getSessionID());
                    // команда о начале новой игры игрокам
                    tempGame.getFirstPlayerConnection().sendString("startGame," + tempGame.getSecondPlayer());
                    tempGame.getSecondPlayerConnection().sendString("startGame," + tempGame.getFirstPlayer());
                }
            }
            case "chatMsg" -> {
                Game tempGame = MainServer.getGameByID(Integer.parseInt(commandList.get(2)));
                tempGame.setCommand("sendMsg," + tempGame.getSessionID() + ","
                        + commandList.get(1) + "," + commandList.get(3));
            }
            case "changeTurn" -> {
                MainServer.getGameByID(Integer.parseInt(commandList.get(1))).changeTurn();
                if(MainServer.getGameByID(Integer.parseInt(commandList.get(1))).getPlayerTurn() == 0)
                    MainServer.getGameByID(Integer.parseInt(commandList.get(1))).getFirstPlayerConnection().sendString("takeTurn");
                else MainServer.getGameByID(Integer.parseInt(commandList.get(1))).getSecondPlayerConnection().sendString("takeTurn");
            }
            case "takeCard" -> MainServer.getGameByID(Integer.parseInt(commandList.get(1))).takeCard(commandList.get(2));
            case "playCard" -> MainServer.getGameByID(Integer.parseInt(commandList.get(1))).
                    playCard(commandList.get(2), commandList.get(3), commandList.get(4));
            case "cardFromHand" -> {
                if (commandList.get(2).equals(MainServer.getGameByID(Integer.parseInt(commandList.get(1))).getFirstPlayer()))
                    MainServer.getGameByID(Integer.parseInt(commandList.get(1))).getSecondPlayerConnection().sendString("enemyCardFromHand");
                else MainServer.getGameByID(Integer.parseInt(commandList.get(1))).getFirstPlayerConnection().sendString("enemyCardFromHand");
            }
            case "endTurn" -> MainServer.getGameByID(Integer.parseInt(commandList.get(1))).checkRoundEnd(commandList.get(2));
        }
    }

    // структура команды в игре
    // название команды, номер игры, имя отдавшего команду игрока, прочие параметры
    public static void handGameCommand(ArrayList<String> commandList) {
        try {
            switch (commandList.get(0)) {
                case "dropGame" -> MainServer.dropSession(MainServer.getGameByID(Integer.parseInt(commandList.get(1))));
                case "setNewCommand" -> MainServer.getGameByID(Integer.parseInt(commandList.get(1))).setCommand(String.join(",", commandList));
                case "sendMsg" -> {
                    MainServer.getGameByID(Integer.parseInt(commandList.get(1))).getFirstPlayerConnection().sendString("writeChatMsg," + commandList.get(2) + "," + commandList.get(3));
                    MainServer.getGameByID(Integer.parseInt(commandList.get(1))).getSecondPlayerConnection().sendString("writeChatMsg," + commandList.get(2) + "," + commandList.get(3));
                }
                case "closeGame" -> {
                    MainServer.getGameByID(Integer.parseInt(commandList.get(1))).getFirstPlayerConnection().sendString("closeGame");
                    MainServer.getGameByID(Integer.parseInt(commandList.get(1))).getSecondPlayerConnection().sendString("closeGame");
                    MainServer.dropSession(MainServer.getGameByID(Integer.parseInt(commandList.get(1))));
                }
                case "showFirstPlayer" -> {
                    MainServer.getGameByID(Integer.parseInt(commandList.get(1))).getFirstPlayerConnection().sendString("showFirstPlayer," + commandList.get(2));
                    MainServer.getGameByID(Integer.parseInt(commandList.get(1))).getSecondPlayerConnection().sendString("showFirstPlayer," + commandList.get(2));
                }
                case "changeTime" -> {
                    MainServer.getGameByID(Integer.parseInt(commandList.get(1))).getFirstPlayerConnection().sendString("changeTime," + commandList.get(2));
                    MainServer.getGameByID(Integer.parseInt(commandList.get(1))).getSecondPlayerConnection().sendString("changeTime," + commandList.get(2));
                }
            }
        } catch (NullPointerException ignored) {}
    }
}
