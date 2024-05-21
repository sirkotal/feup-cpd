import java.net.*;
import java.io.*;
import java.lang.Runnable;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

public class Server {
    private static final File file = new File("wordlist.txt");
    private static final String DB_FILE = "database.csv";
    private static final int PLAYER_PER_GAME = 2;
    private static final int MAX_CONCURRENT_GAME = 3;
    private static final int MAX_CONCURRENT_AUTH = 5;
    private static final int TIMEOUT = 30 * 1000; // 30s timeout
    private static final int PING_INTERVAL = 10 * 1000; // 10s interval
    private static final int START_RANK_INTERVAL = 1;

    private final int port;

    private List<String> wordlist;
    private Queue queue;
    private Database database;

    private ServerSocket serverSocket;

    private ExecutorService gameThreadPool;
    private ExecutorService authThreadPool;

    private Queue rankedQueue;
    private int rankInterval;

    public Server(int port) throws IOException {
        this.port = port;
        this.wordlist = new ArrayList<String>();

        BufferedReader br = new BufferedReader(new FileReader(file));
        for (String st; (st = br.readLine()) != null;) {
            this.wordlist.add(st);
        }

        this.queue = new Queue();
        this.database = new Database(DB_FILE);
        this.gameThreadPool = Executors.newFixedThreadPool(MAX_CONCURRENT_GAME);
        this.authThreadPool = Executors.newFixedThreadPool(MAX_CONCURRENT_AUTH);

        this.rankedQueue = new Queue();
        this.rankInterval = START_RANK_INTERVAL;

        br.close();
    }
    
    private void startSimpleGame() {
        if (this.queue.size() >= PLAYER_PER_GAME) {
            int randomIdx = (new Random()).nextInt(wordlist.size());
            String word = wordlist.get(randomIdx);
            List<Player> gameClients = new ArrayList<>();

            this.queue.lock();
            for (int i = 0; i < PLAYER_PER_GAME; i++) {
                gameClients.add(queue.pop());
            }
            queue.unlock();

            Game game = new Game(gameClients, word, this.queue, this.database);
            this.gameThreadPool.execute(game);
        }
    }

    private void startRankedGame() {
        if (this.rankedQueue.size() >= PLAYER_PER_GAME) {
            this.rankedQueue.lock();
            this.rankedQueue.sort();

            boolean gameStarted = false;

            List<Player> queue = this.rankedQueue.toList();
            for (int i = 0; i < queue.size(); i += PLAYER_PER_GAME) {
                Player playerOne = queue.get(i);
                Player playerTwo = queue.get(i + 1);

                if (playerOne.getRank() - playerTwo.getRank() > this.rankInterval) {
                    continue;
                }

                List<Player> gameClients = new ArrayList<>();
                for (int j = 0; j < PLAYER_PER_GAME; j++) {
                    gameClients.add(this.rankedQueue.get(i));
                }

                Game game = new Game(gameClients, this.wordlist.get(i), this.rankedQueue, this.database);
                this.gameThreadPool.execute(game);

                gameStarted = true;

                // reset rank interval
                this.rankInterval = START_RANK_INTERVAL;
            }

            if (!gameStarted) {
                // TODO: make it update in intervals of x seconds instead of every iteration
                this.rankInterval++;
            }

            this.rankedQueue.unlock();
        }
    }

    private Player login(String username, String password, Socket socket) throws IOException {
        String token = SocketUtils.encrypt(
            username + ThreadLocalRandom.current().nextInt()
        );

        this.queue.lock();
        for (Player player : this.queue.toList()) {
            if (player.getUsername().equals(username)) {
                SocketUtils.send(socket, new String[]{
                    SocketUtils.RequestType.REJ.toString(),
                    "User already in queue"
                });

                SocketUtils.RequestType requestType =
                    SocketUtils.RequestType.fromString(SocketUtils.receive(socket)[0]);

                if (requestType != SocketUtils.RequestType.ACK) {
                    SocketUtils.send(socket, new String[]{
                        SocketUtils.RequestType.CLOSE.toString(),
                        "Unexpected error, connection closed"
                    });

                    socket.close();
                }

                this.queue.unlock();
                return null;
            }
        }
        this.queue.unlock();

        Player player = this.database.login(username, password, token, socket);

        if (player == null) {
            SocketUtils.send(socket, new String[]{
                SocketUtils.RequestType.REJ.toString(),
                "Wrong username or password"
            });

            SocketUtils.RequestType requestType =
                SocketUtils.RequestType.fromString(SocketUtils.receive(socket)[0]);

            if (requestType != SocketUtils.RequestType.ACK) {
                SocketUtils.send(socket, new String[]{
                    SocketUtils.RequestType.CLOSE.toString(),
                    "Unexpected error, connection closed"
                });

                socket.close();
            }

            return null;
        }

        SocketUtils.send(socket, new String[]{
            SocketUtils.RequestType.AUTH.toString(),
            token
        });

        SocketUtils.RequestType requestType =
            SocketUtils.RequestType.fromString(SocketUtils.receive(socket)[0]);

        if (requestType != SocketUtils.RequestType.ACK) {
            SocketUtils.send(socket, new String[]{
                SocketUtils.RequestType.CLOSE.toString(),
                "Unexpected error, connection closed"
            });

            socket.close();
            return null;
        }

        return player;
    }

