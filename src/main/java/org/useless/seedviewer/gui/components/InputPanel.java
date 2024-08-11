package org.useless.seedviewer.gui.components;

import org.useless.seedviewer.Global;
import org.useless.seedviewer.collections.ObjectWrapper;
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
    public JButton openWorld;
    public JButton closeWorld;

    private final List<Component> resizeList = new ArrayList<>();

    public InputPanel(SeedViewer seedViewer) {
        this.seedViewer = seedViewer;
        this.setLayout(null); // using no layout managers
        this.setBorder(new LineBorder(Color.BLACK, 1));
    }

    public void setup() {
        final int boxHeight = 15;
        final int textHeight = 15;
        final int textFieldHeight = 30;
        final int button = 30;

        titleLabel = new JLabel("User Controls: ");
        titleLabel.setSize(0, textHeight);
        this.add(titleLabel);

        slimeChunksBox = new JCheckBox("Slime Chunks");
        slimeChunksBox.setSize(0, boxHeight);
        slimeChunksBox.setSelected(seedViewer.viewport.showSlimeChunks.get());
        slimeChunksBox.addChangeListener(e -> seedViewer.viewport.showSlimeChunks.set(slimeChunksBox.isSelected()));
        slimeChunksBox.addChangeListener(e -> seedViewer.viewport.repaint());
        showBordersBox = new JCheckBox("Chunk Borders");
        showBordersBox.setSize(0, boxHeight);
        showBordersBox.setSelected(seedViewer.viewport.showBiomeBorders.get());
        showBordersBox.addChangeListener(e -> seedViewer.viewport.showBiomeBorders.set(showBordersBox.isSelected()));
        showBordersBox.addChangeListener(e -> seedViewer.viewport.repaint());
        showCrosshairBox = new JCheckBox("Enable Cross-hair");
        showCrosshairBox.setSize(0, boxHeight);
        showCrosshairBox.setSelected(seedViewer.viewport.showCrosshair.get());
        showCrosshairBox.addChangeListener(e -> seedViewer.viewport.showCrosshair.set(showCrosshairBox.isSelected()));
        showCrosshairBox.addChangeListener(e -> seedViewer.viewport.repaint());

        addManaged(slimeChunksBox);
        addManaged(showBordersBox);
        addManaged(showCrosshairBox);

        seedInputBox = new JTextField(seedViewer.viewport.seed.toString());
        seedInputBox.setSize(0, textFieldHeight);
        seedInputBox.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long boxSeed;
                try {
                    boxSeed = Long.parseLong(seedInputBox.getText());
                } catch (NumberFormatException exception) {
                    boxSeed = seedInputBox.getText().hashCode();
                }
                if (boxSeed != seedViewer.viewport.seed.get()) {
                    seedViewer.viewport.setSeed(boxSeed);
                }
            }
        });
        addManaged(seedInputBox);

        screenshot = new JButton("Save Screenshot");
        screenshot.setSize(0, button);
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
        addManaged(screenshot);

        openWorld = new JButton("Open World");
        openWorld.setSize(0, button);
        openWorld.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser worldSelector = new JFileChooser(new File("./"));

                worldSelector.setDialogTitle("Select World");
                worldSelector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                worldSelector.setAcceptAllFileFilterUsed(false);

                int result = worldSelector.showOpenDialog(seedViewer);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = worldSelector.getSelectedFile();
                    System.out.println("Selected file: " + selectedFile.getAbsolutePath());
                    seedViewer.viewport.world.set(selectedFile);

                } else {
                    seedViewer.viewport.world.set(null);
                }
            }
        });
        seedViewer.viewport.world.addChangeListener(newValue -> {
            if (newValue != null) {
                seedInputBox.setEnabled(false);
                seedInputBox.setVisible(false);

                closeWorld.setEnabled(true);
                closeWorld.setVisible(true);
                seedViewer.forceResize();
            } else {
                seedInputBox.setEnabled(true);
                seedInputBox.setVisible(true);

                closeWorld.setEnabled(false);
                closeWorld.setVisible(false);
                seedViewer.forceResize();
            }
        });
        addManaged(openWorld);

        closeWorld = new JButton("Close World");
        closeWorld.setSize(0, button);
        closeWorld.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                seedViewer.viewport.world.set(null);
            }
        });
        addManaged(closeWorld);

        if (seedViewer.viewport.world.get() == null) {
            seedInputBox.setEnabled(true);
            seedInputBox.setVisible(true);

            closeWorld.setEnabled(false);
            closeWorld.setVisible(false);
        } else {
            seedInputBox.setEnabled(false);
            seedInputBox.setVisible(false);

            closeWorld.setEnabled(true);
            closeWorld.setVisible(true);
        }
    }

    public void addManaged(Component c) {
        add(c);
        resizeList.add(c);
    }

    public void onResize(Rectangle newDimensions) {
        setBounds(newDimensions.x, newDimensions.y, newDimensions.width, newDimensions.height);

        final int edgePad = 5;
        final int elementPad = 2;
        titleLabel.setBounds(edgePad, edgePad, newDimensions.width - edgePad * 2, titleLabel.getHeight());

        int lastY = titleLabel.getY() + titleLabel.getHeight() + edgePad * 2;
        for (Component c : resizeList) {
            if (!c.isVisible()) continue;
            c.setBounds(edgePad, lastY, newDimensions.width - edgePad * 2, c.getHeight());
            lastY += c.getHeight() + elementPad;
        }
    }
}
