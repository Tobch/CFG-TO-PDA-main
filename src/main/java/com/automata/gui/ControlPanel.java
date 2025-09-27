package com.automata.gui;

import javax.swing.*;
import java.awt.*;

/**
 * panel with all the main buttons you need
 */
public class ControlPanel extends JPanel {
    
    private JButton convertButton;
    private JButton validateButton;
    private JButton clearButton;
    private JButton exampleButton;
    private JProgressBar progressBar;
    
    /**
     * makes the control panel with all the buttons
     */
    public ControlPanel() {
        initializeComponents();
        setupLayout();
    }
    
    /**
     * sets up all the buttons and progress bar
     */
    private void initializeComponents() {
        // the important buttons
        convertButton = new JButton("Convert to PDA");
        convertButton.setToolTipText("Convert the CFG to an equivalent PDA (F6)");
        convertButton.setFont(convertButton.getFont().deriveFont(Font.BOLD));
        convertButton.setPreferredSize(new Dimension(140, 30));
        
        validateButton = new JButton("Validate Grammar");
        validateButton.setToolTipText("Validate the CFG syntax without conversion (F5)");
        validateButton.setPreferredSize(new Dimension(140, 30));
        
        clearButton = new JButton("Clear All");
        clearButton.setToolTipText("Clear all input and output (Ctrl+N)");
        clearButton.setPreferredSize(new Dimension(100, 30));
        
        exampleButton = new JButton("Load Example");
        exampleButton.setToolTipText("Load a sample grammar");
        exampleButton.setPreferredSize(new Dimension(120, 30));
        
        // progress bar that's hidden at first
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");
        progressBar.setPreferredSize(new Dimension(200, 20));
        progressBar.setVisible(false);
    }
    
    /**
     * arranges everything on the panel
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // left side with the main buttons
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.add(convertButton);
        leftPanel.add(validateButton);
        leftPanel.add(new JSeparator(SwingConstants.VERTICAL));
        leftPanel.add(clearButton);
        leftPanel.add(exampleButton);
        
        // right side with progress bar
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.add(progressBar);
        
        // put the panels in the main layout
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
        
        // add a nice border
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }
    
    /**
     * Gets the convert button.
     * @return The convert button
     */
    public JButton getConvertButton() {
        return convertButton;
    }
    
    /**
     * Gets the validate button.
     * @return The validate button
     */
    public JButton getValidateButton() {
        return validateButton;
    }
    
    /**
     * Gets the clear button.
     * @return The clear button
     */
    public JButton getClearButton() {
        return clearButton;
    }
    
    /**
     * Gets the example button.
     * @return The example button
     */
    public JButton getExampleButton() {
        return exampleButton;
    }
    
    /**
     * Shows the progress bar with a message.
     * @param message The progress message
     */
    public void showProgress(String message) {
        progressBar.setString(message);
        progressBar.setIndeterminate(true);
        progressBar.setVisible(true);
        
        // Disable buttons during processing
        setButtonsEnabled(false);
        
        revalidate();
        repaint();
    }
    
    /**
     * Hides the progress bar and re-enables buttons.
     */
    public void hideProgress() {
        progressBar.setVisible(false);
        progressBar.setIndeterminate(false);
        progressBar.setString("Ready");
        
        // Re-enable buttons
        setButtonsEnabled(true);
        
        revalidate();
        repaint();
    }
    
    /**
     * Sets the enabled state of all buttons.
     * @param enabled Whether buttons should be enabled
     */
    private void setButtonsEnabled(boolean enabled) {
        convertButton.setEnabled(enabled);
        validateButton.setEnabled(enabled);
        clearButton.setEnabled(enabled);
        exampleButton.setEnabled(enabled);
    }
    
    /**
     * Updates the progress bar with a specific value.
     * @param value The progress value (0-100)
     * @param message The progress message
     */
    public void updateProgress(int value, String message) {
        progressBar.setIndeterminate(false);
        progressBar.setValue(value);
        progressBar.setString(message);
        
        if (!progressBar.isVisible()) {
            progressBar.setVisible(true);
            revalidate();
            repaint();
        }
    }
    
    /**
     * Sets the status message without showing progress.
     * @param message The status message
     */
    public void setStatus(String message) {
        if (!progressBar.isVisible()) {
            progressBar.setString(message);
        }
    }
    
    /**
     * Adds a separator to the button layout.
     * @return A vertical separator component
     */
    private Component createSeparator() {
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setPreferredSize(new Dimension(2, 25));
        return separator;
    }
}