    private Player tokenLogin(String username, String token, Socket socket) throws IOException {
        Player player = null;

        this.queue.lock();
        for (Player p : this.queue.toList()) {
            if (p.getUsername().equals(username)) {
                player = this.database.tokenLogin(username, token, socket);
            }
        }
        this.queue.unlock();

        if (player == null) {
            SocketUtils.send(socket, new String[]{
                SocketUtils.RequestType.REJ.toString(),
            });

            SocketUtils.RequestType requestType =
                SocketUtils.RequestType.fromString(SocketUtils.receive(socket)[0]);

            if (requestType != SocketUtils.RequestType.ACK) {
                SocketUtils.send(socket, new String[]{
                    SocketUtils.RequestType.CLOSE.toString(),
                    "Unexpected error, connection closed"
                });

                socket.close();
            }

            return null;
        }

        SocketUtils.send(socket, new String[]{
            SocketUtils.RequestType.AUTH.toString(),
            token
        });

        SocketUtils.RequestType requestType =
            SocketUtils.RequestType.fromString(SocketUtils.receive(socket)[0]);

        if (requestType != SocketUtils.RequestType.ACK) {
            SocketUtils.send(socket, new String[]{
                SocketUtils.RequestType.CLOSE.toString(),
                "Unexpected error, connection closed"
            });

            socket.close();
            return null;
        }

        return player;
    }

    private Player register(String username, String password, Socket socket) throws IOException {
        String token = SocketUtils.encrypt(
            username + ThreadLocalRandom.current().nextInt()
        );

        Player player = this.database.register(username, password, token, socket);

        if (player == null) {
            SocketUtils.send(socket, new String[]{
                SocketUtils.RequestType.REJ.toString(),
                "Username already exists"
            });

            SocketUtils.RequestType requestType =
                SocketUtils.RequestType.fromString(SocketUtils.receive(socket)[0]);

            if (requestType != SocketUtils.RequestType.ACK) {
                SocketUtils.send(socket, new String[]{
                    SocketUtils.RequestType.CLOSE.toString(),
                    "Unexpected error, connection closed"
                });

                socket.close();
            }

            return null;
        }

        SocketUtils.send(socket, new String[]{
            SocketUtils.RequestType.AUTH.toString(),
            token
        });

        SocketUtils.RequestType requestType =
            SocketUtils.RequestType.fromString(SocketUtils.receive(socket)[0]);

        if (requestType != SocketUtils.RequestType.ACK) {
            SocketUtils.send(socket, new String[]{
                SocketUtils.RequestType.CLOSE.toString(),
                "Unexpected error, connection closed"
            });

            socket.close();
            return null;
        }

        return player;
    }

