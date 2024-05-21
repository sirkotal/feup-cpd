import java.net.*;
import java.io.*;
import java.util.Arrays;

public class Client {
    private static final String CLEAR   = "\033[H\033[2J";

    private Socket socket;

    public Client(String hostname, int port) throws UnknownHostException, IOException {
        this.socket = new Socket(hostname, port);
        System.out.println("Connected to the server:");
    }

    private static void clearScreen() {
        System.out.print(CLEAR);
        System.out.flush();
    }

    private void storeToken(String t, String username) {
        File file = new File("tokens/client_" + username + ".tok");

        // check if the directory exists, if not create it
        File dir = new File("tokens");
        if (!dir.exists()) {
            dir.mkdir();
        }

        
        if (file.exists()) {
            file.delete();
        }

        try {
            FileWriter writer = new FileWriter(file);
            writer.write(t);
            writer.close();
        } catch (IOException ex) {
            System.out.println("Error storing token: " + ex.getMessage());
        }
    }

    public String loadToken(String username) {
        File file = new File("tokens/client_" + username + ".tok");

        if (!file.exists()) {
            return "No Token";
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String token = reader.readLine();
            reader.close();
            return token;
        } catch (IOException ex) {
            return "No Token";
        }
    }

    public boolean authenticate() {
        String[] response;
        SocketUtils.RequestType requestType;
        String message;

        String username = "";

        try { do {
            response = SocketUtils.receive(this.socket);
            requestType = SocketUtils.RequestType.fromString(response[0]);

            switch (requestType) {
                case OPT:
                    message = String.join("\n", Arrays.stream(response).skip(1).toArray(String[]::new));
                    System.out.print(message);

                    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                    String option = reader.readLine();
                    SocketUtils.send(this.socket, option);
                    break;
                case USR:
                    clearScreen();
                    message = response[1];
                    System.out.print(message);
                    
                    reader = new BufferedReader(new InputStreamReader(System.in));
                    username = reader.readLine();
                    SocketUtils.send(this.socket, username);

                    String token = loadToken(username);
                    SocketUtils.send(this.socket, token);

                    break;
                case REG:
                    clearScreen();
                    message = response[1];
                    System.out.print(message);
                    
                    reader = new BufferedReader(new InputStreamReader(System.in));
                    username = reader.readLine();
                    SocketUtils.send(this.socket, username);
                    break;
                case PWD:
                    message = response[1];
                    System.out.print(message);
                    
                    reader = new BufferedReader(new InputStreamReader(System.in));
                    String password = reader.readLine();
                    password = SocketUtils.encrypt(password);
                    SocketUtils.send(this.socket, password);
                    break;
                case AUTH:
                    message = response[1];
                    storeToken(message, username);

                    SocketUtils.send(this.socket, new String[]{
                        SocketUtils.RequestType.ACK.toString()
                    });
                    break;
                case REJ:
                    if (response.length > 1) {
                        clearScreen();
                        message = response[1];
                        System.out.println(message);
                    }

                    SocketUtils.send(this.socket, new String[]{
                        SocketUtils.RequestType.ACK.toString()
                    });
                    break;
                case CLOSE:
                    clearScreen();
                    message = response[1];
                    System.out.println(message);
                    return false;
                default:
                    throw new RuntimeException("Invalid request type: " + requestType);
            }
        } while (requestType != SocketUtils.RequestType.AUTH);
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        } catch (StringIndexOutOfBoundsException ex) {
            System.out.println("Lost connection to the server.");
        }

        return true;
    }

    private void play() {
        String[] response;
        SocketUtils.RequestType requestType;
        String message;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        try { do {
            response = SocketUtils.receive(this.socket);
            requestType = SocketUtils.RequestType.fromString(response[0]);

            switch (requestType) {
                case START:
                    clearScreen();
                    message = String.join("\n", Arrays.stream(response).skip(1).toArray(String[]::new));
                    System.out.println(message);
                    break;
                case TURN:
                    message = String.join("\n", Arrays.stream(response).skip(1).toArray(String[]::new));
                    System.out.print(message);

                    // Clear input buffer
                    while (reader.ready()) {
                        reader.readLine();
                    }

                    String guess = reader.readLine();
                    SocketUtils.send(this.socket, guess);

                    break;
                case FEEDBACK:
                    message = String.join("\n", Arrays.stream(response).skip(1).toArray(String[]::new));
                    System.out.println(message);

                    SocketUtils.send(this.socket, new String[]{
                        SocketUtils.RequestType.ACK.toString()
                    });
                    break;
                case OVER:
                    message = String.join("\n", Arrays.stream(response).skip(1).toArray(String[]::new));
                    System.out.print(message);

                    // Clear input buffer
                    while (reader.ready()) {
                        reader.readLine();
                    }

                    String res = reader.readLine();
                    while (!res.equals("y") && !res.equals("n")) {
                        System.out.print("Invalid input. Please enter 'y' or 'n': ");
                        res = reader.readLine();
                    }

                    SocketUtils.send(this.socket, res);

                    if (res.toLowerCase().equals("y")) {
                        clearScreen();
                        System.out.println("Waiting in queue...");
                    } else {
                        return;
                    }
                    break;
                case PING:
                    System.out.println("Ping received.");

                    SocketUtils.send(this.socket, new String[]{
                        SocketUtils.RequestType.ACK.toString()
                    });
                    break;
                default:
                    throw new RuntimeException("Invalid request type: " + requestType);
            }
        } while (true);
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        } catch (StringIndexOutOfBoundsException ex) {
            System.out.println("Lost connection to the server.");
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) return;
 
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        Client client = null;

        try {
            client = new Client(hostname, port);
        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
            return;
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
            return;
        }

        if (!client.authenticate()) {
            return;
        }

        clearScreen();
        System.out.println("Waiting in queue...");

        client.play();
    }
}
