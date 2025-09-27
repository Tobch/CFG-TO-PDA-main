package com.automata.gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * panel for inputting grammar text and displaying validation feedback
 */
public class CFGInputPanel extends JPanel {
    
    private JTextArea grammarInput;
    private JTextArea validationOutput;
    private JLabel statusLabel;
    private JScrollPane inputScrollPane;
    private JScrollPane validationScrollPane;
    
    /**
     * makes the panel for typing in grammars
     */
    public CFGInputPanel() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    /**
     * sets up all the components we need
     */
    private void initializeComponents() {
        // where you type the grammar
        grammarInput = new JTextArea(15, 30);
        grammarInput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        grammarInput.setTabSize(4);
        grammarInput.setLineWrap(false);
        grammarInput.setWrapStyleWord(false);
        
        // add some margins so it looks nice
        grammarInput.setMargin(new Insets(5, 5, 5, 5));
        
        inputScrollPane = new JScrollPane(grammarInput);
        inputScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        inputScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // where we show validation messages
        validationOutput = new JTextArea(8, 30);
        validationOutput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        validationOutput.setEditable(false);
        validationOutput.setBackground(getBackground());
        validationOutput.setForeground(Color.DARK_GRAY);
        
        validationScrollPane = new JScrollPane(validationOutput);
        validationScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        validationScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // status message at the bottom
        statusLabel = new JLabel("Enter your Context-Free Grammar above");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.ITALIC));
        statusLabel.setForeground(Color.GRAY);
    }
    
    /**
     * arranges everything on the panel
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(new TitledBorder("Context-Free Grammar Input"));
        
        // make the main panel with input and validation stuff
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // input area with a nice title
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Grammar Rules"));
        inputPanel.add(inputScrollPane, BorderLayout.CENTER);
        
        // add some help text
        JLabel helpLabel = new JLabel("<html><small>" +
            "Format: A -> alpha (one rule per line)<br>" +
            "Use 'epsilon' for ε-productions<br>" +
            "Comments start with #" +
            "</small></html>");
        helpLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        inputPanel.add(helpLabel, BorderLayout.SOUTH);
        
        // validation area with title
        JPanel validationPanel = new JPanel(new BorderLayout());
        validationPanel.setBorder(BorderFactory.createTitledBorder("Validation Messages"));
        validationPanel.add(validationScrollPane, BorderLayout.CENTER);
        
        // split the input and validation areas
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(inputPanel);
        splitPane.setBottomComponent(validationPanel);
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.7);
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    /**
     * sets up the event handlers so things respond to clicks and typing
     */
    private void setupEventHandlers() {
        // listen for key presses to update stuff in real time
        grammarInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateStatus();
            }
        });
        
        // Add document listener for text changes
        grammarInput.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateStatus();
            }
            
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateStatus();
            }
            
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateStatus();
            }
        });
    }
    
    /**
     * Updates the status label based on input content.
     */
    private void updateStatus() {
        String text = grammarInput.getText().trim();
        if (text.isEmpty()) {
            statusLabel.setText("Enter your Context-Free Grammar above");
            statusLabel.setForeground(Color.GRAY);
        } else {
            int lines = text.split("\\n").length;
            int nonEmptyLines = 0;
            for (String line : text.split("\\n")) {
                if (!line.trim().isEmpty() && !line.trim().startsWith("#")) {
                    nonEmptyLines++;
                }
            }
            statusLabel.setText(String.format("Lines: %d, Rules: %d", lines, nonEmptyLines));
            statusLabel.setForeground(Color.BLACK);
        }
    }
    
    /**
     * Gets the grammar text from the input area.
     * @return The grammar text
     */
    public String getGrammarText() {
        return grammarInput.getText();
    }
    
    /**
     * Sets the grammar text in the input area.
     * @param text The grammar text to set
     */
    public void setGrammarText(String text) {
        grammarInput.setText(text);
        grammarInput.setCaretPosition(0);
        updateStatus();
    }
    
    /**
     * Clears the input area.
     */
    public void clearInput() {
        grammarInput.setText("");
        updateStatus();
    }
    
    /**
     * Displays validation results in the validation area.
     * @param messages List of validation messages
     */
    public void displayValidationResults(List<String> messages) {
        if (messages == null || messages.isEmpty()) {
            validationOutput.setText("No validation messages.");
            validationOutput.setForeground(Color.GRAY);
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        boolean hasErrors = false;
        boolean hasWarnings = false;
        
        for (String message : messages) {
            sb.append(message).append("\n");
            
            if (message.toLowerCase().contains("error")) {
                hasErrors = true;
            } else if (message.toLowerCase().contains("warning")) {
                hasWarnings = true;
            }
        }
        
        validationOutput.setText(sb.toString());
        
        // Set color based on message type
        if (hasErrors) {
            validationOutput.setForeground(Color.RED);
        } else if (hasWarnings) {
            validationOutput.setForeground(Color.ORANGE);
        } else {
            validationOutput.setForeground(Color.BLUE);
        }
        
        // Scroll to top
        validationOutput.setCaretPosition(0);
    }
    
    /**
     * Clears the validation results area.
     */
    public void clearValidationResults() {
        validationOutput.setText("");
        validationOutput.setForeground(Color.GRAY);
    }
    
    /**
     * Focuses the input area.
     */
    public void focusInput() {
        grammarInput.requestFocusInWindow();
    }
    
    /**
     * Gets the input text area component (for advanced operations).
     * @return The JTextArea component
     */
    public JTextArea getInputTextArea() {
        return grammarInput;
    }
    
    /**
     * Adds sample text to help users understand the format.
     */
    public void showSampleFormat() {
        if (grammarInput.getText().trim().isEmpty()) {
            String sample = "# Example: Simple balanced strings grammar\n" +
                           "# Language: {a^n b^n | n >= 0}\n" +
                           "S -> a S b\n" +
                           "S -> epsilon\n\n" +
                           "# Replace this with your own grammar rules";
            setGrammarText(sample);
        }
    }
}