package simplejavaloopgame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class Simple2DGame extends JPanel {

    // ── Player ────────────────────────────────────────────────────────────────
    int playerX = 180;
    final int playerY = 340;
    final int playerSize = 40;

    // ── Blocks ────────────────────────────────────────────────────────────────
    final int blockSize = 40;
    final int speed = 5;
    CopyOnWriteArrayList<Rectangle> redBlocks = new CopyOnWriteArrayList<>();

    // ── Game state ────────────────────────────────────────────────────────────
    int rowCount = 0;
    final int maxRows = 10;
    boolean gameOver = false;
    boolean gameWin  = false;
    int score = 0;

    // ── Visuals ───────────────────────────────────────────────────────────────
    CopyOnWriteArrayList<Star>     stars    = new CopyOnWriteArrayList<>();
    CopyOnWriteArrayList<Particle> particles = new CopyOnWriteArrayList<>();
    float               playerGlow = 0f;   // oscillating glow intensity
    float               glowDir    = 0.04f;
    int                 flashFrames = 0;   // red-screen flash on hit

    // ── Palette ───────────────────────────────────────────────────────────────
    static final Color BG_TOP    = new Color(5,  5, 20);
    static final Color BG_BOT    = new Color(10, 5, 35);
    static final Color PLAYER_C  = new Color(80, 160, 255);
    static final Color BLOCK_C   = new Color(255, 60,  60);
    static final Color GLOW_P    = new Color(80, 160, 255, 40);
    static final Color GLOW_B    = new Color(255, 60,  60, 50);
    static final Color HUD_COLOR = new Color(200, 220, 255);

    Random rand = new Random();

    // ─────────────────────────────────────────────────────────────────────────
    // Inner classes
    // ─────────────────────────────────────────────────────────────────────────

    static class Star {
        float x, y, size, brightness, speed;
        Star(int w, Random r) {
            x = r.nextFloat() * w;
            y = r.nextFloat() * 400;
            size = r.nextFloat() * 2f + 0.5f;
            brightness = r.nextFloat();
            speed = r.nextFloat() * 0.4f + 0.1f;
        }
        void update() { brightness += (Math.random() > 0.5 ? 0.02f : -0.02f); brightness = Math.max(0.2f, Math.min(1f, brightness)); }
    }

    static class Particle {
        float x, y, vx, vy, life, maxLife, size;
        Color color;
        Particle(float x, float y, Random r, Color c) {
            this.x = x; this.y = y;
            vx = (r.nextFloat() - 0.5f) * 3f;
            vy = r.nextFloat() * -2f - 0.5f;
            maxLife = life = r.nextInt(18) + 8;
            size = r.nextFloat() * 4f + 2f;
            color = c;
        }
        void update() { x += vx; y += vy; vy += 0.15f; life--; }
        boolean dead() { return life <= 0; }
        float alpha() { return life / maxLife; }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────

    public Simple2DGame() {
        setFocusable(true);

        // Seed stars
        for (int i = 0; i < 80; i++) stars.add(new Star(400, rand));

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (!gameOver && !gameWin) {
                    int key = e.getKeyCode();
                    if (key == KeyEvent.VK_LEFT  && playerX > 0)                    playerX -= 20;
                    if (key == KeyEvent.VK_RIGHT  && playerX < getWidth()-playerSize) playerX += 20;
                }
            }
        });

        JButton restartButton = new JButton("RESTART");
        styleButton(restartButton);
        restartButton.setBounds(140, 210, 120, 40);
        restartButton.addActionListener(e -> restartGame());
        setLayout(null);
        add(restartButton);
        restartButton.setVisible(false);
    }

    private void styleButton(JButton b) {
        b.setForeground(new Color(200, 220, 255));
        b.setBackground(new Color(20, 20, 60));
        b.setFont(new Font("Monospaced", Font.BOLD, 14));
        b.setBorder(BorderFactory.createLineBorder(new Color(80, 130, 255), 2));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Paint
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int W = getWidth(), H = getHeight();

        // ── Background gradient ──────────────────────────────────────────────
        GradientPaint bg = new GradientPaint(0, 0, BG_TOP, 0, H, BG_BOT);
        g2.setPaint(bg);
        g2.fillRect(0, 0, W, H);

        // ── Red flash overlay ────────────────────────────────────────────────
        if (flashFrames > 0) {
            g2.setColor(new Color(255, 0, 0, Math.min(120, flashFrames * 12)));
            g2.fillRect(0, 0, W, H);
        }

        // ── Stars ────────────────────────────────────────────────────────────
        for (Star s : stars) {
            int alpha = (int)(s.brightness * 200);
            g2.setColor(new Color(200, 210, 255, alpha));
            g2.fillOval((int)s.x, (int)s.y, (int)s.size, (int)s.size);
        }

        // ── Grid lines (subtle) ──────────────────────────────────────────────
        g2.setColor(new Color(255, 255, 255, 8));
        for (int x = 0; x < W; x += 40) g2.drawLine(x, 0, x, H);
        for (int y = 0; y < H; y += 40) g2.drawLine(0, y, W, y);

        // ── Particles ────────────────────────────────────────────────────────
        for (Particle p : particles) {
            int a = (int)(p.alpha() * 200);
            g2.setColor(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), a));
            g2.fillOval((int)(p.x - p.size/2), (int)(p.y - p.size/2), (int)p.size, (int)p.size);
        }

        // ── Red blocks ───────────────────────────────────────────────────────
        for (Rectangle r : redBlocks) {
            // Glow halo
            drawGlowRect(g2, r.x, r.y, r.width, r.height, GLOW_B, 10);
            // Body
            GradientPaint bp = new GradientPaint(r.x, r.y, new Color(255, 100, 80),
                                                  r.x, r.y + r.height, new Color(200, 30, 30));
            g2.setPaint(bp);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
            // Shine
            g2.setColor(new Color(255, 255, 255, 50));
            g2.fillRoundRect(r.x + 6, r.y + 4, r.width - 16, 8, 4, 4);
        }

        // ── Player ───────────────────────────────────────────────────────────
        // Pulsing glow
        int glowSize = (int)(playerGlow * 18);
        drawGlowRect(g2, playerX - glowSize/2, playerY - glowSize/2,
                     playerSize + glowSize, playerSize + glowSize, GLOW_P, 14);

        GradientPaint pp = new GradientPaint(playerX, playerY, new Color(120, 190, 255),
                                              playerX, playerY + playerSize, new Color(40, 100, 220));
        g2.setPaint(pp);
        g2.fillRoundRect(playerX + 2, playerY + 2, playerSize - 4, playerSize - 4, 10, 10);

        // Player border
        g2.setColor(new Color(160, 210, 255, 180));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(playerX + 2, playerY + 2, playerSize - 4, playerSize - 4, 10, 10);

        // Player shine
        g2.setColor(new Color(255, 255, 255, 80));
        g2.fillRoundRect(playerX + 7, playerY + 5, playerSize - 18, 8, 4, 4);

        // ── HUD ──────────────────────────────────────────────────────────────
        drawHUD(g2, W);

        // ── Game Over / Win overlay ───────────────────────────────────────────
        if (gameOver || gameWin) drawEndScreen(g2, W, H);
    }

    private void drawGlowRect(Graphics2D g2, int x, int y, int w, int h, Color c, int layers) {
        for (int i = layers; i > 0; i--) {
            int alpha = (int)((1f - (float)i / layers) * c.getAlpha() * 0.6f);
            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, alpha)));
            g2.fillRoundRect(x - i, y - i, w + i*2, h + i*2, 12, 12);
        }
    }

    private void drawHUD(Graphics2D g2, int W) {
        // Semi-transparent HUD bar
        g2.setColor(new Color(10, 10, 40, 160));
        g2.fillRoundRect(8, 8, 180, 30, 8, 8);
        g2.setColor(new Color(80, 130, 255, 120));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(8, 8, 180, 30, 8, 8);

        g2.setFont(new Font("Monospaced", Font.BOLD, 13));
        g2.setColor(HUD_COLOR);
        g2.drawString("ROWS: " + score + " / " + maxRows, 18, 28);

        // Progress bar
        int barW = W - 20;
        g2.setColor(new Color(30, 30, 70));
        int H = 0;
        g2.fillRoundRect(10, H - 18, barW, 8, 6, 6);
        if (maxRows > 0) {
            float pct = Math.min(1f, (float) score / maxRows);
            GradientPaint barGrad = new GradientPaint(10, 0, new Color(60, 120, 255), 10 + (int)(barW * pct), 0, new Color(120, 220, 255));
            g2.setPaint(barGrad);
            g2.fillRoundRect(10, H - 18, (int)(barW * pct), 8, 6, 6);
        }
    }

    private void drawEndScreen(Graphics2D g2, int W, int H) {
        // Dimmed overlay
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRect(0, 0, W, H);

        String msg   = gameWin ? "YOU WIN!" : "GAME OVER";
        Color  color = gameWin ? new Color(100, 255, 160) : new Color(255, 80, 80);

        // Glow text
        g2.setFont(new Font("Monospaced", Font.BOLD, 36));
        FontMetrics fm = g2.getFontMetrics();
        int tx = (W - fm.stringWidth(msg)) / 2;
        for (int i = 6; i > 0; i--) {
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 18));
            g2.drawString(msg, tx - i, 160 + i);
            g2.drawString(msg, tx + i, 160 - i);
        }
        g2.setColor(color);
        g2.drawString(msg, tx, 160);

        // Sub-text
        g2.setFont(new Font("Monospaced", Font.PLAIN, 14));
        g2.setColor(new Color(160, 180, 220));
        String sub = "Score: " + score + " row" + (score == 1 ? "" : "s");
        g2.drawString(sub, (W - g2.getFontMetrics().stringWidth(sub)) / 2, 186);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Game loop
    // ─────────────────────────────────────────────────────────────────────────

    public void gameLoop() throws InterruptedException {
        while (true) {
            if (!gameOver && !gameWin) {

                // Spawn rows
                if (rowCount < maxRows && (redBlocks.isEmpty() || redBlocks.get(redBlocks.size()-1).y > 50)) {
                    int blocksInRow = rand.nextInt(3) + 1;
                    for (int i = 0; i < blocksInRow; i++) {
                        int x = rand.nextInt(Math.max(1, getWidth() - blockSize));
                        redBlocks.add(new Rectangle(x, 0, blockSize, blockSize));
                    }
                    rowCount++;
                    score = rowCount;
                }

                // Move blocks
                for (Rectangle r : redBlocks) r.y += speed;

                // Emit player trail particles every few frames
                if (rand.nextInt(3) == 0) {
                    particles.add(new Particle(
                        playerX + playerSize / 2f + (rand.nextFloat() - 0.5f) * 8,
                        playerY + playerSize,
                        rand, new Color(60, 120, 255)));
                }

                // Collision + block death particles
                for (Rectangle r : redBlocks) {
                    if (r.intersects(new Rectangle(playerX, playerY, playerSize, playerSize))) {
                        gameOver = true;
                        flashFrames = 12;
                        spawnExplosion(r.x + r.width/2f, r.y + r.height/2f, BLOCK_C);
                        spawnExplosion(playerX + playerSize/2f, playerY + playerSize/2f, PLAYER_C);
                    }
                }

                // Remove off-screen blocks, emit dust
                CopyOnWriteArrayList<Rectangle> offScreen = new CopyOnWriteArrayList<>();
                for (Rectangle r : redBlocks) {
                    if (r.y > getHeight()) {
                        spawnDust(r.x + r.width/2f, getHeight(), new Color(255, 60, 60));
                        offScreen.add(r);
                    }
                }
                redBlocks.removeAll(offScreen);

                // Win check
                if (rowCount >= maxRows && redBlocks.isEmpty()) gameWin = true;

            } else {
                for (Component c : getComponents())
                    if (c instanceof JButton) c.setVisible(true);
            }

            // Animate stars
            for (Star s : stars) s.update();

            // Animate glow
            playerGlow += glowDir;
            if (playerGlow > 1f || playerGlow < 0f) glowDir = -glowDir;

            // Decay flash
            if (flashFrames > 0) flashFrames--;

            // Update & prune particles (collect removals, then bulk-remove)
            CopyOnWriteArrayList<Particle> dead = new CopyOnWriteArrayList<>();
            for (Particle p : particles) { p.update(); if (p.dead()) dead.add(p); }
            particles.removeAll(dead);

            repaint();
            Thread.sleep(30);
        }
    }

    private void spawnExplosion(float x, float y, Color c) {
        for (int i = 0; i < 18; i++) particles.add(new Particle(x, y, rand, c));
    }

    private void spawnDust(float x, float y, Color c) {
        for (int i = 0; i < 6; i++) particles.add(new Particle(x, y, rand, c));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Restart
    // ─────────────────────────────────────────────────────────────────────────

    public void restartGame() {
        playerX = 180;
        redBlocks.clear();
        particles.clear();
        rowCount = 0;
        score = 0;
        gameOver = false;
        gameWin  = false;
        flashFrames = 0;
        for (Component c : getComponents())
            if (c instanceof JButton) c.setVisible(false);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Entry point
    // ─────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) throws InterruptedException {
        JFrame frame = new JFrame("Falling Blocks");
        frame.setUndecorated(false);
        Simple2DGame game = new Simple2DGame();
        frame.add(game);
        frame.setSize(400, 420);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        game.gameLoop();
    }
}