package com.automata.parser;

import com.automata.model.CFG;
import com.automata.model.ProductionRule;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * parser for context-free grammar input text
 * handles different input formats and provides comprehensive validation
 */
public class CFGParser {
    
    private List<String> validationErrors;
    private List<String> validationWarnings;
    private static final Pattern PRODUCTION_PATTERN = Pattern.compile("^\\s*([A-Z])\\s*->\\s*(.+)\\s*$");
    private static final Pattern SYMBOL_PATTERN = Pattern.compile("[a-zA-Z0-9+*()epsilon]|epsilon|id");
    
    /**
     * makes a new CFG parser
     */
    public CFGParser() {
        this.validationErrors = new ArrayList<>();
        this.validationWarnings = new ArrayList<>();
    }
    
    /**
     * Parses a CFG from string input.
     * Expected format:
     * - Each line contains one production rule
     * - Format: "A -> alpha" where A is non-terminal, alpha is sequence of symbols
     * - Multiple rules for same non-terminal can be on separate lines
     * - Epsilon can be represented as "epsilon"
     * - Comments start with # and are ignored
     * - Empty lines are ignored
     * 
     * @param input The input string containing grammar rules
     * @return Parsed CFG object, or null if parsing fails
     */
    public CFG parseFromString(String input) {
        validationErrors.clear();
        validationWarnings.clear();
        
        if (input == null || input.trim().isEmpty()) {
            validationErrors.add("Input cannot be empty");
            return null;
        }
        
        CFG cfg = new CFG();
        String[] lines = input.split("\\r?\\n");
        String startSymbol = null;
        
        for (int lineNum = 0; lineNum < lines.length; lineNum++) {
            String line = lines[lineNum].trim();
            
            // skip empty lines and comments
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            
            // parse the production rule
            ProductionRule rule = parseProductionRule(line, lineNum + 1);
            if (rule != null) {
                cfg.addRule(rule);
                
                // first non-terminal becomes start symbol if we don't have one yet
                if (startSymbol == null) {
                    startSymbol = rule.getLeftSide();
                }
            }
        }
        
        // set the start symbol
        if (startSymbol != null) {
            cfg.setStartSymbol(startSymbol);
        }
        
        // check if the whole grammar is valid
        if (!cfg.isValid()) {
            validationErrors.addAll(cfg.getValidationErrors());
            return null; // only return null for real errors, not warnings
        }
        
        // do extra validation (just warnings)
        validateGrammarStructure(cfg);
        
        return cfg;
    }
    
