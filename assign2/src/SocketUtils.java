import java.io.*;
import java.net.*;
import java.security.MessageDigest;

public class SocketUtils {
    public enum RequestType {
        ACK("ACK"),
        REJ("REJ"),
        OPT("OPT"),
        USR("USR"),
        REG("REG"),
        PWD("PWD"),
        AUTH("AUTH"),
        PING("PING"),
        START("START"),
        TURN("TURN"),
        FEEDBACK("FEEDBACK"),
        OVER("OVER"),
        CLOSE("CLOSE");

        private final String value;

        RequestType(String value) {
            this.value = value;
        }

        public String toString() {
            return this.value;
        }

        public static RequestType fromString(String text) {
            for (RequestType requestType : RequestType.values()) {
                if (requestType.value.equalsIgnoreCase(text)) {
                    return requestType;
                }
            }

            throw new IllegalArgumentException("No constant with text " + text + " found");
        }
    }

    // Encrypt the message using SHA-256
    public static String encrypt(String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(message.getBytes("UTF-8"));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    public static String[] receive(Socket socket) throws IOException, StringIndexOutOfBoundsException {
        InputStream input = socket.getInputStream();
        byte[] data = new byte[1024];
        int count = input.read(data);
        String message = new String(data, 0, count);
        return message.split("\n");
    }

    public static void send(Socket socket, String message) throws IOException {
        OutputStream output = socket.getOutputStream();
        output.write((message + "\n").getBytes());
    }

    public static void send(Socket socket, String[] messages) throws IOException {
        String finalMessage = "";
        for (String message : messages) {
            finalMessage += message + "\n";
        }

        send(socket, finalMessage);
    }
}
