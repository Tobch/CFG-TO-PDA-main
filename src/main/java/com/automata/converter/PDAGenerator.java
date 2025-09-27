package com.automata.converter;

import com.automata.model.CFG;
import com.automata.model.PDA;
import com.automata.model.PDATransition;
import com.automata.model.ProductionRule;
import java.util.*;

/**
 * this converts your CFG into a PDA
 * uses the standard algorithm for this conversion
 */
public class PDAGenerator {
    
    private static final String INITIAL_STATE = "q0";
    private static final String LOOP_STATE = "q1";
    private static final String FINAL_STATE = "q2";
    private static final String INITIAL_STACK_SYMBOL = "Z0";
    
    /**
     * takes your grammar and makes it into a PDA that does the same thing
     * if something goes wrong, you get null back instead
     * 
     * @param grammar the grammar you wanna convert
     * @return the PDA version, or null if it breaks
     */
    public PDA convertCFGToPDA(CFG grammar) {
        if (grammar == null || !grammar.isValid()) {
            return null;
        }
        
        PDA pda = new PDA();
        
        // make the states
        createStates(pda);
        
        // set up the starting stuff
        pda.setInitialState(INITIAL_STATE);
        pda.setInitialStackSymbol(INITIAL_STACK_SYMBOL);
        pda.addAcceptingState(FINAL_STATE);
        
        // make all the transitions
        createInitialTransition(pda, grammar);
        createGrammarTransitions(pda, grammar);
        createTerminalTransitions(pda, grammar);
        createFinalTransition(pda);
        
        return pda;
    }
    
    /**
     * makes the 3 states we need - start, loop, and end state
     * @param pda the PDA we're building
     */
    private void createStates(PDA pda) {
        pda.addState(INITIAL_STATE);
        pda.addState(LOOP_STATE);
        pda.addState(FINAL_STATE);
    }
    
    /**
     * makes the first transition that puts the start symbol on the stack
     * basically goes from q0 to q1 and pushes S on top of Z0
     * 
     * @param pda the PDA we're working on
     * @param grammar the original grammar
     */
    private void createInitialTransition(PDA pda, CFG grammar) {
        String startSymbol = grammar.getStartSymbol();
        
        // put the start symbol on the stack
        PDATransition initialTransition = new PDATransition(
            INITIAL_STATE,
            "epsilon",
            INITIAL_STACK_SYMBOL,
            LOOP_STATE,
            startSymbol + INITIAL_STACK_SYMBOL
        );
        
        pda.addTransition(initialTransition);
    }
    
    /**
     * makes transitions for every rule in the grammar
     * so if you got A -> alpha, we make (q1, epsilon, A) -> (q1, alpha)
     * 
     * @param pda the PDA we're building
     * @param grammar the grammar with all the rules
     */
    private void createGrammarTransitions(PDA pda, CFG grammar) {
        for (ProductionRule rule : grammar.getRules()) {
            String leftSide = rule.getLeftSide();
            List<String> rightSide = rule.getRightSide();
            
            // figure out what to push on the stack
            StringBuilder stackPush = new StringBuilder();
            
            if (rule.isEpsilonProduction()) {
                // epsilon rules just pop, don't push nothing
                stackPush.append("epsilon");
            } else {
                // gotta push backwards - last symbol goes first
                for (int i = rightSide.size() - 1; i >= 0; i--) {
                    stackPush.append(rightSide.get(i));
                }
            }
            
            PDATransition grammarTransition = new PDATransition(
                LOOP_STATE,
                "epsilon",
                leftSide,
                LOOP_STATE,
                stackPush.toString()
            );
            
            pda.addTransition(grammarTransition);
        }
    }
    
    /**
     * makes transitions for the terminal symbols
     * each terminal 'a' gets a transition (q1, a, a) -> (q1, epsilon)
     * 
     * @param pda the PDA we're making
     * @param grammar where we get the terminals from
     */
    private void createTerminalTransitions(PDA pda, CFG grammar) {
        for (String terminal : grammar.getTerminals()) {
            // skip epsilon cause it ain't a real terminal
            if (terminal.equals("epsilon")) {
                continue;
            }
            
            PDATransition terminalTransition = new PDATransition(
                LOOP_STATE,
                terminal,
                terminal,
                LOOP_STATE,
                "epsilon"
            );
            
            pda.addTransition(terminalTransition);
        }
    }
    
