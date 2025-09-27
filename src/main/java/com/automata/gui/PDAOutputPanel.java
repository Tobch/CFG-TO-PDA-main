package com.automata.gui;

import com.automata.model.PDA;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * panel that shows the PDA output in a nice format
 */
public class PDAOutputPanel extends JPanel {
    
    private JTextArea pdaDisplay;
    private JScrollPane scrollPane;
    private JButton copyButton;
    private JButton saveButton;
    private JLabel statusLabel;
    
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
    }
    
    /**
     * arranges everything on the panel
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(new TitledBorder("Pushdown Automaton Output"));
        
        // main area where we show stuff
        JPanel displayPanel = new JPanel(new BorderLayout());
        displayPanel.add(scrollPane, BorderLayout.CENTER);
        
        // panel with buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(copyButton);
        buttonPanel.add(saveButton);
        
        // Bottom panel with buttons and status
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.WEST);
        bottomPanel.add(statusLabel, BorderLayout.CENTER);
        
        add(displayPanel, BorderLayout.CENTER);
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