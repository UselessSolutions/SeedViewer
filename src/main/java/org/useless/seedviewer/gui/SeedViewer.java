package org.useless.seedviewer.gui;

import org.jetbrains.annotations.NotNull;
import org.useless.seedviewer.Global;
import org.useless.seedviewer.collections.ObjectWrapper;
import org.useless.seedviewer.bta.BTAChunkProvider;
import org.useless.seedviewer.collections.ChunkLocation;
import org.useless.seedviewer.gui.components.InfoPanel;
import org.useless.seedviewer.gui.components.InputPanel;
import org.useless.seedviewer.gui.components.Viewport;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class SeedViewer extends JFrame {
    // Static Configuration
    public static final int TICKS_PER_SECOND = 10;
    public static final int BEZEL = 15;

    // Storage
    public final Properties launchProperties;
    public volatile boolean needsResize = true;

    // Configuration

    // Components
    public final Viewport viewport;
    public final InputPanel inputPanel;
    public final InfoPanel infoPanel;

    public SeedViewer(Properties properties) {
        Global.LOGGER.info("Starting Seed Viewer!");
        this.launchProperties = properties;

        viewport = new Viewport(this);
        inputPanel = new InputPanel(this);
        infoPanel = new InfoPanel(this);


        initFrame();
        addComponents();



        new Thread(
            () -> {
                long tickCount = 0;
                while (true) {
                    try {
                        tick();
                        tickCount++;
                    } catch (Exception e){
                        Global.LOGGER.error("Exception when running tick!", e);
                    }

                    if (tickCount % (5 * TICKS_PER_SECOND) == 0) {
                        System.gc();
                    }

                    try {
                        Thread.sleep(1000/TICKS_PER_SECOND);
                    } catch (InterruptedException e) {
                        Global.LOGGER.error("Unexpected interrupt in tick thread!", e);
                    }
                }
            }
        ).start();
    }

    public void initFrame() {
        // Creating instance of JFrame
        Global.LOGGER.info("Initializing Frame");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setTitle("BTA Seed Viewer! Version: " + Global.VERSION);
        this.setSize(960, 720);
        this.setMinimumSize(new Dimension(480, 360));
        this.setResizable(true);
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                needsResize = true;
                viewport.repaint();
            }
        });
        this.setLayout(null); // using no layout managers
        this.setVisible(true); // making the frame visible

        Global.LOGGER.info("Loading Icons");
        try {
            List<Image> l = new ArrayList<>();
            l.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/1024.png")));
            l.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/512.png")));
            l.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/256.png")));
            l.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/128.png")));
            l.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/64.png")));
            l.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/32.png")));
            l.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/16.png")));
            this.setIconImages(l);
        } catch (Exception e) {
            Global.LOGGER.error("Failed to load icons!", e);
        }
    }

    public void addComponents() {
        // State labels
        Global.LOGGER.info("Creating Labels");
        infoPanel.setup();
        this.add(infoPanel);

        // Checkboxes
        Global.LOGGER.info("Creating Check boxes");
        inputPanel.setup();
        this.add(inputPanel);

        Global.LOGGER.info("Creating Image frame");
        viewport.setup();
        this.add(viewport);
    }

    public void initComponents() {
        if (!needsResize) return;
        Global.LOGGER.info("Initializing Components");
        needsResize = false;
        int screenWidth = this.getContentPane().getWidth();
        int screenHeight = this.getContentPane().getHeight();

        {
            int inWidth = screenWidth / 4;
            if (inWidth < 100) inWidth = 100;
            if (inWidth > 200) inWidth = 200;
            int inHeight = screenHeight - BEZEL * 2;
            inputPanel.onResize(new Rectangle(0, BEZEL, inWidth, inHeight/2));
            inputPanel.setVisible(true);
        }

        {
            int inWidth = screenWidth / 4;
            if (inWidth < 100) inWidth = 100;
            if (inWidth > 200) inWidth = 200;
            int inHeight = screenHeight - BEZEL * 2;
            infoPanel.onResize(new Rectangle(0, inputPanel.getY() + inputPanel.getHeight() - 1, inWidth, inHeight/2));
            infoPanel.setVisible(true);
        }

        {
            int viewWidth = screenWidth - (BEZEL * 2) - (inputPanel.isVisible() ? inputPanel.getWidth() : 0);
            int viewX = BEZEL + (inputPanel.isVisible() ? inputPanel.getWidth() : 0);
            viewport.onResize(
                new Rectangle(
                    viewX,
                    BEZEL,
                    viewWidth,
                    screenHeight - BEZEL * 2));
        }
    }

    public synchronized void tick() {
        initComponents();
        viewport.tick();
    }

    public static boolean isSlimeChunk(long worldSeed, @NotNull ChunkLocation location) {
        long slimeXOR = 0x3AD8025FL;
        Random slimerandom = new Random(worldSeed + (location.x * location.x * 0x4c1906L) + (location.x * 0x5ac0dbL) + (location.z * location.z * 0x4307a7L) + (location.z * 0x5f24fL) ^ slimeXOR);
        return slimerandom.nextInt(10) == 0;
    }
}
