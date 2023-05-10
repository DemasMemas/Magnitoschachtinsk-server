import GameObjects.Building;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class Game {
    private final String firstPlayer, password;
    private String secondPlayer;
    private final int sessionID;
    private final TCPConnection firstPlayerConnection;
    private TCPConnection secondPlayerConnection;
    private String lastCommand;
    private final ArrayList<String> commandQuery;
    private int playerTurn = 0, turnTime;
    private final Random random = new Random();
    private final HashMap<Integer,Card> firstPlayerDeck = new HashMap<>(),  secondPlayerDeck = new HashMap<>();
    private boolean firstPlayerEndRound = false, secondPlayerEndRound = false, started;
    private int firstPlayerHandSize = 0, secondPlayerHandSize = 0;
    private String firstPlayerEndStatus = "", secondPlayerEndStatus = "";
    private int firstPlayerVP = 0, secondPlayerVP = 0;
    private int firstPlayerPeopleAndBuildings = 0, secondPlayerPeopleAndBuildings = 0;

    Connection conn = new DatabaseHandler().getConnection();

    public Game(int ID, TCPConnection firstPlayerConnection, String firstPlayer, String password){
        this.sessionID = ID;
        this.firstPlayerConnection = firstPlayerConnection;
        this.firstPlayer = firstPlayer;
        if (password.equals("noPassword"))
            this.password = "";
        else
            this.password = password;

        lastCommand = "";
        commandQuery = new ArrayList<>();

        fillFirstPlayerDeck();
    }

    public String getCommand(){
        String tempReturnCommand = lastCommand;
        lastCommand = "";
        checkCommandQuery();
        return tempReturnCommand;
    }

    public int getSessionID(){
        return sessionID;
    }

    public synchronized void setCommand(String newCommand){
        if (!lastCommand.equals(""))
        lastCommand = newCommand;
        else commandQuery.add(newCommand);
    }

    public String getFirstPlayer() {
        return firstPlayer;
    }

    public TCPConnection getFirstPlayerConnection() {
        return firstPlayerConnection;
    }

    public String getSecondPlayer() { return secondPlayer; }

    public String getPassword() { return password; }

    public TCPConnection getSecondPlayerConnection() { return secondPlayerConnection; }

    public void setSecondPlayerConnection(TCPConnection secondPlayerConnection) {
        this.secondPlayerConnection = secondPlayerConnection; }

    public void setSecondPlayer(String secondPlayer) {
        this.secondPlayer = secondPlayer;
        makeDeck(secondPlayer, secondPlayerDeck);
    }

    private void makeDeck(String player, HashMap<Integer, Card> playerDeck) {
        try {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT decks.cards FROM decks,users " +
                    "WHERE decks.deck_id = users.active_deck AND users.nickname = ?");
            preparedStatement.setString(1, player);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();

            for (String value : resultSet.getString("cards").split(","))
                playerDeck.put(Integer.parseInt(value.split(":")[0]),
                        getUserCardByID(Integer.parseInt(value.split(":")[0]),
                                Integer.parseInt(value.split(":")[1])));
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public boolean isStarted() { return started; }

    public void setStarted(boolean started) {
        this.started = started;
        if (started) startGame();
    }

    public synchronized void checkCommandQuery(){
        if (commandQuery.size() != 0 && lastCommand.equals("")){
            lastCommand = commandQuery.get(0);
            commandQuery.remove(0);
        }
    }

    @Override
    public String toString(){
        if (secondPlayer != null)
        return firstPlayer + "," + secondPlayer + "," + sessionID + "," + getPassword().length() + ",";
        else return firstPlayer + ", ," + sessionID + "," + getPassword().length() + ",";
    }

    public void gameLoop(){ setCommand("changeTime," + sessionID + "," + --turnTime); }

    public void startGame(){
        //выбор первого игрока, раздача карт
        playerTurn = random.nextInt(2);
        if (playerTurn == 0) setCommand("showFirstPlayer," + sessionID + "," + firstPlayer);
        else setCommand("showFirstPlayer," + sessionID + "," + secondPlayer);

        // Запуск таймера на ход
        turnTime = 60;
        Runnable task = () -> { while (isStarted()) try {
            Thread.sleep(1000);
            gameLoop();
        } catch (InterruptedException e) {
            e.printStackTrace(); }};
        new Thread(task).start();
    }

    public void changeTurn(){
        if (turnTime != 60){
            turnTime = 60;
            playerTurn = Math.abs(playerTurn-1);
        }
    }

    public void endGame(String playerName, String endStatus, int victoryPoints, int buildingCounter){
        if (isStarted()) {
            setStarted(false);
            setCommand("closeGame");
        } else return;
        if (playerName.equals(firstPlayer)){
            firstPlayerEndStatus = endStatus;
            firstPlayerVP = victoryPoints;
            firstPlayerPeopleAndBuildings = buildingCounter;
            secondPlayerConnection.sendString("sendEndStatus");
        }
        else{
            secondPlayerEndStatus = endStatus;
            secondPlayerVP = victoryPoints;
            secondPlayerPeopleAndBuildings = buildingCounter;
            firstPlayerConnection.sendString("sendEndStatus");
        }
    }
    public void finallyEndGame(String playerName, int completedObjectives, int victoryPoints, int buildingCounter) throws SQLException {
        if (playerName.equals(firstPlayer)) checkWinner(completedObjectives, victoryPoints, buildingCounter,
                secondPlayerEndStatus, secondPlayerVP, secondPlayerPeopleAndBuildings, firstPlayer, secondPlayer);
        else checkWinner(completedObjectives, victoryPoints, buildingCounter, firstPlayerEndStatus, firstPlayerVP,
                firstPlayerPeopleAndBuildings, secondPlayer, firstPlayer);
    }

    private void checkWinner(int completedObjectives, int victoryPoints, int buildingCounter, String enemyPlayerEndStatus, int enemyPlayerVP, int enemyPlayerPeopleAndBuildings, String checkedPlayer, String enemyPlayer) throws SQLException {
        if (enemyPlayerEndStatus.equals("victory")){
            if (victoryPoints == enemyPlayerVP){
                if (buildingCounter == enemyPlayerPeopleAndBuildings) {
                    draw();
                    return;
                }
                if (buildingCounter > enemyPlayerPeopleAndBuildings){
                    win(checkedPlayer);
                    lose(enemyPlayer);
                    changeRating(checkedPlayer, enemyPlayer);
                    return;
                }
                win(enemyPlayer);
                lose(checkedPlayer);
                changeRating(enemyPlayer, checkedPlayer);
                return;
            }
            if (victoryPoints > enemyPlayerVP){
                win(checkedPlayer);
                lose(enemyPlayer);
                changeRating(checkedPlayer, enemyPlayer);
                return;
            }
            win(enemyPlayer);
            lose(checkedPlayer);
            changeRating(enemyPlayer, checkedPlayer);
        } else {
            if (completedObjectives == 3 || victoryPoints > enemyPlayerVP){
                win(checkedPlayer);
                lose(enemyPlayer);
                changeRating(checkedPlayer, enemyPlayer);
            } else if (victoryPoints == enemyPlayerVP){
                if (buildingCounter > enemyPlayerPeopleAndBuildings){
                    win(checkedPlayer);
                    lose(enemyPlayer);
                    changeRating(checkedPlayer, enemyPlayer);
                    return;
                }
                if (buildingCounter == enemyPlayerPeopleAndBuildings){
                    draw();
                    return;
                }
                win(enemyPlayer);
                lose(checkedPlayer);
                changeRating(enemyPlayer, checkedPlayer);
            }
        }
    }

    public int getPlayerRating(String playerName) throws SQLException {
        PreparedStatement preparedStatement;
        ResultSet resultSet;
        preparedStatement = conn.prepareStatement("SELECT rating FROM users WHERE nickname = ?");
        preparedStatement.setString(1, playerName);
        resultSet = preparedStatement.executeQuery();
        resultSet.next();
        return resultSet.getInt("rating");
    }

    public void draw() throws SQLException {
        PreparedStatement preparedStatement;
        updateLevelAndDogtags(firstPlayer);
        updateLevelAndDogtags(secondPlayer);

        int firstRating = getPlayerRating(firstPlayer);
        int secondRating = getPlayerRating(secondPlayer);
        int average = (firstRating + secondRating) / 2;
        if (firstRating > average){
            firstRating -= (average / 20);
            secondRating += (average / 20);
        } else if (secondRating > average){
            firstRating += (average / 20);
            secondRating -= (average / 20);
        }
        if (firstRating > 1000) firstRating = 1000;
        if (secondRating > 1000) secondRating = 1000;
        if (firstRating <= 0) firstRating = 1;
        if (secondRating <= 0) secondRating = 1;
        preparedStatement = conn.prepareStatement("UPDATE users SET rating = ? WHERE nickname = ?");
        preparedStatement.setInt(1, firstRating);
        preparedStatement.setString(2, firstPlayer);
        preparedStatement.executeUpdate();
        preparedStatement = conn.prepareStatement("UPDATE users SET rating = ? WHERE nickname = ?");
        preparedStatement.setInt(1, secondRating);
        preparedStatement.setString(2, secondPlayer);
        preparedStatement.executeUpdate();
        firstPlayerConnection.sendString("endScreen,draw");
        secondPlayerConnection.sendString("endScreen,draw");
    }

    private void updateLevelAndDogtags(String player) throws SQLException {
        PreparedStatement preparedStatement;
        preparedStatement = conn.prepareStatement("UPDATE users SET dogtags = dogtags + 10, " +
                "experience = experience + 2 WHERE nickname = ?");
        preparedStatement.setString(1, player);
        preparedStatement.executeUpdate();
        updateLevel(player);
    }

    public void updateLevel(String player){
        try {
            PreparedStatement preparedStatement;
            ResultSet resultSet;
            preparedStatement = conn.prepareStatement("SELECT * FROM users WHERE nickname = ?");
            preparedStatement.setString(1, player);
            resultSet = preparedStatement.executeQuery();
            resultSet.next();
            if (resultSet.getInt("experience") / 25 > resultSet.getInt("level")){
                preparedStatement = conn.prepareStatement("UPDATE users SET experience = experience - users.level * 25, " +
                        "level = level + 1 WHERE nickname = ?");
                preparedStatement.setString(1, player);
                preparedStatement.executeUpdate();
            }
        } catch (Exception ignored){}
    }

    public void changeRating(String winner, String loser) throws SQLException {
        PreparedStatement preparedStatement;
        int winnerRating = getPlayerRating(winner);
        int loserRating = getPlayerRating(loser);
        if (Math.abs(winnerRating - loserRating) >= 200) {
            if (winnerRating < loserRating){
                int result = Math.abs(winnerRating - loserRating) / 8;
                preparedStatement = changeRating(winner, winnerRating, loserRating, result);
            } else {
                if (winnerRating + 1 > 1000) winnerRating = 1000 - 1;
                if (loserRating - 1 <= 0) loserRating = 1 + 1;
                preparedStatement = conn.prepareStatement("UPDATE users SET rating = ? WHERE nickname = ?");
                preparedStatement.setInt(1, winnerRating + 1);
                preparedStatement.setString(2, winner);
                preparedStatement.executeUpdate();
                preparedStatement = conn.prepareStatement("UPDATE users SET rating = ? WHERE nickname = ?");
                preparedStatement.setInt(1, loserRating - 1);
            }
        } else {
            int preResult = ((winnerRating + loserRating + Math.abs(winnerRating - loserRating))) / 2;
            if (winnerRating >= loserRating) preResult /= Math.pow(10, String.valueOf(winnerRating).length() - 1);
            else preResult /= Math.pow(10, String.valueOf(loserRating).length() - 1);

            preparedStatement = changeRating(winner, winnerRating, loserRating, preResult);
        }
        preparedStatement.setString(2, loser);
        preparedStatement.executeUpdate();
        if (winner.equals(firstPlayer)){
            firstPlayerConnection.sendString("endScreen,victory");
            secondPlayerConnection.sendString("endScreen,lose");
        } else {
            firstPlayerConnection.sendString("endScreen,lose");
            secondPlayerConnection.sendString("endScreen,victory");
        }
    }

    private PreparedStatement changeRating(String winner, int winnerRating, int loserRating, int result) throws SQLException {
        PreparedStatement preparedStatement;
        if (winnerRating + result > 1000) winnerRating = 1000 - result;
        if (loserRating - result <= 0) loserRating = 1 + result;
        preparedStatement = conn.prepareStatement("UPDATE users SET rating = ? WHERE nickname = ?");
        preparedStatement.setInt(1, winnerRating + result);
        preparedStatement.setString(2, winner);
        preparedStatement.executeUpdate();
        preparedStatement = conn.prepareStatement("UPDATE users SET rating = ? WHERE nickname = ?");
        preparedStatement.setInt(1, loserRating - result);
        return preparedStatement;
    }

    public void lose(String playerName) throws SQLException {
        PreparedStatement preparedStatement = conn.prepareStatement("UPDATE users SET dogtags = dogtags + 5, " +
                "experience = experience + 1 WHERE nickname = ?");
        preparedStatement.setString(1, playerName);
        preparedStatement.executeUpdate();
        updateLevel(playerName);
    }
    public void win(String playerName) throws SQLException {
        PreparedStatement preparedStatement = conn.prepareStatement("UPDATE users SET dogtags = dogtags + 15, " +
                "experience = experience + 3 WHERE nickname = ?");
        preparedStatement.setString(1, playerName);
        preparedStatement.executeUpdate();
        updateLevel(playerName);
    }

    public int getPlayerTurn(){ return playerTurn; }

    public Card getUserCardByID(int id, int currentAmount) {
        PreparedStatement cardPreparedStatement;
        try {
            cardPreparedStatement = conn.prepareStatement("SELECT * FROM cards WHERE card_id = ?");
            cardPreparedStatement.setInt(1, id);
            ResultSet cardResultSet = cardPreparedStatement.executeQuery();
            cardResultSet.next();
            return new Card(cardResultSet.getInt("card_id"),
                    cardResultSet.getString("name"),
                    cardResultSet.getString("image_path"),
                    cardResultSet.getString("type"),
                    cardResultSet.getString("description"),
                    cardResultSet.getInt("deck_limit"),
                    cardResultSet.getString("cost_type"),
                    cardResultSet.getInt("health_status"),
                    cardResultSet.getString("effects"),
                    cardResultSet.getInt("price"),
                    cardResultSet.getInt("rareness"),
                    cardResultSet.getInt("attack"),
                    cardResultSet.getInt("defence"),
                    cardResultSet.getInt("stealth"),
                    currentAmount);
        } catch (SQLException exception) {
            return null;
        }
    }

    public void fillFirstPlayerDeck(){
        makeDeck(firstPlayer, firstPlayerDeck);
    }

    public void takeCard(String playerName){
        if (playerName.equals(firstPlayer) &&!firstPlayerDeck.isEmpty() && firstPlayerHandSize < 9){
            firstPlayerConnection.sendString("takeCard," + takeCardFromDeck(firstPlayerDeck).card_id);
            secondPlayerConnection.sendString("enemyTakeCard");
            firstPlayerHandSize++;
        }
        else {
            if(!secondPlayerDeck.isEmpty() && secondPlayerHandSize < 9){
                secondPlayerConnection.sendString("takeCard," + takeCardFromDeck(secondPlayerDeck).card_id);
                firstPlayerConnection.sendString("enemyTakeCard");
                secondPlayerHandSize++;
            }
        }
    }

    public void takeCardWithType(String playerName, String cardType){
        if (playerName.equals(firstPlayer) &&!firstPlayerDeck.isEmpty() && firstPlayerHandSize < 9){
            firstPlayerConnection.sendString("takeCard," + takeCardWithTypeFromDeck(firstPlayerDeck, cardType).card_id);
            secondPlayerConnection.sendString("enemyTakeCard");
            firstPlayerHandSize++;
        }
        else {
            if(!secondPlayerDeck.isEmpty() && secondPlayerHandSize < 9){
                secondPlayerConnection.sendString("takeCard," + takeCardWithTypeFromDeck(secondPlayerDeck, cardType).card_id);
                firstPlayerConnection.sendString("enemyTakeCard");
                secondPlayerHandSize++;
            }
        }
    }
    public Card takeCardWithTypeFromDeck(HashMap<Integer, Card> deck, String cardType){
        Object[] cardNumbs = deck.keySet().toArray();
        Card tempCard;
        int cardNumb;
        do{
            Object key = cardNumbs[random.nextInt(cardNumbs.length)];
            cardNumb = Integer.parseInt(String.valueOf(key));
            tempCard = deck.get(cardNumb);
        } while (!tempCard.type.equals(cardType));
        tempCard.current_amount -= 1;
        if (tempCard.current_amount <= 0) deck.remove(cardNumb);
        else deck.replace(cardNumb, tempCard);
        return tempCard;
    }

    public void takeCardNotFromDeck(String playerName){
        if (playerName.equals(firstPlayer)){
            secondPlayerConnection.sendString("enemyTakeCard");
            firstPlayerHandSize++;
        }
        else {
            firstPlayerConnection.sendString("enemyTakeCard");
            secondPlayerHandSize++;
        }
    }

    public Card takeCardFromDeck(HashMap<Integer, Card> deck){
        Object[] cardNumbs = deck.keySet().toArray();
        Object key = cardNumbs[random.nextInt(cardNumbs.length)];
        int cardNumb = Integer.parseInt(String.valueOf(key));
        Card tempCard = deck.get(cardNumb);
        tempCard.current_amount -= 1;
        if (tempCard.current_amount == 0) deck.remove(cardNumb);
        else deck.replace(cardNumb, tempCard);
        return tempCard;
    }

    public void playCard(String playerName, String cardType, String cardInfo){
        switch (cardType){
            case "person" -> {
                if (playerName.equals(firstPlayer))
                    secondPlayerConnection.sendString("enemyCard,people," + cardInfo);
                else firstPlayerConnection.sendString("enemyCard,people," + cardInfo);
            }
            case "building" -> {
                String[] info = cardInfo.split(" ");
                Card tempCard = getUserCardByID(Integer.parseInt(info[0]), 1);
                tempCard.building = new Building(Integer.parseInt(info[0]));
                if (playerName.equals(firstPlayer))
                    secondPlayerConnection.sendString("enemyCard,building," + tempCard.getBuildingCard());
                else firstPlayerConnection.sendString("enemyCard,building," + tempCard.getBuildingCard());
            }
        }
    }

    public void checkRoundEnd(String playerName){
        if (playerName.equals(firstPlayer)) firstPlayerEndRound = true;
        else secondPlayerEndRound = true;
        if(firstPlayerEndRound && secondPlayerEndRound){
            firstPlayerEndRound = false;
            secondPlayerEndRound = false;
            endRound();
        }
    }

    public void endRound(){
        takeCard(firstPlayer);
        takeCard(firstPlayer);
        takeCard(secondPlayer);
        takeCard(secondPlayer);
        firstPlayerConnection.sendString("updateResources");
        secondPlayerConnection.sendString("updateResources");
        firstPlayerConnection.sendString("checkRoundEndStatus");
        secondPlayerConnection.sendString("checkRoundEndStatus");
    }

    public void cancelRoundEnd(String playerName){
        if (playerName.equals(firstPlayer) && firstPlayerEndRound) firstPlayerEndRound = false;
        if (playerName.equals(secondPlayer) && secondPlayerEndRound) secondPlayerEndRound = false;
    }

    public void decrementHandSize(String playerName){
        if (playerName.equals(firstPlayer)) firstPlayerHandSize--;
        else secondPlayerHandSize--;
    }
}
