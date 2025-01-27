package org.example;

import javax.swing.JFrame;

public class App {
    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) {

        JFrame frame = new JFrame("PacMan");
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        PacMan pacmanGame = new PacMan();
        frame.setSize(pacmanGame.getBoardWidth(), pacmanGame.getBoardHeight());
        frame.add(pacmanGame);
        frame.pack();
        pacmanGame.requestFocus();
        frame.setVisible(true);
    }
}
