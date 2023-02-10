public class Game {
    private final String firstPlayer;
    private String secondPlayer;
    private final int sessionID;
    private final String password;
    private final TCPConnection firstPlayerConnection;
    private TCPConnection secondPlayerConnection;
    private String lastCommand;
    private boolean started;

    public Game(int ID, TCPConnection firstPlayerConnection, String firstPlayer, String password){
        this.sessionID = ID;
        this.firstPlayerConnection = firstPlayerConnection;
        this.firstPlayer = firstPlayer;
        if (password.equals("noPassword"))
            this.password = "";
        else
            this.password = password;

    }

    public String getCommand(){
        String tempReturnCommand = lastCommand;
        lastCommand = "";
        return tempReturnCommand;
    }

    public int getSessionID(){
        return sessionID;
    }

    public void setCommand(String newCommand){
        lastCommand = newCommand;
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

    public void setStarted(boolean started) { this.started = started; }

    @Override
    public String toString(){
        if (secondPlayer != null)
        return firstPlayer + "," + secondPlayer + "," + sessionID + "," + getPassword().length() + ",";
        else return firstPlayer + ", ," + sessionID + "," + getPassword().length() + ",";
    }
}
