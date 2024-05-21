import java.io.*;
import java.lang.Runnable;
import java.net.*;
import java.util.*;

public class Game implements Runnable {
    private static final int MAX_ATTEMPTS = 4;
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";

    private final String targetWord;
    private Player playerOne;
    private Player playerTwo;
    private Queue queue;
    private Database database;
    private boolean ranked;

    public Game(List<Player> gameClients, String word, Queue queue, Database database, boolean ranked) {
        this.targetWord = word;
        this.ranked = ranked;

        Collections.shuffle(gameClients);
        this.playerOne = gameClients.get(0);
        this.playerTwo = gameClients.get(1);

        // Remove timeout
        try {
            this.playerOne.getSocket().setSoTimeout(0);
            this.playerTwo.getSocket().setSoTimeout(0);
        } catch (SocketException ex) {
            System.out.println("Error removing socket timeout.");
        }

        try {
            SocketUtils.send(this.playerOne.getSocket(), new String[]{
                SocketUtils.RequestType.START.toString(),
                "You are playing against " + this.playerTwo.getUsername(),
                "You are the first to guess the word.",
                "The word has " + this.targetWord.length() + " letters."
            });

            SocketUtils.send(this.playerTwo.getSocket(), new String[]{
                SocketUtils.RequestType.START.toString(),
                "You are playing against " + this.playerOne.getUsername(),
                "You are the second to guess the word.",
                "The word has " + this.targetWord.length() + " letters."
            });
        } catch (IOException ex) {
            System.out.println("Error sending message to clients.");
        }

        // Play again feature
        this.queue = queue;

        // Invalidate token feature
        this.database = database;
    }

    // since ranked is not completely implemented there is a constructor
    // that does not take the ranked parameter, making all games unranked
    public Game(List<Player> gameClients, String word, Queue queue, Database database) {
        this(gameClients, word, queue, database, false);
    }

    public void run() {
        try {
            this.startGame();
        } catch (IOException | StringIndexOutOfBoundsException e) {
            try {
                SocketUtils.send(this.playerOne.getSocket(), new String[]{
                    SocketUtils.RequestType.OVER.toString(),
                    "An error occurred. The game has ended.",
                    "Would you like to play again? [y/n]: "
                });

                String response = SocketUtils.receive(this.playerOne.getSocket())[0];
                if (response.toLowerCase().equals("y")) {
                    this.queue.lock();
                    this.queue.add(this.playerOne);
                    this.queue.unlock();
                } else {
                    this.database.invalidateToken(this.playerOne.getUsername());
                }
            } catch(IOException | StringIndexOutOfBoundsException ex) { }

            try {
                SocketUtils.send(this.playerTwo.getSocket(), new String[]{
                    SocketUtils.RequestType.OVER.toString(),
                    "An error occurred. The game has ended.",
                    "Would you like to play again? [y/n]: "
                });

                String response = SocketUtils.receive(this.playerTwo.getSocket())[0];
                if (response.toLowerCase().equals("y")) {
                    this.queue.lock();
                    this.queue.add(this.playerTwo);
                    this.queue.unlock();
                } else {
                    this.database.invalidateToken(this.playerTwo.getUsername());
                }
            } catch(IOException | StringIndexOutOfBoundsException ex) { }
        }
    }

