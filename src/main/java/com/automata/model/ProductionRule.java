package com.automata.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * represents a production rule in a context-free grammar
 * has the form A -> alpha where A is a non-terminal
 * and alpha is a sequence of terminals and non-terminals
 */
public class ProductionRule {
    private String leftSide;
    private List<String> rightSide;

    // Token pattern: identifiers (letters+digits/underscore) OR any single non-whitespace symbol.
    // This ensures "(E)" becomes ["(", "E", ")"] rather than a combined "()" token.
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[A-Za-z][A-Za-z0-9_]*|\\S");
    
    /**
     * makes a new production rule
     * @param left the left side non-terminal
     * @param right the right side symbols
     */
    public ProductionRule(String left, List<String> right) {
        this.leftSide = left;
        this.rightSide = new ArrayList<>(right);
    }
    
    /**
     * makes a production rule from a string
     * @param left the left side non-terminal
     * @param rightStr the right side as a string (symbols separated by spaces OR not)
     */
    public ProductionRule(String left, String rightStr) {
        this.leftSide = left;
        this.rightSide = new ArrayList<>();
        
        if (rightStr == null) {
            return;
        }
        
        String trimmed = rightStr.trim();
        if (trimmed.equals("ε") || trimmed.equalsIgnoreCase("epsilon")) {
            // epsilon production
            this.rightSide.add("epsilon");
            return;
        }
        
        // Use pattern-based tokenization so tokens like "(" and ")" are separate
        Matcher m = TOKEN_PATTERN.matcher(trimmed);
        while (m.find()) {
            String token = m.group();
            if (token != null && !token.isEmpty()) {
                this.rightSide.add(token);
            }
        }
    }
    
    /**
     * checks if this production rule makes sense
     * @return true if it's valid, false if something's wrong
     */
    public boolean isValid() {
        if (leftSide == null || leftSide.trim().isEmpty()) {
            return false;
        }
        
        if (rightSide == null || rightSide.isEmpty()) {
            return false;
        }
        
        // left side should be one non-terminal (uppercase letter)
        if (!leftSide.matches("[A-Z]")) {
            return false;
        }
        
        // right side symbols gotta be valid terminals or non-terminals
        for (String symbol : rightSide) {
            if (symbol == null || symbol.trim().isEmpty()) {
                return false;
            }
            // allow uppercase single-letter non-terminals, multi-char identifiers, epsilon, and special symbols
            if (!symbol.matches("[A-Z]") &&
                !symbol.matches("[a-zA-Z][a-zA-Z0-9_]*") &&
                !symbol.matches("epsilon|\\+|\\*|\\-|\\/|\\(|\\)")) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * checks if this is an epsilon production
     * @return true if the right side is just epsilon
     */
    public boolean isEpsilonProduction() {
        return rightSide.size() == 1 && 
               (rightSide.get(0).equals("ε") || rightSide.get(0).equalsIgnoreCase("epsilon") || rightSide.get(0).equals("epsilon"));
    }
    
    // Getters
    public String getLeftSide() {
        return leftSide;
    }
    
    public List<String> getRightSide() {
        return new ArrayList<>(rightSide);
    }
    
    // Setters
    public void setLeftSide(String leftSide) {
        this.leftSide = leftSide;
    }
    
    public void setRightSide(List<String> rightSide) {
        this.rightSide = new ArrayList<>(rightSide);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(leftSide).append(" -> ");
        
        if (rightSide.isEmpty()) {
            sb.append("epsilon");
        } else {
            for (int i = 0; i < rightSide.size(); i++) {
                if (i > 0) sb.append(" ");
                String symbol = rightSide.get(i);
                if (symbol.equals("ε") || symbol.equalsIgnoreCase("epsilon")) {
                    sb.append("epsilon");
                } else {
                    sb.append(symbol);
                }
            }
        }
        
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ProductionRule that = (ProductionRule) obj;
        return Objects.equals(leftSide, that.leftSide) &&
               Objects.equals(rightSide, that.rightSide);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(leftSide, rightSide);
    }
}
