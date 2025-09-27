package com.automata.model;

import java.util.*;

/**
 * this represents a context-free grammar
 * it's got non-terminals, terminals, a start symbol, and production rules
 */
public class CFG {
    private Set<String> nonTerminals;
    private Set<String> terminals;
    private String startSymbol;
    private List<ProductionRule> rules;
    
    /**
     * makes a new empty grammar
     */
    public CFG() {
        this.nonTerminals = new HashSet<>();
        this.terminals = new HashSet<>();
        this.rules = new ArrayList<>();
        this.startSymbol = null;
    }
    
    /**
     * makes a CFG with all the parts you give it
     * @param nonTerminals the non-terminal symbols
     * @param terminals the terminal symbols  
     * @param startSymbol where to start
     * @param rules all the production rules
     */
    public CFG(Set<String> nonTerminals, Set<String> terminals, 
               String startSymbol, List<ProductionRule> rules) {
        this.nonTerminals = new HashSet<>(nonTerminals);
        this.terminals = new HashSet<>(terminals);
        this.startSymbol = startSymbol;
        this.rules = new ArrayList<>(rules);
    }
    
    /**
     * adds a production rule to the grammar
     * @param rule the rule you want to add
     */
    public void addRule(ProductionRule rule) {
        if (rule != null && rule.isValid()) {
            rules.add(rule);
            nonTerminals.add(rule.getLeftSide());
            
            // put symbols in the right sets
            for (String symbol : rule.getRightSide()) {
                if (!symbol.equals("epsilon")) {
                    if (symbol.matches("[A-Z]")) {
                        nonTerminals.add(symbol);
                    } else {
                        terminals.add(symbol);
                    }
                }
            }
        }
    }
    
    /**
     * Adds a production rule from string format.
     * @param left Left-hand side non-terminal
     * @param right Right-hand side symbols
     */
    public void addRule(String left, String right) {
        ProductionRule rule = new ProductionRule(left, right);
        addRule(rule);
    }
    
    /**
     * sets which symbol is the start symbol
     * @param startSymbol the symbol to start with
     */
    public void setStartSymbol(String startSymbol) {
        this.startSymbol = startSymbol;
        // don't add it to nonTerminals automatically - rules should do that
    }
    
    /**
     * Gets all production rules for a specific non-terminal.
     * @param nonTerminal The non-terminal to find rules for
     * @return List of production rules with the given left-hand side
     */
    public List<ProductionRule> getRulesFor(String nonTerminal) {
        List<ProductionRule> result = new ArrayList<>();
        for (ProductionRule rule : rules) {
            if (rule.getLeftSide().equals(nonTerminal)) {
                result.add(rule);
            }
        }
        return result;
    }
    
    /**
     * checks if the grammar makes sense
     * @return true if it's good, false if something's wrong
     */
    public boolean isValid() {
        // gotta have at least one rule
        if (rules.isEmpty()) {
            return false;
        }
        
        // need a start symbol
        if (startSymbol == null || startSymbol.trim().isEmpty()) {
            return false;
        }
        
        // All rules must be valid
        for (ProductionRule rule : rules) {
            if (!rule.isValid()) {
                return false;
            }
        }
        
        // All non-terminals in right-hand sides must be defined as left-hand sides
        Set<String> definedNonTerminals = new HashSet<>();
        for (ProductionRule rule : rules) {
            definedNonTerminals.add(rule.getLeftSide());
        }
        
        // Start symbol must be a defined non-terminal (has at least one rule)
        if (!definedNonTerminals.contains(startSymbol)) {
            return false;
        }
        
        for (ProductionRule rule : rules) {
            for (String symbol : rule.getRightSide()) {
                if (symbol.matches("[A-Z]") && !definedNonTerminals.contains(symbol)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Gets validation errors for the grammar.
     * @return List of validation error messages
     */
    public List<String> getValidationErrors() {
        List<String> errors = new ArrayList<>();
        
        if (rules.isEmpty()) {
            errors.add("Grammar must contain at least one production rule");
        }
        
        if (startSymbol == null || startSymbol.trim().isEmpty()) {
            errors.add("Grammar must have a start symbol");
        } else if (!nonTerminals.contains(startSymbol)) {
            errors.add("Start symbol '" + startSymbol + "' must be a defined non-terminal");
        }
        
        for (int i = 0; i < rules.size(); i++) {
            ProductionRule rule = rules.get(i);
            if (!rule.isValid()) {
                errors.add("Rule " + (i + 1) + " is invalid: " + rule.toString());
            }
        }
        
        // Check for undefined non-terminals (symbols used in RHS but not defined as LHS)
        Set<String> definedNonTerminals = new HashSet<>();
        for (ProductionRule rule : rules) {
            definedNonTerminals.add(rule.getLeftSide());
        }
        
        Set<String> undefinedNonTerminals = new HashSet<>();
        for (ProductionRule rule : rules) {
            for (String symbol : rule.getRightSide()) {
                if (symbol.matches("[A-Z]") && !definedNonTerminals.contains(symbol)) {
                    undefinedNonTerminals.add(symbol);
                }
            }
        }
        
        for (String undefined : undefinedNonTerminals) {
            errors.add("Non-terminal '" + undefined + "' is used but not defined");
        }
        
        return errors;
    }
    
    /**
     * Finds unreachable symbols in the grammar.
     * @return Set of unreachable non-terminals
     */
    public Set<String> getUnreachableSymbols() {
        Set<String> reachable = new HashSet<>();
        Set<String> toProcess = new HashSet<>();
        
        if (startSymbol != null) {
            toProcess.add(startSymbol);
        }
        
        while (!toProcess.isEmpty()) {
            String current = toProcess.iterator().next();
            toProcess.remove(current);
            reachable.add(current);
            
            for (ProductionRule rule : getRulesFor(current)) {
                for (String symbol : rule.getRightSide()) {
                    if (symbol.matches("[A-Z]") && !reachable.contains(symbol)) {
                        toProcess.add(symbol);
                    }
                }
            }
        }
        
        Set<String> unreachable = new HashSet<>(nonTerminals);
        unreachable.removeAll(reachable);
        return unreachable;
    }
    
    // Getters
    public Set<String> getNonTerminals() {
        return new HashSet<>(nonTerminals);
    }
    
    public Set<String> getTerminals() {
        return new HashSet<>(terminals);
    }
    
    public String getStartSymbol() {
        return startSymbol;
    }
    
    public List<ProductionRule> getRules() {
        return new ArrayList<>(rules);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CFG:\n");
        sb.append("Non-terminals: ").append(nonTerminals).append("\n");
        sb.append("Terminals: ").append(terminals).append("\n");
        sb.append("Start symbol: ").append(startSymbol).append("\n");
        sb.append("Production rules:\n");
        
        for (ProductionRule rule : rules) {
            sb.append("  ").append(rule.toString()).append("\n");
        }
        
        return sb.toString();
    }
}