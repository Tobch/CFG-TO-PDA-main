package com.automata.gui;

import com.automata.converter.PDAGenerator;
import com.automata.model.CFG;
import com.automata.model.PDA;
import com.automata.parser.CFGParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * the main window for the application
 * provides interface for inputting grammars and displaying PDA output
 */
public class MainWindow extends JFrame {
    
    private CFGInputPanel inputPanel;
    private PDAOutputPanel outputPanel;
    private ControlPanel controlPanel;
    
    private CFGParser parser;
    private PDAGenerator generator;
    
    /**
     * makes the main window with all the panels
     */
    public MainWindow() {
        this.parser = new CFGParser();
        this.generator = new PDAGenerator();
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupMenuBar();
        
        setTitle("CFG to PDA Converter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // try to set an icon if we got one
        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage("icon.png"));
        } catch (Exception e) {
            // no icon found, whatever
        }
    }
    
    /**
     * sets up all the GUI stuff
     */
    private void initializeComponents() {
        inputPanel = new CFGInputPanel();
        outputPanel = new PDAOutputPanel();
        controlPanel = new ControlPanel();
    }
    
    /**
     * arranges all the panels in the main window
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // make the main split pane
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setLeftComponent(inputPanel);
        mainSplitPane.setRightComponent(outputPanel);
        mainSplitPane.setDividerLocation(400);
        mainSplitPane.setResizeWeight(0.4);
        
        // add everything to the main window
        add(mainSplitPane, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
        
        // add a status bar at the bottom
        JLabel statusBar = new JLabel("Ready");
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
        add(statusBar, BorderLayout.SOUTH);
    }
    
    /**
     * makes all the buttons do stuff when you click them
     */
    private void setupEventHandlers() {
        // Convert button action
        controlPanel.getConvertButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performConversion();
            }
        });
        
        // Clear button action
        controlPanel.getClearButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearAll();
            }
        });
        
        // Validate button action
        controlPanel.getValidateButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                validateGrammar();
            }
        });
        
        // Example button action
        controlPanel.getExampleButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadExample();
            }
        });
    }
    
    /**
     * Sets up the menu bar.
     */
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        
        JMenuItem newItem = new JMenuItem("New", KeyEvent.VK_N);
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        newItem.addActionListener(e -> clearAll());
        
        JMenuItem exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
        exitItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(newItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // Edit menu
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
        
        JMenuItem validateItem = new JMenuItem("Validate Grammar", KeyEvent.VK_V);
        validateItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        validateItem.addActionListener(e -> validateGrammar());
        
        JMenuItem convertItem = new JMenuItem("Convert to PDA", KeyEvent.VK_C);
        convertItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
        convertItem.addActionListener(e -> performConversion());
        
        editMenu.add(validateItem);
        editMenu.add(convertItem);
        
        // Examples menu
        JMenu examplesMenu = new JMenu("Examples");
        examplesMenu.setMnemonic(KeyEvent.VK_X);
        
        JMenuItem simpleExample = new JMenuItem("Simple Grammar (S -> aSb | ε)");
        simpleExample.addActionListener(e -> loadSimpleExample());
        
        JMenuItem arithmeticExample = new JMenuItem("Arithmetic Grammar");
        arithmeticExample.addActionListener(e -> loadArithmeticExample());
        
        JMenuItem balancedExample = new JMenuItem("Balanced Parentheses");
        balancedExample.addActionListener(e -> loadBalancedExample());
        
        examplesMenu.add(simpleExample);
        examplesMenu.add(arithmeticExample);
        examplesMenu.add(balancedExample);
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        
        JMenuItem aboutItem = new JMenuItem("About", KeyEvent.VK_A);
        aboutItem.addActionListener(e -> showAboutDialog());
        
        JMenuItem helpItem = new JMenuItem("Help", KeyEvent.VK_H);
        helpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        helpItem.addActionListener(e -> showHelpDialog());
        
        helpMenu.add(helpItem);
        helpMenu.addSeparator();
        helpMenu.add(aboutItem);
        
        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(examplesMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    /**
     * Performs the CFG to PDA conversion.
     */
    private void performConversion() {
        String grammarText = inputPanel.getGrammarText();
        
        if (grammarText.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a Context-Free Grammar first.", 
                "No Input", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            // Parse the grammar
            CFG cfg = parser.parseFromString(grammarText);
            
            if (cfg == null) {
                inputPanel.displayValidationResults(parser.getValidationErrors());
                outputPanel.clearOutput();
                return;
            }
            
            // Convert to PDA
            PDAGenerator.ConversionResult result = generator.convertCFGToPDAWithValidation(cfg);
            
            if (result.isSuccessful()) {
                PDA pda = result.getPDA();
                outputPanel.displayPDA(pda);
                
                // Show conversion description
                String description = generator.getConversionDescription(cfg);
                outputPanel.appendText("\n" + description);
                
                // Show complexity analysis
                String analysis = generator.analyzeGrammarComplexity(cfg);
                outputPanel.appendText("\n" + analysis);
                
                inputPanel.displayValidationResults(result.getWarnings());
                
                JOptionPane.showMessageDialog(this, 
                    "Conversion completed successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                inputPanel.displayValidationResults(result.getErrors());
                outputPanel.clearOutput();
                
                JOptionPane.showMessageDialog(this, 
                    "Conversion failed. Please check the error messages.", 
                    "Conversion Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "An unexpected error occurred: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Validates the input grammar without conversion.
     */
    private void validateGrammar() {
        String grammarText = inputPanel.getGrammarText();
        
        if (grammarText.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a Context-Free Grammar first.", 
                "No Input", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        CFG cfg = parser.parseFromString(grammarText);
        
        if (cfg != null) {
            inputPanel.displayValidationResults(java.util.Arrays.asList("Grammar is valid!"));
            JOptionPane.showMessageDialog(this, 
                "Grammar validation successful!", 
                "Valid Grammar", 
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            inputPanel.displayValidationResults(parser.getValidationErrors());
        }
    }
    
    /**
     * Clears all input and output.
     */
    private void clearAll() {
        inputPanel.clearInput();
        outputPanel.clearOutput();
        inputPanel.clearValidationResults();
    }
    
    /**
     * Loads a sample grammar.
     */
    private void loadExample() {
        loadSimpleExample();
    }
    
    /**
     * Loads a simple grammar example.
     */
    private void loadSimpleExample() {
        String example = "# Simple grammar for balanced strings\n" +
                        "# Language: {a^n b^n | n >= 0}\n" +
                        "S -> a S b\n" +
                        "S -> epsilon";
        inputPanel.setGrammarText(example);
    }
    
    /**
     * Loads an arithmetic grammar example.
     */
    private void loadArithmeticExample() {
        String example = "# Arithmetic expression grammar\n" +
                        "# Supports +, *, parentheses, and identifiers\n" +
                        "E -> E + T\n" +
                        "E -> T\n" +
                        "T -> T * F\n" +
                        "T -> F\n" +
                        "F -> ( E )\n" +
                        "F -> id";
        inputPanel.setGrammarText(example);
    }
    
    /**
     * Loads a balanced parentheses grammar example.
     */
    private void loadBalancedExample() {
        String example = "# Balanced parentheses grammar\n" +
                        "# Language: balanced parentheses strings\n" +
                        "S -> ( S )\n" +
                        "S -> S S\n" +
                        "S -> epsilon";
        inputPanel.setGrammarText(example);
    }
    
    /**
     * Shows the about dialog.
     */
    private void showAboutDialog() {
        String message = "CFG to PDA Converter\n" +
                        "Version 1.0\n\n" +
                        "A tool for converting Context-Free Grammars\n" +
                        "to equivalent Pushdown Automata.\n\n" +
                        "Developed for CSE432: Automata and Computability\n" +
                        "Ain Shams University";
        
        JOptionPane.showMessageDialog(this, message, "About", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Shows the help dialog.
     */
    private void showHelpDialog() {
        String helpText = "CFG to PDA Converter - Help\n" +
                         "===========================\n\n" +
                         "How to use:\n" +
                         "1. Enter your Context-Free Grammar in the left panel\n" +
                         "2. Use the format: A -> alpha (one rule per line)\n" +
                         "3. Use 'epsilon' for epsilon productions\n" +
                         "4. Click 'Validate' to check grammar syntax\n" +
                         "5. Click 'Convert' to generate the equivalent PDA\n\n" +
                         "Grammar Format:\n" +
                         "- Non-terminals: uppercase letters (A, B, S, etc.)\n" +
                         "- Terminals: lowercase letters, digits, symbols\n" +
                         "- Comments: lines starting with #\n" +
                         "- Epsilon: use 'epsilon' keyword\n\n" +
                         "Keyboard Shortcuts:\n" +
                         "- F5: Validate Grammar\n" +
                         "- F6: Convert to PDA\n" +
                         "- Ctrl+N: New (Clear All)\n" +
                         "- F1: Show this help";
        
        JTextArea textArea = new JTextArea(helpText);
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Help", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Main method to run the application.
     */
    public static void main(String[] args) {
        // Use default look and feel
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainWindow().setVisible(true);
            }
        });
    }
}