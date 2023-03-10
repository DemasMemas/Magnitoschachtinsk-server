import GameObjects.Building;
import GameObjects.Equip.Armor;
import GameObjects.Equip.Weapon;
import GameObjects.Person;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class Game {
    private final String firstPlayer;
    private String secondPlayer;
    private final int sessionID;
    private final String password;
    private final TCPConnection firstPlayerConnection;
    private TCPConnection secondPlayerConnection;
    private String lastCommand;
    private boolean started;
    private final ArrayList<String> commandQuery;
    private int playerTurn = 0;
    private int turnTime;
    private final Random random = new Random();
    private final HashMap<Integer,Card> firstPlayerDeck = new HashMap<>();
    private final HashMap<Integer,Card> secondPlayerDeck = new HashMap<>();
    private final ArrayList<Card> firstPlayerActiveCards = new ArrayList<>();
    private final ArrayList<Card> secondPlayerActiveCards = new ArrayList<>();
    private boolean firstPlayerEndRound = false;
    private boolean secondPlayerEndRound = false;

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

    public void setSecondPlayerConnection(TCPConnection secondPlayerConnection) { this.secondPlayerConnection = secondPlayerConnection; }

    public void setSecondPlayer(String secondPlayer) {
        this.secondPlayer = secondPlayer;
        makeDeck(secondPlayer, secondPlayerDeck);
    }

    private void makeDeck(String player, HashMap<Integer, Card> playerDeck) {
        try {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT decks.cards FROM decks,users WHERE decks.deck_id = users.active_deck AND users.nickname = ?");
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
        else endGame();
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
        //?????????? ?????????????? ????????????, ?????????????? ????????
        playerTurn = random.nextInt(2);
        if (playerTurn == 0) setCommand("showFirstPlayer," + sessionID + "," + firstPlayer);
        else setCommand("showFirstPlayer," + sessionID + "," + secondPlayer);

        // ???????????? ?????????????? ???? ??????
        turnTime = 60;
        Runnable task = () -> { while (isStarted()) try {
            Thread.sleep(1000);
            gameLoop();
        } catch (InterruptedException e) {
            e.printStackTrace(); }};
        new Thread(task).start();
    }

    public void changeTurn(){
        turnTime = 60;
        playerTurn = Math.abs(playerTurn-1);
    }

    public void endGame(){

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
        if (playerName.equals(firstPlayer)){
            firstPlayerConnection.sendString("takeCard," + takeCardFromDeck(firstPlayerDeck).card_id);
            secondPlayerConnection.sendString("enemyTakeCard");
        }
        else {
            secondPlayerConnection.sendString("takeCard," + takeCardFromDeck(secondPlayerDeck).card_id);
            firstPlayerConnection.sendString("enemyTakeCard");
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
        cancelRoundEnd(playerName);
        switch (cardType){
            case "person" -> {
                String[] info = cardInfo.split(" ");
                Card tempCard = getUserCardByID(Integer.parseInt(info[0]), 1);
                Person tempPerson = new Person();

                // setting armor
                String[] armorInfo = info[1].split(";");
                Armor tempArmor;
                if (armorInfo.length == 3)
                    tempArmor = new Armor(Integer.parseInt(armorInfo[0]), Integer.parseInt(armorInfo[1]),
                            armorInfo[2], null);
                else
                    tempArmor = new Armor(Integer.parseInt(armorInfo[0]), Integer.parseInt(armorInfo[1]),
                            armorInfo[2], Arrays.stream(armorInfo[3].split(";")).mapToInt(Integer::parseInt).toArray());
                tempPerson.setArmor(tempArmor);

                // setting weapon
                String[] weaponInfo = info[1].split(";");
                Weapon tempWeapon;
                if (weaponInfo.length == 3)
                    tempWeapon = new Weapon(Integer.parseInt(weaponInfo[0]), Integer.parseInt(weaponInfo[1]),
                            weaponInfo[2], null);
                else
                    tempWeapon = new Weapon(Integer.parseInt(weaponInfo[0]), Integer.parseInt(weaponInfo[1]),
                            weaponInfo[2], Arrays.stream(weaponInfo[3].split(";")).mapToInt(Integer::parseInt).toArray());
                tempPerson.setWeapon(tempWeapon);

                // set everything else later
                tempCard.person = tempPerson;
                if (playerName.equals(firstPlayer)){
                    firstPlayerActiveCards.add(tempCard);
                    secondPlayerConnection.sendString("enemyCard,people," + tempCard.getPersonCard());
                } else {
                    firstPlayerConnection.sendString("enemyCard,people," + tempCard.getPersonCard());
                    secondPlayerActiveCards.add(tempCard);
                }
            }
            case "building" -> {
                String[] info = cardInfo.split(" ");
                Card tempCard = getUserCardByID(Integer.parseInt(info[0]), 1);
                // set everything else later
                tempCard.building = new Building(Integer.parseInt(info[0]));
                if (playerName.equals(firstPlayer)){
                    firstPlayerActiveCards.add(tempCard);
                    secondPlayerConnection.sendString("enemyCard,building," + tempCard.getBuildingCard());
                } else {
                    firstPlayerConnection.sendString("enemyCard,building," + tempCard.getBuildingCard());
                    secondPlayerActiveCards.add(tempCard);
                }
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
}
