package com.automata.gui;

import com.automata.model.PDA;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * panel that shows the PDA output in a nice format
 */
public class PDAOutputPanel extends JPanel {
    
    private JTextArea pdaDisplay;
    private JScrollPane scrollPane;
    private JButton copyButton;
    private JButton saveButton;
    private JLabel statusLabel;

    // diagram related
    private PDADiagramPanel diagramPanel;
    private JButton exportPngButton;
    private JButton exportDotButton;

    private JTabbedPane tabbedPane;
    
    /**
     * makes the panel where we show the PDA output
     */
    public PDAOutputPanel() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    /**
     * sets up all the components
     */
    private void initializeComponents() {
        // where we show the PDA
        pdaDisplay = new JTextArea(25, 40);
        pdaDisplay.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        pdaDisplay.setEditable(false);
        pdaDisplay.setBackground(Color.WHITE);
        pdaDisplay.setForeground(Color.BLACK);
        pdaDisplay.setMargin(new Insets(10, 10, 10, 10));
        
        scrollPane = new JScrollPane(pdaDisplay);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // buttons for copy and save
        copyButton = new JButton("Copy to Clipboard");
        copyButton.setToolTipText("Copy the PDA output to clipboard");
        copyButton.setEnabled(false);
        
        saveButton = new JButton("Save to File");
        saveButton.setToolTipText("Save the PDA output to a text file");
        saveButton.setEnabled(false);
        
        // status message
        statusLabel = new JLabel("PDA output will appear here after conversion");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.ITALIC));
        statusLabel.setForeground(Color.GRAY);

        // diagram panel and export buttons
        diagramPanel = new PDADiagramPanel();
        exportPngButton = new JButton("Export Diagram PNG");
        exportPngButton.setToolTipText("Export the diagram view as a PNG image");
        exportPngButton.setEnabled(false);

        exportDotButton = new JButton("Export DOT");
        exportDotButton.setToolTipText("Export the PDA as a Graphviz DOT file");
        exportDotButton.setEnabled(false);

        // tabbed pane will host text and diagram
        tabbedPane = new JTabbedPane();
    }
    
    /**
     * arranges everything on the panel
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(new TitledBorder("Pushdown Automaton Output"));
        
        // prepare the tabbed pane
        tabbedPane.addTab("Text", scrollPane);
        tabbedPane.addTab("Diagram", diagramPanel);

        // panel with buttons (left) and diagram controls (right)
        JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftButtonPanel.add(copyButton);
        leftButtonPanel.add(saveButton);

        JPanel diagramControls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        diagramControls.add(exportDotButton);
        diagramControls.add(exportPngButton);

        // Bottom panel with left buttons and diagram controls on the right, plus status in center
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(leftButtonPanel, BorderLayout.WEST);
        bottomPanel.add(statusLabel, BorderLayout.CENTER);
        bottomPanel.add(diagramControls, BorderLayout.EAST);
        
        add(tabbedPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        // show helpful text when there's nothing
        displayEmptyMessage();
    }
    
    /**
     * Sets up event handlers.
     */
    private void setupEventHandlers() {
        // Copy button action
        copyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyToClipboard();
            }
        });
        
        // Save button action
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveToFile();
            }
        });

        // Export PNG action
        exportPngButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportDiagramAsPng();
            }
        });

        // Export DOT action
        exportDotButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportPdaAsDot();
            }
        });
    }
    
    /**
     * Displays a PDA in the output area.
     * @param pda The PDA to display
     */
    public void displayPDA(PDA pda) {
        if (pda == null) {
            displayEmptyMessage();
            return;
        }
        
        String formattedOutput = pda.getFormattedOutput();
        pdaDisplay.setText(formattedOutput);
        pdaDisplay.setCaretPosition(0);
        
        // Enable buttons
        copyButton.setEnabled(true);
        saveButton.setEnabled(true);
        exportPngButton.setEnabled(true);
        exportDotButton.setEnabled(true);
        
        // Update diagram
        diagramPanel.setPDA(pda);
        
        // Update status
        int transitionCount = pda.getTransitions().size();
        int stateCount = pda.getStates().size();
        statusLabel.setText(String.format("PDA generated: %d states, %d transitions", 
                                         stateCount, transitionCount));
        statusLabel.setForeground(Color.BLACK);
    }
    
    /**
     * Appends text to the current output.
     * @param text The text to append
     */
    public void appendText(String text) {
        if (text != null && !text.isEmpty()) {
            pdaDisplay.append(text);
            
            // Enable buttons if not already enabled
            copyButton.setEnabled(true);
            saveButton.setEnabled(true);
        }
    }
    
    /**
     * Clears the output area.
     */
    public void clearOutput() {
        displayEmptyMessage();
        copyButton.setEnabled(false);
        saveButton.setEnabled(false);
        exportPngButton.setEnabled(false);
        exportDotButton.setEnabled(false);
        statusLabel.setText("PDA output will appear here after conversion");
        statusLabel.setForeground(Color.GRAY);
    }
    
    /**
     * Displays an empty message in the output area.
     */
    private void displayEmptyMessage() {
        String emptyMessage = "CFG to PDA Converter\n" +
                             "===================\n\n" +
                             "Instructions:\n" +
                             "1. Enter your Context-Free Grammar in the left panel\n" +
                             "2. Click 'Validate' to check the grammar syntax\n" +
                             "3. Click 'Convert' to generate the equivalent PDA\n" +
                             "4. The resulting PDA will be displayed here\n\n" +
                             "Grammar Format:\n" +
                             "- Use format: A -> alpha (one rule per line)\n" +
                             "- Non-terminals: uppercase letters (A, B, S, etc.)\n" +
                             "- Terminals: lowercase letters, digits, symbols\n" +
                             "- Epsilon productions: use 'epsilon'\n" +
                             "- Comments: lines starting with #\n\n" +
                             "Example:\n" +
                             "S -> a S b\n" +
                             "S -> epsilon\n\n" +
                             "This grammar generates the language {a^n b^n | n >= 0}";
        
        pdaDisplay.setText(emptyMessage);
        pdaDisplay.setCaretPosition(0);
    }
    
    /**
     * Copies the PDA output to the system clipboard.
     */
    private void copyToClipboard() {
        String text = pdaDisplay.getText();
        if (text != null && !text.isEmpty()) {
            java.awt.datatransfer.StringSelection stringSelection = 
                new java.awt.datatransfer.StringSelection(text);
            java.awt.datatransfer.Clipboard clipboard = 
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            
            // Show confirmation
            JOptionPane.showMessageDialog(this, 
                "PDA output copied to clipboard!", 
                "Copied", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Saves the PDA output to a text file.
     */
    private void saveToFile() {
        String text = pdaDisplay.getText();
        if (text == null || text.isEmpty()) {
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save PDA Output");
        fileChooser.setSelectedFile(new java.io.File("pda_output.txt"));
        
        // Add file filter
        javax.swing.filechooser.FileNameExtensionFilter filter = 
            new javax.swing.filechooser.FileNameExtensionFilter("Text files (*.txt)", "txt");
        fileChooser.setFileFilter(filter);
        
        int userSelection = fileChooser.showSaveDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            
            // Ensure .txt extension
            if (!fileToSave.getName().toLowerCase().endsWith(".txt")) {
                fileToSave = new java.io.File(fileToSave.getAbsolutePath() + ".txt");
            }
            
            try {
                java.io.FileWriter writer = new java.io.FileWriter(fileToSave);
                writer.write(text);
                writer.close();
                
                JOptionPane.showMessageDialog(this, 
                    "PDA output saved to: " + fileToSave.getAbsolutePath(), 
                    "Saved", 
                    JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (java.io.IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error saving file: " + e.getMessage(), 
                    "Save Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Export the diagram panel as a PNG image (file chooser).
     */
    private void exportDiagramAsPng() {
        try {
            BufferedImage img = diagramPanel.toImage();
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("pda-diagram.png"));
            int rc = chooser.showSaveDialog(SwingUtilities.getWindowAncestor(this));
            if (rc == JFileChooser.APPROVE_OPTION) {
                File out = chooser.getSelectedFile();
                // ensure .png extension
                if (!out.getName().toLowerCase().endsWith(".png")) {
                    out = new File(out.getAbsolutePath() + ".png");
                }
                ImageIO.write(img, "PNG", out);
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                    "Saved diagram to " + out.getAbsolutePath());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                "Error exporting PNG: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Export the PDA as a Graphviz DOT file (file chooser).
     */
    private void exportPdaAsDot() {
        try {
            // Need a PDA object - get it from the diagram panel's current pda
            // We don't expose the internal PDA field in PDADiagramPanel, but we can re-create
            // by grabbing the text. Simpler approach: require callers to call displayPDA(pda)
            // so we can store the last displayed PDA. We'll store it now as a small enhancement.
            // To keep this file self-contained, attempt to derive dot from diagramPanel's pda via reflection:

            java.lang.reflect.Field f = diagramPanel.getClass().getDeclaredField("pda");
            f.setAccessible(true);
            Object p = f.get(diagramPanel);
            if (p == null) {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                    "No PDA available to export.", "Export Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String dot;
            try {
                java.lang.reflect.Method m = p.getClass().getMethod("toDot");
                Object res = m.invoke(p);
                dot = res != null ? res.toString() : null;
            } catch (NoSuchMethodException nsme) {
                // fallback: use getFormattedOutput / manual conversion
                java.lang.reflect.Method m2 = p.getClass().getMethod("getFormattedOutput");
                Object res2 = m2.invoke(p);
                dot = "// DOT export not available (no toDot method). PDA textual output below:\n" +
                      (res2 != null ? res2.toString() : "");
            }

            if (dot == null) {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                    "Failed to produce DOT output.", "Export Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("pda.dot"));
            int rc = chooser.showSaveDialog(SwingUtilities.getWindowAncestor(this));
            if (rc == JFileChooser.APPROVE_OPTION) {
                File out = chooser.getSelectedFile();
                // ensure .dot ext
                if (!out.getName().toLowerCase().endsWith(".dot")) {
                    out = new File(out.getAbsolutePath() + ".dot");
                }
                try (java.io.FileWriter fw = new java.io.FileWriter(out)) {
                    fw.write(dot);
                }
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                    "Saved DOT to " + out.getAbsolutePath());
            }
        } catch (NoSuchFieldException nsf) {
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                "Cannot access current PDA for DOT export.", "Export Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                "Error exporting DOT: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Gets the display text area component (for advanced operations).
     * @return The JTextArea component
     */
    public JTextArea getDisplayTextArea() {
        return pdaDisplay;
    }
    
    /**
     * Sets the font size for the display area.
     * @param size The font size
     */
    public void setDisplayFontSize(int size) {
        Font currentFont = pdaDisplay.getFont();
        Font newFont = new Font(currentFont.getName(), currentFont.getStyle(), size);
        pdaDisplay.setFont(newFont);
    }
    
    /**
     * Highlights specific text in the display area.
     * @param searchText The text to highlight
     */
    public void highlightText(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            return;
        }
        
        String content = pdaDisplay.getText();
        int index = content.indexOf(searchText);
        
        if (index >= 0) {
            pdaDisplay.setCaretPosition(index);
            pdaDisplay.select(index, index + searchText.length());
            pdaDisplay.requestFocusInWindow();
        }
    }
}
