import java.util.ArrayList;
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

    public void setSecondPlayer(String secondPlayer) { this.secondPlayer = secondPlayer; }

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
        playerTurn = Math.abs(playerTurn-1);
        turnTime = 60;
    }

    public void endGame(){

    }

    public int getPlayerTurn(){ return playerTurn; }
}
