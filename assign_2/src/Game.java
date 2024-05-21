package src;

import java.io.*;
import java.net.*;
import java.util.*;

public class Game {
    private List<Socket> userSockets;

    public Game(int players, List<Socket> userSockets) {
        this.userSockets = userSockets;
    }

    public void start() {
        // Code to start the game
        System.out.println("Starting game with " + userSockets.size() + " players");
    }
}