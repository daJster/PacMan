package org.example;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javax.swing.*;
import lombok.*;



public class PacMan extends JPanel implements ActionListener, KeyListener{

    

    private static int tileSize = 64;
    private static int characterSize = 50; 
    private static int foodSize = 4;

    @Getter
    private int boardWidth;

    @Getter
    private int boardHeight;
    
    private List<String> ghostColors = new ArrayList<>(List.of("orange", "pink", "red", "blue"));
    private List<String> ghostColorsInitials = new ArrayList<>(List.of("o", "p", "r", "b"));
    private int pacmanDefaultDirectionAngle = 90;

    private Image wallImage;
    private List<Image> ghostImages = new ArrayList<>();
    private List<Image> cherryFrames = new ArrayList<>();
    private int cherryFramesCount = 2;
    private Image pacmanImage;
    private Image scaredGhostImage;
    private Image powerFoodImage;

    private int mapRowsCount;
    private int mapColumnsCount;
    private List<String> tileMap = new ArrayList<>();

    private HashSet<Block> walls = new HashSet<>();
    private HashSet<Block> foods = new HashSet<>();
    private HashSet<Block> cherries = new HashSet<>();
    private HashSet<Block> ghosts = new HashSet<>();
    private Block pacman;

    private Timer gameLoop; 

    PacMan() {
        loadMap("/maps/classic.txt");
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.black);
        addKeyListener(this);
        setFocusable(true);
        loadImages();
        setBlocks();
        gameLoop = new Timer(20, this);
        gameLoop.start();
    }


    class Block {
        private int x;
        private int y;
        private int type;
        private Image image;
        private int width;
        
        private int startX;
        private int startY;

        private int directionAngle = 90;
        private static int speed = tileSize/20;
        private int velocityX;
        private int velocityY;

        Block(int x, int y, int type, Image image, int width) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.type = type;
            this.width = width;
            this.startX = x;
            this.startY = y;
        }


        void updateDirection(int directionAngle) {
            int lastDirectionAngle = this.directionAngle;
            
            this.directionAngle = directionAngle;
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;
            
            if (this.image != null) {
                this.image = rotateImage((BufferedImage) this.image, this.directionAngle);
            }

            for (Block wall : walls) {
                if (booleanCollision(this, wall)) {
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.directionAngle = lastDirectionAngle;
                    updateVelocity();
                }        
            }
        }

        void updateVelocity() {
            switch (this.directionAngle) {
                case 180:
                    velocityX = speed;
                    velocityY = 0;
                    break;
                case 270:
                    velocityY = speed;
                    velocityX = 0;
                    break;
                case 0:
                    velocityX = -speed;
                    velocityY = 0;
                    break;
                case 90:
                    velocityY = -speed;
                    velocityX = 0;
                    break;
            }
        }
    }

    

    public void loadImages() {
        wallImage = new ImageIcon(getClass().getResource("/misc/wall.png")).getImage();
        scaredGhostImage = new ImageIcon(getClass().getResource("/misc/scaredGhost.png")).getImage();
        powerFoodImage = new ImageIcon(getClass().getResource("/misc/powerFood.png")).getImage();

        for (int i = 0; i < ghostColors.size(); i++) {
            Image image = new ImageIcon(getClass().getResource("/misc/" + ghostColors.get(i) + "Ghost.png")).getImage();
            ghostImages.add(image);
        }

        for (int i = 0; i < cherryFramesCount; i++) {
            Image frame = new ImageIcon(getClass().getResource("/misc/cherry" + (i+1) + ".png")).getImage();
            cherryFrames.add(frame);
        }

        pacmanImage = new ImageIcon(getClass().getResource("/misc/pacman.png")).getImage();
    }

    public void loadMap(String pathToMap) {
        try {
            InputStream inputStream = getClass().getResourceAsStream(pathToMap);
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: /maps/classic.txt");
            }

            BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = buffer.readLine()) != null) {
                tileMap.add(line);
                mapColumnsCount = line.length();    
            }

            mapRowsCount = tileMap.size();

            boardWidth = mapColumnsCount * tileSize;
            boardHeight = mapRowsCount * tileSize;

            buffer.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unable to load map. Please verify if the path given for map is correct.");
            System.exit(1);
        }
    }

    public void setBlocks() {
        for (int r = 0; r < mapRowsCount; r++) {
            for (int c = 0; c < mapColumnsCount; c++) {
                char tileChar = tileMap.get(r).charAt(c);

                int x = c*tileSize;
                int y = r*tileSize;

                switch (tileChar) {
                    case 'X':
                        Block wall = new Block(x, y, tileChar, wallImage, tileSize);
                        walls.add(wall);
                        break;
                    case 'P':
                        pacman = new Block(x, y, tileChar, pacmanImage, characterSize);
                        break;
                    case 'C':
                        Block cherry = new Block(x, y, tileChar, cherryFrames.get(0), tileSize);
                        cherries.add(cherry);
                        break;
                    case ' ':
                        Block food = new Block(x + ((tileSize - foodSize)/2), y + ((tileSize - foodSize)/2), tileChar, null, foodSize);
                        foods.add(food);
                        break;
                }

                if (ghostColorsInitials.contains(String.valueOf(tileChar))) {
                    Block ghost = new Block(x, y, tileChar, ghostImages.get(ghostColorsInitials.indexOf(String.valueOf(tileChar))), characterSize);
                    ghosts.add(ghost);
                }
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.width, null);
        }

        for (Block cherry : cherries) {
            g.drawImage(cherry.image, cherry.x, cherry.y, cherry.width, cherry.width, null);
        }    

        g.setColor(Color.WHITE);
        for (Block food : foods) {
            g.fillRect(food.x, food.y, food.width, food.width);
        }

        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.width, null);
        }

        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.width, null);
    }

    public void update() {
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        for (Block wall : walls) {
            if (booleanCollision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
            }
        }
    }

    public static boolean booleanCollision(Block blockA, Block blockB) {
        return blockA.x < blockB.x + blockB.width &&
               blockA.x + blockA.width > blockB.x &&
               blockA.y < blockB.y + blockB.width &&
               blockA.y + blockA.width > blockB.y;
    }

    public static BufferedImage rotateImage(BufferedImage image, int angle) {
        // Calculate the center of the image
        int width = image.getWidth();
        int height = image.getHeight();
        double radians = Math.toRadians(angle); // Convert angle to radians

        // Create a new image with enough space to hold the rotated image
        int newWidth = (int) Math.ceil(Math.abs(width * Math.cos(radians)) + Math.abs(height * Math.sin(radians)));
        int newHeight = (int) Math.ceil(Math.abs(height * Math.cos(radians)) + Math.abs(width * Math.sin(radians)));

        BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, image.getType());

        // Rotate the image
        Graphics2D g2d = rotatedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Set up rotation
        AffineTransform transform = new AffineTransform();
        transform.translate(newWidth / 2.0, newHeight / 2.0); // Move to the center
        transform.rotate(radians); // Rotate
        transform.translate(-width / 2.0, -height / 2.0); // Move back

        // Draw the original image onto the rotated image
        g2d.drawImage(image, transform, null);
        g2d.dispose();

        return rotatedImage;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        update();
        repaint();
    }



    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_RIGHT:
                pacman.updateDirection(180);
                break;
            case KeyEvent.VK_DOWN:
                pacman.updateDirection(270);
                break;
            case KeyEvent.VK_LEFT:
                pacman.updateDirection(0);
                break;
            case KeyEvent.VK_UP:
                pacman.updateDirection(90);
                break;
            default:
                break;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}
}
