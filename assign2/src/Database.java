import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Database {
    private File csvFile;
    private List<String> players;
    private HashMap<String, String> credentials;
    private HashMap<String, String> tokens;
    private HashMap<String, Integer> ranks;
    private ReentrantLock lock;

    public Database(String csvFileName) {
        csvFile = new File(csvFileName);

        this.players = new ArrayList<String>();
        this.credentials = new HashMap<String, String>();
        this.tokens = new HashMap<String, String>();
        this.ranks = new HashMap<String, Integer>();
        this.lock = new ReentrantLock();

        if (!csvFile.exists()) {
            this.createFile();
        } 
        else {
            try {
                this.loadData();
                this.resetTokens();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createFile() {
        try {
            FileWriter writer = new FileWriter(csvFile);
            writer.write("username,password,token\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadData() throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(csvFile));
        br.readLine(); // skip header

        for (String st; (st = br.readLine()) != null;) {
            String[] data = st.split(",");
            String username = data[0];
            String password = data[1];
            String rank = data[2];
            String token;
            if (data.length > 3) {
                token = data[3];
            } else {
                token = "";
            }

            players.add(username);
            credentials.put(username, password);
            tokens.put(username, token);
            ranks.put(username, Integer.parseInt(rank));
        }

        br.close();
    }

    private void resetTokens() {
        for (String player : players) {
            tokens.put(player, "");
        }
    }

    public void storeData() {
        this.lock.lock();

        csvFile.delete();

        try {
            FileWriter writer = new FileWriter(csvFile);
            writer.write("username,password,rank,token\n");

            for (String player : players) {
                writer.write(player + "," + credentials.get(player) + ","
                    + Integer.toString(ranks.get(player)) + "," + tokens.get(player) + "\n");
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.lock.unlock();
    }

    public void invalidateToken(String username) {
        this.lock.lock();
        tokens.put(username, "");
        this.storeData();
        this.lock.unlock();
    }

    public void updateRank(Player player) {
        this.lock.lock();
        ranks.put(player.getUsername(), player.getRank());
        this.storeData();
        this.lock.unlock();
    }

    public Player login(String username, String password, String token, Socket socket) {
        if (!players.contains(username)) {
            return null;
        }

        if (!credentials.get(username).equals(password)) {
            return null;
        }

        this.lock.lock();
        tokens.put(username, token);
        this.lock.unlock();

        this.storeData();

        return new Player(username, socket, ranks.get(username));
    }

    public Player tokenLogin(String username, String token, Socket socket) throws FileNotFoundException, IOException {
        this.lock.lock();

        if (!players.contains(username)) {
            this.lock.unlock();
            return null;
        }

        if (!tokens.get(username).equals(token)) {
            this.lock.unlock();
            return null;
        }

        this.lock.unlock();

        return new Player(username, socket, ranks.get(username));
    }

    public Player register(String username, String password, String token, Socket socket) {
        if (players.contains(username)) {
            return null;
        }

        this.lock.lock();
        players.add(username);
        credentials.put(username, password);
        tokens.put(username, token);
        ranks.put(username, 0);
        this.storeData();
        this.lock.unlock();

        return new Player(username, socket);
    }
}