    public void handleAuthentication(Socket socket) throws IOException {
        Player player = null;
        long startTime = System.currentTimeMillis();

        // if is stuck in reading
        socket.setSoTimeout(TIMEOUT);

        try { do {
            if (System.currentTimeMillis() - startTime > TIMEOUT) {
                SocketUtils.send(socket, new String[]{
                    SocketUtils.RequestType.CLOSE.toString(),
                    "Took too long to authenticate, connection closed"
                });
                socket.close();
                return;
            }

            SocketUtils.send(socket, new String[]{
                SocketUtils.RequestType.OPT.toString(),
                "1 -> Login",
                "2 -> Register",
                "3 -> Exit",
                "Choose an option: "
            });

            String input = SocketUtils.receive(socket)[0];

            if (input.equals("3")) {
                SocketUtils.send(socket, new String[]{
                    SocketUtils.RequestType.CLOSE.toString(),
                    "Connection closed"
                });
                socket.close();
                return;
            }

            if (!input.equals("1") && !input.equals("2")) {
                SocketUtils.send(socket, new String[]{
                    SocketUtils.RequestType.REJ.toString(),
                    "Invalid option"
                });

                SocketUtils.RequestType requestType =
                    SocketUtils.RequestType.fromString(SocketUtils.receive(socket)[0]);

                if (requestType != SocketUtils.RequestType.ACK) {
                    socket.close();
                    return;
                }
                continue;
            }

            String username = null;
            String password = null;
            String token = null;
            switch (input) {
                case "1":
                    SocketUtils.send(socket, new String[]{
                        SocketUtils.RequestType.USR.toString(),
                        "Username: "
                    });
                    username = SocketUtils.receive(socket)[0];

                    token = SocketUtils.receive(socket)[0];
                    if (!token.equals("No Token")) {
                        player = this.tokenLogin(username, token, socket);
                    }

                    if (player == null) { // login with token failed
                        SocketUtils.send(socket, new String[]{
                            SocketUtils.RequestType.PWD.toString(),
                            "Password: "
                        });
                        password = SocketUtils.receive(socket)[0];

                        player = this.login(username, password, socket);
                    }
                    break;
                case "2":
                    SocketUtils.send(socket, new String[]{
                        SocketUtils.RequestType.REG.toString(),
                        "Username: "
                    });
                    username = SocketUtils.receive(socket)[0];

                    SocketUtils.send(socket, new String[]{
                        SocketUtils.RequestType.PWD.toString(),
                        "Password: "
                    });
                    password = SocketUtils.receive(socket)[0];

                    player = this.register(username, password, socket);
                    break;
                default:
            }

            if (player != null) {
                queue.lock();
                if (!queue.isInQueue(player)) {
                    queue.add(player);
                } else {
                    queue.setSocket(player, socket);
                }
                queue.unlock();
            }

        } while (player == null);
        } catch (SocketTimeoutException ex) {
            SocketUtils.send(socket, new String[]{
                SocketUtils.RequestType.CLOSE.toString(),
                "Took too long to authenticate, connection closed"
            });
            socket.close();
        }
    }

    private void pingClients() {
        System.out.println("Pinging clients");

        this.queue.lock();
        for (Player player : this.queue.toList()) {
            try {
                SocketUtils.send(player.getSocket(), new String[]{
                    SocketUtils.RequestType.PING.toString()
                });

                SocketUtils.RequestType requestType =
                    SocketUtils.RequestType.fromString(SocketUtils.receive(player.getSocket())[0]);

                if (requestType != SocketUtils.RequestType.ACK) {
                    String username = this.queue.pop().getUsername();
                    this.database.invalidateToken(username);
                }
            } catch (IOException ex) {
                String username = this.queue.pop().getUsername();
                this.database.invalidateToken(username);
            } catch (StringIndexOutOfBoundsException ex) {
                String username = this.queue.pop().getUsername();
                this.database.invalidateToken(username);
            }
        }
        queue.unlock();
    }

    public void start() {
        try {
            this.serverSocket = new ServerSocket(port);
            System.out.println("Server is listening on port " + port);
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void run() {
        Thread authThread = new Thread(() -> {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println("New client: " + socket.getRemoteSocketAddress());

                    Runnable authTask = () -> {
                        try {
                            this.handleAuthentication(socket);
                        } catch (IOException ex) {
                            System.out.println("I/O error: " + ex.getMessage());
                        } catch (StringIndexOutOfBoundsException ex) {
                            System.out.println("Client disconnected");
                            return;
                        }
                    };
                    this.authThreadPool.execute(authTask);

                } catch (IOException ex) {
                    System.out.println("I/O error: " + ex.getMessage());
                }
            }
        });

        Thread gameThread = new Thread(() -> {
            long lastPing = System.currentTimeMillis();
            while (true) {
                if (System.currentTimeMillis() - lastPing > PING_INTERVAL) {
                    this.pingClients();
                    lastPing = System.currentTimeMillis();
                }

                this.startSimpleGame();
            }
        });

        authThread.start();
        gameThread.start();
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) return;
        int port = Integer.parseInt(args[0]);

        Server server = new Server(port);
        server.start(); server.run();
    }
}
