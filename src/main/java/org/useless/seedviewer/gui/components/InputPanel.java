package org.useless.seedviewer.gui.components;

import org.useless.seedviewer.Global;
import org.useless.seedviewer.gui.SeedViewer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InputPanel extends JPanel {
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
    private final SeedViewer seedViewer;

    public JLabel titleLabel;

    public JCheckBox slimeChunksBox;
    public JCheckBox showBordersBox;
    public JCheckBox showCrosshairBox;
    public JTextField seedInputBox;
    public JButton screenshot;

    private final List<Component> resizeList = new ArrayList<>();

    public InputPanel(SeedViewer seedViewer) {
        this.seedViewer = seedViewer;
        this.setLayout(null); // using no layout managers
        this.setBorder(new LineBorder(Color.BLACK, 1));
    }

    public void setup() {
        titleLabel = new JLabel("User Controls: ");
        this.add(titleLabel);

        slimeChunksBox = new JCheckBox("Slime Chunks");
        slimeChunksBox.setSelected(seedViewer.viewport.showSlimeChunks.get());
        slimeChunksBox.addChangeListener(e -> seedViewer.viewport.showSlimeChunks.set(slimeChunksBox.isSelected()));
        slimeChunksBox.addChangeListener(e -> seedViewer.viewport.repaint());
        showBordersBox = new JCheckBox("Chunk Borders");
        showBordersBox.setSelected(seedViewer.viewport.showBiomeBorders.get());
        showBordersBox.addChangeListener(e -> seedViewer.viewport.showBiomeBorders.set(showBordersBox.isSelected()));
        showBordersBox.addChangeListener(e -> seedViewer.viewport.repaint());
        showCrosshairBox = new JCheckBox("Enable Cross-hair");
        showCrosshairBox.setSelected(seedViewer.viewport.showCrosshair.get());
        showCrosshairBox.addChangeListener(e -> seedViewer.viewport.showCrosshair.set(showCrosshairBox.isSelected()));
        showCrosshairBox.addChangeListener(e -> seedViewer.viewport.repaint());

        this.add(slimeChunksBox);
        resizeList.add(slimeChunksBox);
        this.add(showBordersBox);
        resizeList.add(showBordersBox);
        this.add(showCrosshairBox);
        resizeList.add(showCrosshairBox);

        seedInputBox = new JTextField(seedViewer.seed.toString());
        seedInputBox.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long boxSeed;
                try {
                    boxSeed = Long.parseLong(seedInputBox.getText());
                } catch (NumberFormatException exception) {
                    boxSeed = seedInputBox.getText().hashCode();
                }
                if (boxSeed != seedViewer.seed.get()) {
                    seedViewer.seed.set(boxSeed);
                    seedViewer.viewport.chunkFrames.clear();
                    seedViewer.viewport.viewX.set(0F);
                    seedViewer.viewport.viewZ.set(0F);
                    seedViewer.needsResize = true;
                }
            }
        });

        this.add(seedInputBox);
        resizeList.add(seedInputBox);

        screenshot = new JButton("Save Screenshot");
        screenshot.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final File screenShotFolder = new File("screenshots");
                screenShotFolder.mkdirs();
                BufferedImage viewportImage = new BufferedImage(seedViewer.viewport.getWidth(), seedViewer.viewport.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics g = viewportImage.getGraphics();
                seedViewer.viewport.paintToGraphics(g);
                g.dispose();

                String name = dateFormat.format(new Date());
                File destFile;
                int k = 1;
                while ((destFile = new File(screenShotFolder, name + (k != 1 ? "_" + k : "") + ".png")).exists()) {
                    k++;
                }
                Global.LOGGER.info("Attempting to save screenshot to '{}'!", destFile);
                try {
                    destFile.createNewFile();
                    ImageIO.write(viewportImage, "png", destFile);
                } catch (IOException ex) {
                    Global.LOGGER.error("Failed to save screenshot to '{}'!", destFile, ex);
                }
            }
        });
        this.add(screenshot);
        resizeList.add(screenshot);
    }

    public void onResize(Rectangle newDimensions) {
        setBounds(newDimensions.x, newDimensions.y, newDimensions.width, newDimensions.height);

        final int boxHeight = 15;
        final int padding = 5;
        final int textHeight = 30;

        titleLabel.setBounds(padding, padding, newDimensions.width - padding * 2, 15);

        int lastY = titleLabel.getY() + titleLabel.getHeight() + padding * 2;
        for (Component c : resizeList) {
            if (c instanceof JCheckBox) {
                c.setBounds(padding, lastY, newDimensions.width - padding * 2, boxHeight);
                lastY += boxHeight;
            } else if (c instanceof JTextField) {
                c.setBounds(padding, lastY, newDimensions.width - padding * 2, textHeight);
                lastY += textHeight;
            } else if (c instanceof JButton) {
                c.setBounds(padding, lastY, newDimensions.width - padding * 2, textHeight);
                lastY += textHeight;
            }
        }
    }
}