    private void startGame() throws IOException, StringIndexOutOfBoundsException {
        int attemptsLeft = MAX_ATTEMPTS;
        List<String> previousGuesses = new ArrayList<>();

        Player turn = this.playerOne;

        while (attemptsLeft > 0) {
            String[] message = new String[previousGuesses.size() + 4];
            Arrays.fill(message, "");
            message[0] = SocketUtils.RequestType.TURN.toString();

            if (previousGuesses.size() != 0) {
                message[1] = "Previous guesses: ";
                for (int i = 0; i < previousGuesses.size(); i++) {
                    message[i + 2] = previousGuesses.get(i);
                }
                message[previousGuesses.size() + 2] = "Attempts left: " + attemptsLeft;
                message[previousGuesses.size() + 3] = "Enter your guess: ";
            } else {
                message[1] = "Attempts left: " + attemptsLeft;
                message[2] = "Enter your guess: ";
            }

            SocketUtils.send(turn.getSocket(), message);

            String[] response;
            response = SocketUtils.receive(turn.getSocket());

            String guess = response[0];
            if (guess.equals(this.targetWord)) {
                this.notifyWin(turn);
                break;
            }

            String feedback = giveFeedback(guess);
            previousGuesses.add(feedback);

            SocketUtils.send(turn.getSocket(), new String[]{
                SocketUtils.RequestType.FEEDBACK.toString(),
                "Incorrect guess. Feedback: " + feedback,
                "Wait for the other player to guess the word..."
            });

            SocketUtils.RequestType requestType =
                SocketUtils.RequestType.fromString(SocketUtils.receive(turn.getSocket())[0]);

            if (requestType != SocketUtils.RequestType.ACK) {
                System.out.println("Invalid request type: " + requestType);
                return;
            }

            turn = turn == this.playerOne ? this.playerTwo : this.playerOne;
            if (turn == this.playerTwo) {
                attemptsLeft--;
            }
        }

        if (attemptsLeft == 0) {
            SocketUtils.send(this.playerOne.getSocket(), new String[]{
                SocketUtils.RequestType.OVER.toString(),
                "Game over. Both players have run out of attempts. The correct word was: " + GREEN + this.targetWord + RESET,
                "Would you like to play again? [y/n]: "
            });

            String response = SocketUtils.receive(this.playerOne.getSocket())[0];
            if (response.toLowerCase().equals("y")) {
                this.queue.lock();
                this.queue.add(this.playerOne);
                this.queue.unlock();
            } else {
                this.database.invalidateToken(this.playerOne.getUsername());
            }

            SocketUtils.send(this.playerTwo.getSocket(), new String[]{
                SocketUtils.RequestType.OVER.toString(),
                "Game over. Both players have run out of attempts. The correct word was: " + GREEN + this.targetWord + RESET,
                "Would you like to play again? [y/n]: "
            });

            response = SocketUtils.receive(this.playerTwo.getSocket())[0];
            if (response.toLowerCase().equals("y")) {
                this.queue.lock();
                this.queue.add(this.playerTwo);
                this.queue.unlock();
            } else {
                this.database.invalidateToken(this.playerTwo.getUsername());
            }
        }
    }

    private void notifyWin(Player winner) throws IOException, StringIndexOutOfBoundsException {
        if (this.ranked) {
            winner.modifyRank(1);
            this.database.updateRank(winner);
        }

        SocketUtils.send(winner.getSocket(), new String[]{
            SocketUtils.RequestType.OVER.toString(),
            "Congratulations! You've guessed the word correctly: " + GREEN + this.targetWord + RESET,
            "Would you like to play again? [y/n]: "
        });

        String response = SocketUtils.receive(winner.getSocket())[0];
        if (response.toLowerCase().equals("y")) {
            this.queue.lock();
            this.queue.add(winner);
            this.queue.unlock();
        } else {
            this.database.invalidateToken(winner.getUsername());
        }

        Player loser = winner == this.playerOne ? this.playerTwo : this.playerOne;
        if (this.ranked) {
            loser.modifyRank(-1);
            this.database.updateRank(loser);
        }

        SocketUtils.send(loser.getSocket(), new String[]{
            SocketUtils.RequestType.OVER.toString(),
            "Sorry, you've lost. The correct word was: " + GREEN + this.targetWord + RESET,
            "Would you like to play again? [y/n]: "
        });

        response = SocketUtils.receive(loser.getSocket())[0];
        if (response.toLowerCase().equals("y")) {
            this.queue.lock();
            this.queue.add(loser);
            this.queue.unlock();
        } else {
            this.database.invalidateToken(loser.getUsername());
        }
    }

    private String giveFeedback(String guess) {
        StringBuilder feedback = new StringBuilder();
        for (int i = 0; i < guess.length(); i++) {
            char guessedChar = guess.charAt(i);
            if (this.targetWord.contains(String.valueOf(guessedChar))) {
                if (guess.charAt(i) == this.targetWord.charAt(i)) {
                    feedback.append(GREEN + guessedChar + RESET);
                } else {
                    feedback.append(YELLOW + guessedChar + RESET);
                }
            } else {
                feedback.append(RED + guessedChar + RESET);
            }
        }
        return feedback.toString();
    }
}