    /**
     * makes the last transition so we can accept strings
     * goes (q1, epsilon, Z0) -> (q2, epsilon)
     * 
     * @param pda the PDA we're finishing up
     */
    private void createFinalTransition(PDA pda) {
        PDATransition finalTransition = new PDATransition(
            LOOP_STATE,
            "epsilon",
            INITIAL_STACK_SYMBOL,
            FINAL_STATE,
            "epsilon"
        );
        
        pda.addTransition(finalTransition);
    }
    
    /**
     * checks if the conversion actually worked out
     * @param pda the PDA we just made
     * @return true if it's good, false if something's wrong
     */
    public boolean validateConversion(PDA pda) {
        if (pda == null) {
            return false;
        }
        
        return pda.isValid();
    }
    
    /**
     * converts CFG to PDA but with better error checking and stuff
     * @param grammar the grammar you want converted
     * @return result object with the PDA plus any warnings or errors
     */
    public ConversionResult convertCFGToPDAWithValidation(CFG grammar) {
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        if (grammar == null) {
            errors.add("Grammar cannot be null");
            return new ConversionResult(null, errors, warnings);
        }
        
        if (!grammar.isValid()) {
            errors.addAll(grammar.getValidationErrors());
            return new ConversionResult(null, errors, warnings);
        }
        
        // look for stuff that might mess up the conversion
        checkForConversionWarnings(grammar, warnings);
        
        // do the actual conversion
        PDA pda = convertCFGToPDA(grammar);
        
        if (pda == null || !pda.isValid()) {
            errors.add("Failed to generate valid PDA from grammar");
            if (pda != null) {
                errors.addAll(pda.getValidationErrors());
            }
        }
        
        return new ConversionResult(pda, errors, warnings);
    }
    
    /**
     * looks for grammar patterns that could cause problems
     * @param grammar the grammar we're checking
     * @param warnings where we put the warning messages
     */
    private void checkForConversionWarnings(CFG grammar, List<String> warnings) {
        // check if we got left recursion problems
        for (String nonTerminal : grammar.getNonTerminals()) {
            List<ProductionRule> rules = grammar.getRulesFor(nonTerminal);
            for (ProductionRule rule : rules) {
                if (!rule.getRightSide().isEmpty() && 
                    rule.getRightSide().get(0).equals(nonTerminal)) {
                    warnings.add("Left recursion detected in rule: " + rule.toString() + 
                               " - this may cause infinite loops in some parsing algorithms");
                }
            }
        }
        
        // see if there's symbols we can't reach
        Set<String> unreachable = grammar.getUnreachableSymbols();
        if (!unreachable.isEmpty()) {
            warnings.add("Unreachable non-terminals found: " + unreachable + 
                        " - these will not affect the PDA but indicate potential grammar issues");
        }
        
        // look for confusing patterns
        checkForAmbiguousPatterns(grammar, warnings);
    }
    
