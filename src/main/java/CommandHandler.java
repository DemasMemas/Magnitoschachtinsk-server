import java.sql.SQLException;
import java.util.ArrayList;

public class CommandHandler {
    private static int lastID = 0;

    public static void handCommand(ArrayList<String> commandList, TCPConnection tcpConnection) throws SQLException {
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
                Game tempGame = MainServer.getGameByID(Integer.parseInt(commandList.get(1)));
                tempGame.changeTurn();
                if(tempGame.getPlayerTurn() == 0)
                    tempGame.getFirstPlayerConnection().sendString("takeTurn");
                else tempGame.getSecondPlayerConnection().sendString("takeTurn");
            }
            case "takeCard" -> MainServer.getGameByID(Integer.parseInt(commandList.get(1))).takeCard(commandList.get(2));
            case "takeCardWithType" -> MainServer.getGameByID(Integer.parseInt(commandList.get(1))).takeCardWithType(commandList.get(2), commandList.get(3));
            case "takeCardNotFromDeck" -> MainServer.getGameByID(Integer.parseInt(commandList.get(1))).takeCardNotFromDeck(commandList.get(2));
            case "playCard" -> MainServer.getGameByID(Integer.parseInt(commandList.get(1))).
                    playCard(commandList.get(2), commandList.get(3), commandList.get(4));
            case "cardFromHand" -> {
                Game tempGame = MainServer.getGameByID(Integer.parseInt(commandList.get(1)));
                if (commandList.get(2).equals(tempGame.getFirstPlayer())){
                    tempGame.getSecondPlayerConnection().sendString("enemyCardFromHand");
                    tempGame.decrementHandSize(tempGame.getFirstPlayer());
                    tempGame.cancelRoundEnd(tempGame.getFirstPlayer());
                }
                else {
                    tempGame.getFirstPlayerConnection().sendString("enemyCardFromHand");
                    tempGame.decrementHandSize(tempGame.getSecondPlayer());
                    tempGame.cancelRoundEnd(tempGame.getSecondPlayer());
                }
            }
            case "endTurn" -> MainServer.getGameByID(Integer.parseInt(commandList.get(1))).checkRoundEnd(commandList.get(2));
            case "newObjective" -> {
                Game tempGame = MainServer.getGameByID(Integer.parseInt(commandList.get(1)));
                if (commandList.get(2).equals(tempGame.getFirstPlayer()))
                    tempGame.getSecondPlayerConnection().sendString("enemyNewObjective," + commandList.get(3));
                else tempGame.getFirstPlayerConnection().sendString("enemyNewObjective," + commandList.get(3));
            }
            case "updateEnemyVictoryPoints" -> {
                Game tempGame = MainServer.getGameByID(Integer.parseInt(commandList.get(1)));
                if (commandList.get(2).equals(tempGame.getFirstPlayer()))
                    tempGame.getSecondPlayerConnection().sendString("updateEnemyVictoryPoints," +
                            commandList.get(3) + "," + commandList.get(4));
                else tempGame.getFirstPlayerConnection().sendString("updateEnemyVictoryPoints," +
                        commandList.get(3) + "," + commandList.get(4));
            }
            case "updateEnemyObjectiveDuration" -> {
                Game tempGame = MainServer.getGameByID(Integer.parseInt(commandList.get(1)));
                if (commandList.get(2).equals(tempGame.getFirstPlayer()))
                    tempGame.getSecondPlayerConnection().sendString("updateEnemyObjectiveDuration," +
                            commandList.get(3));
                else tempGame.getFirstPlayerConnection().sendString("updateEnemyObjectiveDuration," +
                        commandList.get(3));
            }
            case "removeKilledAlly" -> {
                Game tempGame = MainServer.getGameByID(Integer.parseInt(commandList.get(1)));
                if (commandList.get(2).equals(tempGame.getFirstPlayer()))
                    tempGame.getSecondPlayerConnection().sendString("removeKilledAlly," +
                            commandList.get(3));
                else tempGame.getFirstPlayerConnection().sendString("removeKilledAlly," +
                        commandList.get(3));
            }
            case "removeKilledEnemy" -> {
                Game tempGame = MainServer.getGameByID(Integer.parseInt(commandList.get(1)));
                if (commandList.get(2).equals(tempGame.getFirstPlayer()))
                    tempGame.getSecondPlayerConnection().sendString("removeKilledEnemy," +
                            commandList.get(3));
                else tempGame.getFirstPlayerConnection().sendString("removeKilledEnemy," +
                        commandList.get(3));
            }
            case "changeEnemyPersonStatus" -> {
                Game tempGame = MainServer.getGameByID(Integer.parseInt(commandList.get(1)));
                if (commandList.get(2).equals(tempGame.getFirstPlayer()))
                    tempGame.getSecondPlayerConnection().sendString("changeEnemyPersonStatus," +
                            commandList.get(3) + "," + commandList.get(4));
                else tempGame.getFirstPlayerConnection().sendString("changeEnemyPersonStatus," +
                        commandList.get(3) + "," + commandList.get(4));
            }
            case "changeAllyPersonStatus" -> {
                Game tempGame = MainServer.getGameByID(Integer.parseInt(commandList.get(1)));
                if (commandList.get(2).equals(tempGame.getFirstPlayer()))
                    tempGame.getSecondPlayerConnection().sendString("changeAllyPersonStatus," +
                            commandList.get(3) + "," + commandList.get(4));
                else tempGame.getFirstPlayerConnection().sendString("changeAllyPersonStatus," +
                        commandList.get(3) + "," + commandList.get(4));
            }
            case "updateDefenders" -> {
                Game tempGame = MainServer.getGameByID(Integer.parseInt(commandList.get(1)));
                if (commandList.get(2).equals(tempGame.getFirstPlayer()))
                    tempGame.getSecondPlayerConnection().sendString("updateDefenders," +
                            commandList.get(3) + "," + commandList.get(4) + "," + commandList.get(5));
                else tempGame.getFirstPlayerConnection().sendString("updateDefenders," +
                        commandList.get(3) + "," + commandList.get(4) + "," + commandList.get(5));
            }
            case "updateStatuses" -> {
                Game tempGame = MainServer.getGameByID(Integer.parseInt(commandList.get(1)));
                if (commandList.get(2).equals(tempGame.getFirstPlayer()))
                    tempGame.getSecondPlayerConnection().sendString("updateStatuses," + commandList.get(3));
                else tempGame.getFirstPlayerConnection().sendString("updateStatuses," + commandList.get(3));
            }
            case "updateMinedUp" -> {
                Game tempGame = MainServer.getGameByID(Integer.parseInt(commandList.get(1)));
                if (commandList.get(2).equals(tempGame.getFirstPlayer()))
                    tempGame.getSecondPlayerConnection().sendString("updateMinedUp," + commandList.get(3));
                else tempGame.getFirstPlayerConnection().sendString("updateMinedUp," + commandList.get(3));
            }
            case "endGame" -> MainServer.getGameByID(Integer.parseInt(commandList.get(1))).endGame(commandList.get(2),
                    commandList.get(3), Integer.parseInt(commandList.get(4)), Integer.parseInt(commandList.get(5)));
            case "endStatus" -> MainServer.getGameByID(Integer.parseInt(commandList.get(1))).finallyEndGame
                    (commandList.get(2), Integer.parseInt(commandList.get(3)), Integer.parseInt(commandList.get(4)),
                            Integer.parseInt(commandList.get(5)));
        }
    }

    // структура команды в игре
    // название команды, номер игры, имя отдавшего команду игрока, прочие параметры
    public static void handGameCommand(ArrayList<String> commandList) {
        try {
            Game tempGame = MainServer.getGameByID(Integer.parseInt(commandList.get(1)));
            switch (commandList.get(0)) {
                case "dropGame" -> MainServer.dropSession(MainServer.getGameByID(Integer.parseInt(commandList.get(1))));
                case "setNewCommand" -> MainServer.getGameByID(Integer.parseInt(commandList.get(1))).setCommand(String.join(",", commandList));
                case "sendMsg" -> {
                    tempGame.getFirstPlayerConnection().sendString("writeChatMsg," + commandList.get(2) + "," + commandList.get(3));
                    tempGame.getSecondPlayerConnection().sendString("writeChatMsg," + commandList.get(2) + "," + commandList.get(3));
                }
                case "closeGame" -> MainServer.dropSession(tempGame);
                case "showFirstPlayer" -> {
                    tempGame.getFirstPlayerConnection().sendString("showFirstPlayer," + commandList.get(2));
                    tempGame.getSecondPlayerConnection().sendString("showFirstPlayer," + commandList.get(2));
                }
                case "changeTime" -> {
                    tempGame.getFirstPlayerConnection().sendString("changeTime," + commandList.get(2));
                    tempGame.getSecondPlayerConnection().sendString("changeTime," + commandList.get(2));
                }
            }
        } catch (NullPointerException ignored) {}
    }
}