    /**
     * Parses a CFG from alternative format with explicit start symbol.
     * Expected format:
     * - First line: "START: S" (where S is the start symbol)
     * - Remaining lines: production rules as in parseFromString
     * 
     * @param input The input string with explicit start symbol
     * @return Parsed CFG object, or null if parsing fails
     */
    public CFG parseWithExplicitStart(String input) {
        validationErrors.clear();
        
        if (input == null || input.trim().isEmpty()) {
            validationErrors.add("Input cannot be empty");
            return null;
        }
        
        String[] lines = input.split("\\r?\\n");
        String explicitStart = null;
        List<String> ruleLines = new ArrayList<>();
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            
            if (line.toUpperCase().startsWith("START:")) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    explicitStart = parts[1].trim();
                    if (!explicitStart.matches("[A-Z]")) {
                        validationErrors.add("Start symbol must be a single uppercase letter: " + explicitStart);
                        return null;
                    }
                }
            } else {
                ruleLines.add(line);
            }
        }
        
        // Parse rules
        CFG cfg = parseFromString(String.join("\n", ruleLines));
        if (cfg != null && explicitStart != null) {
            cfg.setStartSymbol(explicitStart);
            
            // Re-validate with explicit start symbol
            if (!cfg.isValid()) {
                validationErrors.clear();
                validationErrors.addAll(cfg.getValidationErrors());
                return null;
            }
        }
        
        return cfg;
    }
    
    /**
     * Parses a single production rule from a line.
     * @param line The line containing the production rule
     * @param lineNumber The line number for error reporting
     * @return Parsed ProductionRule, or null if parsing fails
     */
    private ProductionRule parseProductionRule(String line, int lineNumber) {
        Matcher matcher = PRODUCTION_PATTERN.matcher(line);
        
        if (!matcher.matches()) {
            validationErrors.add("Line " + lineNumber + ": Invalid production rule format. Expected 'A -> alpha': " + line);
            return null;
        }
        
        String leftSide = matcher.group(1);
        String rightSide = matcher.group(2).trim();
        
        // check the left side
        if (!leftSide.matches("[A-Z]")) {
            validationErrors.add("Line " + lineNumber + ": Left side must be a single uppercase letter: " + leftSide);
            return null;
        }
        
        // check the right side symbols
        if (!validateRightSide(rightSide, lineNumber)) {
            return null;
        }
        
        return new ProductionRule(leftSide, rightSide);
    }
    
    /**
     * Validates the right-hand side of a production rule.
     * @param rightSide The right-hand side string
     * @param lineNumber The line number for error reporting
     * @return true if valid, false otherwise
     */
    private boolean validateRightSide(String rightSide, int lineNumber) {
        if (rightSide.isEmpty()) {
            validationErrors.add("Line " + lineNumber + ": Right side cannot be empty. Use 'epsilon' for epsilon production");
            return false;
        }
        
        // handle epsilon productions
        if (rightSide.equals("epsilon") || rightSide.equals("ε")) {
            return true;
        }
        
        // split into symbols and check each one
        String[] symbols = rightSide.split("\\s+");
        for (String symbol : symbols) {
            if (!validateSymbol(symbol)) {
                validationErrors.add("Line " + lineNumber + ": Invalid symbol '" + symbol + "'. " +
                    "Symbols must be uppercase letters (non-terminals), lowercase letters/digits (terminals), " +
                    "or special symbols (+, *, (, ), id, epsilon)");
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Validates a single symbol.
     * @param symbol The symbol to validate
     * @return true if valid, false otherwise
     */
    private boolean validateSymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return false;
        }
        
        // allow non-terminals (uppercase letters)
        if (symbol.matches("[A-Z]")) {
            return true;
        }
        
        // allow terminals (lowercase letters, digits)
        if (symbol.matches("[a-z0-9]")) {
            return true;
        }
        
        // Allow special symbols
        if (symbol.equals("epsilon") || symbol.equals("ε") ||
            symbol.equals("+") || symbol.equals("*") || 
            symbol.equals("(") || symbol.equals(")") || 
            symbol.equals("id") || symbol.equals("num") ||
            symbol.equals("if") || symbol.equals("then") || symbol.equals("else") ||
            symbol.equals("-") || symbol.equals("/")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Validates a complete CFG object.
     * @param grammar The CFG to validate
     * @return true if valid, false otherwise
     */
    public boolean validateCFG(CFG grammar) {
        validationErrors.clear();
        
        if (grammar == null) {
            validationErrors.add("Grammar cannot be null");
            return false;
        }
        
        if (!grammar.isValid()) {
            validationErrors.addAll(grammar.getValidationErrors());
            return false;
        }
        
        // Additional validation checks
        validateGrammarStructure(grammar);
        
        return validationErrors.isEmpty();
    }
    
    /**
     * Performs additional structural validation on the grammar.
     * @param grammar The CFG to validate
     */
    private void validateGrammarStructure(CFG grammar) {
        // Check for unreachable symbols
        Set<String> unreachable = grammar.getUnreachableSymbols();
        if (!unreachable.isEmpty()) {
            validationWarnings.add("Warning: Unreachable non-terminals found: " + unreachable);
        }
        
        // Check for left recursion
        checkLeftRecursion(grammar);
        
        // Check for useless productions
        checkUselessProductions(grammar);
        
        // Check for ambiguous grammar patterns
        checkAmbiguityPatterns(grammar);
        
        // Check for proper start symbol definition
        validateStartSymbol(grammar);
        
        // Check for empty language
        checkEmptyLanguage(grammar);
    }
    
    /**
     * Checks for immediate left recursion in the grammar.
     * @param grammar The CFG to check
     */
    private void checkLeftRecursion(CFG grammar) {
        for (String nonTerminal : grammar.getNonTerminals()) {
            List<ProductionRule> rules = grammar.getRulesFor(nonTerminal);
            for (ProductionRule rule : rules) {
                if (!rule.getRightSide().isEmpty() && 
                    rule.getRightSide().get(0).equals(nonTerminal)) {
                    validationWarnings.add("Warning: Left recursion detected in rule: " + rule.toString());
                }
            }
        }
    }
    
    /**
     * Checks for productions that don't contribute to the language.
     * @param grammar The CFG to check
     */
    private void checkUselessProductions(CFG grammar) {
        // Find non-terminals that can derive terminal strings
        Set<String> productive = findProductiveNonTerminals(grammar);
        
        for (String nonTerminal : grammar.getNonTerminals()) {
            if (!productive.contains(nonTerminal)) {
                validationWarnings.add("Warning: Non-terminal '" + nonTerminal + "' cannot derive any terminal string");
            }
        }
    }
    
    /**
     * Finds non-terminals that can derive terminal strings.
     * @param grammar The CFG to analyze
     * @return Set of productive non-terminals
     */
    private Set<String> findProductiveNonTerminals(CFG grammar) {
        Set<String> productive = new HashSet<>();
        boolean changed = true;
        
        while (changed) {
            changed = false;
            for (ProductionRule rule : grammar.getRules()) {
                if (!productive.contains(rule.getLeftSide())) {
                    boolean canDerive = true;
                    for (String symbol : rule.getRightSide()) {
                        if (symbol.matches("[A-Z]") && !productive.contains(symbol)) {
                            canDerive = false;
                            break;
                        }
                    }
                    if (canDerive) {
                        productive.add(rule.getLeftSide());
                        changed = true;
                    }
                }
            }
        }
        
        return productive;
    }
    
    /**
     * Gets the list of validation errors from the last parsing attempt.
     * @return List of validation error messages
     */
    public List<String> getValidationErrors() {
        return new ArrayList<>(validationErrors);
    }
    
    /**
     * Gets the list of validation warnings from the last parsing attempt.
     * @return List of validation warning messages
     */
    public List<String> getValidationWarnings() {
        return new ArrayList<>(validationWarnings);
    }
    
    /**
     * Checks if the last parsing attempt had any errors.
     * @return true if there were validation errors, false otherwise
     */
    public boolean hasErrors() {
        return !validationErrors.isEmpty();
    }
    
    /**
     * Checks if the last parsing attempt had any warnings.
     * @return true if there were validation warnings, false otherwise
     */
    public boolean hasWarnings() {
        return !validationWarnings.isEmpty();
    }
    
    /**
     * Validates the start symbol definition.
     * @param grammar The CFG to validate
     */
    private void validateStartSymbol(CFG grammar) {
        String startSymbol = grammar.getStartSymbol();
        
        if (startSymbol == null) {
            validationErrors.add("Error: Start symbol is not defined");
            return;
        }
        
        // Check if start symbol has at least one production rule
        List<ProductionRule> startRules = grammar.getRulesFor(startSymbol);
        if (startRules.isEmpty()) {
            validationErrors.add("Error: Start symbol '" + startSymbol + "' has no production rules");
        }
        
        // Check if start symbol appears on right-hand side (not necessarily an error, but worth noting)
        boolean appearsOnRHS = false;
        for (ProductionRule rule : grammar.getRules()) {
            if (rule.getRightSide().contains(startSymbol)) {
                appearsOnRHS = true;
                break;
            }
        }
        
        if (appearsOnRHS) {
            validationWarnings.add("Info: Start symbol '" + startSymbol + "' appears in right-hand side of productions");
        }
    }
    
    /**
     * Checks for common ambiguity patterns in the grammar.
     * @param grammar The CFG to check
     */
    private void checkAmbiguityPatterns(CFG grammar) {
        for (String nonTerminal : grammar.getNonTerminals()) {
            List<ProductionRule> rules = grammar.getRulesFor(nonTerminal);
            
            // Check for common prefix (potential LL(1) conflict)
            checkCommonPrefix(nonTerminal, rules);
            
            // Check for left factoring issues
            checkLeftFactoring(nonTerminal, rules);
        }
    }
    
    /**
     * Checks for common prefixes in production rules.
     * @param nonTerminal The non-terminal being checked
     * @param rules The production rules for this non-terminal
     */
    private void checkCommonPrefix(String nonTerminal, List<ProductionRule> rules) {
        for (int i = 0; i < rules.size(); i++) {
            for (int j = i + 1; j < rules.size(); j++) {
                ProductionRule rule1 = rules.get(i);
                ProductionRule rule2 = rules.get(j);
                
                if (!rule1.getRightSide().isEmpty() && !rule2.getRightSide().isEmpty()) {
                    String first1 = rule1.getRightSide().get(0);
                    String first2 = rule2.getRightSide().get(0);
                    
                    if (first1.equals(first2)) {
                        validationWarnings.add("Warning: Common prefix detected for non-terminal '" + 
                            nonTerminal + "': rules '" + rule1.toString() + "' and '" + 
                            rule2.toString() + "' both start with '" + first1 + "'");
                    }
                }
            }
        }
    }
    
    /**
     * Checks for left factoring issues.
     * @param nonTerminal The non-terminal being checked
     * @param rules The production rules for this non-terminal
     */
    private void checkLeftFactoring(String nonTerminal, List<ProductionRule> rules) {
        // Look for rules that could benefit from left factoring
        Map<String, List<ProductionRule>> prefixGroups = new HashMap<>();
        
        for (ProductionRule rule : rules) {
            if (!rule.getRightSide().isEmpty()) {
                String firstSymbol = rule.getRightSide().get(0);
                prefixGroups.computeIfAbsent(firstSymbol, k -> new ArrayList<>()).add(rule);
            }
        }
        
        for (Map.Entry<String, List<ProductionRule>> entry : prefixGroups.entrySet()) {
            if (entry.getValue().size() > 1) {
                validationWarnings.add("Info: Non-terminal '" + nonTerminal + 
                    "' has multiple rules starting with '" + entry.getKey() + 
                    "' - consider left factoring");
            }
        }
    }
    
    /**
     * Checks if the grammar generates an empty language.
     * @param grammar The CFG to check
     */
    private void checkEmptyLanguage(CFG grammar) {
        Set<String> productive = findProductiveNonTerminals(grammar);
        
        if (!productive.contains(grammar.getStartSymbol())) {
            validationErrors.add("Error: Grammar generates empty language - start symbol cannot derive any terminal string");
        }
    }
    
    /**
     * Performs comprehensive validation with detailed error categorization.
     * @param grammar The CFG to validate
     * @return ValidationResult containing categorized errors and warnings
     */
    public ValidationResult performDetailedValidation(CFG grammar) {
        validationErrors.clear();
        
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> info = new ArrayList<>();
        
        if (grammar == null) {
            errors.add("Grammar cannot be null");
            return new ValidationResult(false, errors, warnings, info);
        }
        
        // Basic validation
        if (!grammar.isValid()) {
            errors.addAll(grammar.getValidationErrors());
        }
        
        // Advanced validation
        validateGrammarStructure(grammar);
        
        // Categorize validation messages
        for (String message : validationErrors) {
            if (message.startsWith("Error:")) {
                errors.add(message);
            } else if (message.startsWith("Warning:")) {
                warnings.add(message);
            } else if (message.startsWith("Info:")) {
                info.add(message);
            } else {
                // Default to warning for uncategorized messages
                warnings.add(message);
            }
        }
        
        boolean isValid = errors.isEmpty();
        return new ValidationResult(isValid, errors, warnings, info);
    }
    
    /**
     * Result class for detailed validation.
     */
    public static class ValidationResult {
        private final boolean isValid;
        private final List<String> errors;
        private final List<String> warnings;
        private final List<String> info;
        
        public ValidationResult(boolean isValid, List<String> errors, List<String> warnings, List<String> info) {
            this.isValid = isValid;
            this.errors = new ArrayList<>(errors);
            this.warnings = new ArrayList<>(warnings);
            this.info = new ArrayList<>(info);
        }
        
        public boolean isValid() { return isValid; }
        public List<String> getErrors() { return new ArrayList<>(errors); }
        public List<String> getWarnings() { return new ArrayList<>(warnings); }
        public List<String> getInfo() { return new ArrayList<>(info); }
        
        public boolean hasErrors() { return !errors.isEmpty(); }
        public boolean hasWarnings() { return !warnings.isEmpty(); }
        public boolean hasInfo() { return !info.isEmpty(); }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Validation Result: ").append(isValid ? "VALID" : "INVALID").append("\n");
            
            if (!errors.isEmpty()) {
                sb.append("Errors:\n");
                for (String error : errors) {
                    sb.append("  - ").append(error).append("\n");
                }
            }
            
            if (!warnings.isEmpty()) {
                sb.append("Warnings:\n");
                for (String warning : warnings) {
                    sb.append("  - ").append(warning).append("\n");
                }
            }
            
            if (!info.isEmpty()) {
                sb.append("Info:\n");
                for (String infoMsg : info) {
                    sb.append("  - ").append(infoMsg).append("\n");
                }
            }
            
            return sb.toString();
        }
    }
    
    /**
     * Clears all validation errors.
     */
    public void clearErrors() {
        validationErrors.clear();
    }
}