    /**
     * looks for patterns that might mean the grammar is ambiguous
     * @param grammar the grammar we're checking
     * @param warnings where to put the warnings
     */
    private void checkForAmbiguousPatterns(CFG grammar, List<String> warnings) {
        for (String nonTerminal : grammar.getNonTerminals()) {
            List<ProductionRule> rules = grammar.getRulesFor(nonTerminal);
            
            // check if rules start with the same thing
            for (int i = 0; i < rules.size(); i++) {
                for (int j = i + 1; j < rules.size(); j++) {
                    ProductionRule rule1 = rules.get(i);
                    ProductionRule rule2 = rules.get(j);
                    
                    if (!rule1.getRightSide().isEmpty() && !rule2.getRightSide().isEmpty()) {
                        String first1 = rule1.getRightSide().get(0);
                        String first2 = rule2.getRightSide().get(0);
                        
                        if (first1.equals(first2)) {
                            warnings.add("Common prefix detected for non-terminal '" + nonTerminal + 
                                        "': rules '" + rule1.toString() + "' and '" + rule2.toString() + 
                                        "' - this may indicate potential ambiguity");
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Result class for enhanced conversion with validation.
     */
    public static class ConversionResult {
        private final PDA pda;
        private final List<String> errors;
        private final List<String> warnings;
        
        public ConversionResult(PDA pda, List<String> errors, List<String> warnings) {
            this.pda = pda;
            this.errors = new ArrayList<>(errors);
            this.warnings = new ArrayList<>(warnings);
        }
        
        public PDA getPDA() { return pda; }
        public List<String> getErrors() { return new ArrayList<>(errors); }
        public List<String> getWarnings() { return new ArrayList<>(warnings); }
        
        public boolean isSuccessful() { return pda != null && errors.isEmpty(); }
        public boolean hasWarnings() { return !warnings.isEmpty(); }
        public boolean hasErrors() { return !errors.isEmpty(); }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Conversion Result: ").append(isSuccessful() ? "SUCCESS" : "FAILED").append("\n");
            
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
            
            return sb.toString();
        }
    }
    
    /**
     * Gets a detailed description of the conversion process.
     * @param grammar The source CFG
     * @return String describing the conversion steps
     */
    public String getConversionDescription(CFG grammar) {
        if (grammar == null) {
            return "Cannot describe conversion: grammar is null";
        }
        
        StringBuilder description = new StringBuilder();
        description.append("CFG to PDA Conversion Process:\n");
        description.append("============================\n\n");
        
        description.append("1. Create three states: q0 (initial), q1 (loop), q2 (final)\n");
        description.append("2. Set initial stack symbol: Z0\n");
        description.append("3. Create initial transition: (q0, epsilon, Z0) -> (q1, ").append(grammar.getStartSymbol()).append("Z0)\n\n");
        
        description.append("4. For each production rule, create epsilon transition:\n");
        for (ProductionRule rule : grammar.getRules()) {
            description.append("   Rule: ").append(rule.toString()).append("\n");
            description.append("   Transition: (q1, epsilon, ").append(rule.getLeftSide()).append(") -> (q1, ");
            
            if (rule.isEpsilonProduction()) {
                description.append("epsilon");
            } else {
                List<String> rightSide = rule.getRightSide();
                for (int i = rightSide.size() - 1; i >= 0; i--) {
                    description.append(rightSide.get(i));
                }
            }
            description.append(")\n\n");
        }
        
        description.append("5. For each terminal symbol, create matching transition:\n");
        for (String terminal : grammar.getTerminals()) {
            if (!terminal.equals("epsilon")) {
                description.append("   Terminal: ").append(terminal).append("\n");
                description.append("   Transition: (q1, ").append(terminal).append(", ").append(terminal).append(") -> (q1, epsilon)\n");
            }
        }
        
        description.append("\n6. Create final transition: (q1, epsilon, Z0) -> (q2, epsilon)\n");
        
        return description.toString();
    }
    
    /**
     * Analyzes the complexity of the grammar and provides recommendations.
     * @param grammar The CFG to analyze
     * @return Analysis report with complexity metrics and recommendations
     */
    public String analyzeGrammarComplexity(CFG grammar) {
        if (grammar == null) {
            return "Cannot analyze: grammar is null";
        }
        
        StringBuilder analysis = new StringBuilder();
        analysis.append("Grammar Complexity Analysis:\n");
        analysis.append("===========================\n\n");
        
        // Basic metrics
        analysis.append("Basic Metrics:\n");
        analysis.append("- Non-terminals: ").append(grammar.getNonTerminals().size()).append("\n");
        analysis.append("- Terminals: ").append(grammar.getTerminals().size()).append("\n");
        analysis.append("- Production rules: ").append(grammar.getRules().size()).append("\n\n");
        
        // Recursion analysis
        int leftRecursiveRules = 0;
        int rightRecursiveRules = 0;
        int selfRecursiveNonTerminals = 0;
        
        for (String nonTerminal : grammar.getNonTerminals()) {
            List<ProductionRule> rules = grammar.getRulesFor(nonTerminal);
            boolean hasSelfRecursion = false;
            
            for (ProductionRule rule : rules) {
                List<String> rightSide = rule.getRightSide();
                if (!rightSide.isEmpty()) {
                    // Check left recursion
                    if (rightSide.get(0).equals(nonTerminal)) {
                        leftRecursiveRules++;
                    }
                    // Check right recursion
                    if (rightSide.get(rightSide.size() - 1).equals(nonTerminal)) {
                        rightRecursiveRules++;
                    }
                    // Check any self-recursion
                    if (rightSide.contains(nonTerminal)) {
                        hasSelfRecursion = true;
                    }
                }
            }
            
            if (hasSelfRecursion) {
                selfRecursiveNonTerminals++;
            }
        }
        
        analysis.append("Recursion Analysis:\n");
        analysis.append("- Left recursive rules: ").append(leftRecursiveRules).append("\n");
        analysis.append("- Right recursive rules: ").append(rightRecursiveRules).append("\n");
        analysis.append("- Self-recursive non-terminals: ").append(selfRecursiveNonTerminals).append("\n\n");
        
        // Complexity assessment
        analysis.append("Complexity Assessment:\n");
        int totalSymbols = grammar.getNonTerminals().size() + grammar.getTerminals().size();
        if (totalSymbols <= 5 && grammar.getRules().size() <= 10) {
            analysis.append("- Overall complexity: LOW\n");
            analysis.append("- Expected PDA size: Small (< 20 transitions)\n");
        } else if (totalSymbols <= 15 && grammar.getRules().size() <= 25) {
            analysis.append("- Overall complexity: MEDIUM\n");
            analysis.append("- Expected PDA size: Medium (20-50 transitions)\n");
        } else {
            analysis.append("- Overall complexity: HIGH\n");
            analysis.append("- Expected PDA size: Large (> 50 transitions)\n");
        }
        
        // Recommendations
        analysis.append("\nRecommendations:\n");
        if (leftRecursiveRules > 0) {
            analysis.append("- Consider eliminating left recursion for better parsing performance\n");
        }
        if (selfRecursiveNonTerminals > grammar.getNonTerminals().size() / 2) {
            analysis.append("- High recursion detected - ensure adequate stack space for PDA simulation\n");
        }
        if (grammar.getUnreachableSymbols().size() > 0) {
            analysis.append("- Remove unreachable symbols to simplify the grammar\n");
        }
        
        return analysis.toString();
    }
    
    /**
     * Tests the generated PDA with sample strings (simulation).
     * @param pda The PDA to test
     * @param testStrings Array of test strings
     * @return Test results showing which strings should be accepted/rejected
     */
    public String testPDAWithSamples(PDA pda, String[] testStrings) {
        if (pda == null) {
            return "Cannot test: PDA is null";
        }
        
        StringBuilder results = new StringBuilder();
        results.append("PDA Test Results:\n");
        results.append("================\n\n");
        
        results.append("Note: This is a theoretical analysis based on PDA structure.\n");
        results.append("Actual string acceptance would require full PDA simulation.\n\n");
        
        for (String testString : testStrings) {
            results.append("Test string: \"").append(testString).append("\"\n");
            results.append("  - Input symbols: ");
            
            boolean validInput = true;
            for (char c : testString.toCharArray()) {
                String symbol = String.valueOf(c);
                if (!pda.getInputAlphabet().contains(symbol)) {
                    validInput = false;
                    break;
                }
            }
            
            if (validInput || testString.isEmpty()) {
                results.append("Valid (all symbols in input alphabet)\n");
                results.append("  - Theoretical result: Requires full simulation\n");
            } else {
                results.append("Invalid (contains symbols not in input alphabet)\n");
                results.append("  - Theoretical result: REJECTED\n");
            }
            results.append("\n");
        }
        
        return results.toString();
    }
}