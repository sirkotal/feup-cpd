import java.net.Socket;

public class Player {
    private int rank;
    private String username;
    private Socket socket;

    public Player(String username, Socket socket, int rank) {
        this.rank = rank;

        this.username = username;
        this.socket = socket;
    }

    public Player(String username, Socket socket) {
        this(username, socket, 0);
    }

    public Socket getSocket() {
        return this.socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getUsername() {
        return this.username;
    }

    public int getRank() {   
        return this.rank;
    }

    public void modifyRank(int change) {
        this.rank += change;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj.getClass() != this.getClass()) {
            return false;
        }

        final Player other = (Player) obj;

        return this.username.equals(other.getUsername());
    }
}
