package org.uselesssolutions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.uselesssolutions.collections.ChunkLocation;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.Properties;
import java.util.Random;

public class SeedViewer {
    public final Properties launchProperties;
    private final JFrame mainFrame;
    private JLabel imageFrame;
    private JButton button;
    public SeedViewer(Properties properties) {
        this.launchProperties = properties;
        mainFrame = createFrame();
        initComponents(null);
    }

    public @NotNull JFrame createFrame() {
        // Creating instance of JFrame
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Creating instance of JButton
        button = new JButton(" GFG WebSite Click");

        imageFrame = new JLabel();

        // adding button in JFrame
        frame.add(button);
        frame.add(imageFrame);
        frame.setSize(960, 720);

        frame.setResizable(true);
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                initComponents(e);
            }
        }); // Register `initComponents` to run when frame is resized
        frame.setLayout(null); // using no layout managers
        frame.setVisible(true); // making the frame visible

        return frame;
    }

    public void initComponents(@Nullable ComponentEvent event) {
        int screenWidth = mainFrame.getContentPane().getWidth();
        int screenHeight = mainFrame.getContentPane().getHeight();

        int bWidth = (int) (screenWidth * 0.5f);
        int bHeight = (int) (screenHeight * 0.2f);
        button.setBounds((screenWidth - bWidth)/2, screenHeight - bHeight, bWidth, bHeight);

        int imgWidth = screenWidth - (30 * 2);
        int imgHeight = (screenHeight - bHeight - (30 * 2));

        int scale = 8;

        BufferedImage image = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                image.setRGB(x, y, (((x/scale) % 2 == 0) && ((y/scale) % 2 == 0)) ? 0xFF000000 : 0xFFCCCCCC);
            }
        }
        imageFrame.setIcon(new ImageIcon(image));
        imageFrame.setBounds(30, 30, imgWidth, imgHeight);
    }

    public static boolean isSlimeChunk(long worldSeed, @NotNull ChunkLocation location) {
        long slimeXOR = 0x3AD8025FL;
        Random slimerandom = new Random(worldSeed + (location.x * location.x * 0x4c1906L) + (location.x * 0x5ac0dbL) + (location.z * location.z * 0x4307a7L) + (location.z * 0x5f24fL) ^ slimeXOR);
        return slimerandom.nextInt(10) == 0;
    }
}
