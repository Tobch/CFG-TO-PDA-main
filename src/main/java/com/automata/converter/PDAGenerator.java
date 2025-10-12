package com.automata.converter;

import com.automata.model.CFG;
import com.automata.model.PDA;
import com.automata.model.PDATransition;
import com.automata.model.ProductionRule;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * this converts your CFG into a PDA
 * supports two modes:
 *  - classic three-state PDA (q0, q1, q2) [default]
 *  - per-nonterminal-state PDA (q0, s<NT> for each nonterminal, qf) (better for visualization)
 *
 * Use PDAGenerator() for default three-state behavior.
 * Use PDAGenerator(true) to enable per-nonterminal-state mode.
 */
public class PDAGenerator {

    private static final String INITIAL_STATE = "q0";
    private static final String LOOP_STATE = "q1";
    private static final String THREESTATE_FINAL = "q2";
    private static final String PERNT_FINAL = "qf";
    private static final String INITIAL_STACK_SYMBOL = "Z0";

    // Token pattern used for tokenization: multi-char identifiers OR any single non-space char
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[A-Za-z][A-Za-z0-9_]*|\\S");

    // If true, build one PDA state per non-terminal (visualization-friendly).
    private final boolean usePerNonterminalStates;

    /**
     * Default constructor — three-state PDA behavior (compatible with original).
     */
    public PDAGenerator() {
        this(false);
    }

    /**
     * Constructor with option to use per-nonterminal states.
     * @param usePerNonterminalStates if true, create s<NT> states for each non-terminal.
     */
    public PDAGenerator(boolean usePerNonterminalStates) {
        this.usePerNonterminalStates = usePerNonterminalStates;
    }

    /**
     * Main entry: convert CFG to PDA (respects the per-nonterminal flag).
     * @param grammar source CFG
     * @return PDA or null if conversion cannot proceed
     */
    public PDA convertCFGToPDA(CFG grammar) {
        if (grammar == null || !grammar.isValid()) return null;

        if (usePerNonterminalStates) {
            return convertPerNonterminalPDA(grammar);
        } else {
            return convertThreeStatePDA(grammar);
        }
    }

    // --------------------------
    // Three-state PDA generation
    // --------------------------
    private PDA convertThreeStatePDA(CFG grammar) {
        PDA pda = new PDA();

        // make the states
        pda.addState(INITIAL_STATE);
        pda.addState(LOOP_STATE);
        pda.addState(THREESTATE_FINAL);

        // set up the starting stuff
        pda.setInitialState(INITIAL_STATE);
        pda.setInitialStackSymbol(INITIAL_STACK_SYMBOL);
        pda.addAcceptingState(THREESTATE_FINAL);

        // make all the transitions
        createInitialTransitionThreeState(pda, grammar);
        createGrammarTransitionsThreeState(pda, grammar);
        createTerminalTransitionsThreeState(pda, grammar);
        createFinalTransitionThreeState(pda);

        // debug print
        debugPrintPDA(pda);

        return pda;
    }

    private void createInitialTransitionThreeState(PDA pda, CFG grammar) {
        String startSymbol = grammar.getStartSymbol();
        if (startSymbol == null) startSymbol = "";

        String push = (startSymbol.isEmpty() ? INITIAL_STACK_SYMBOL : (startSymbol + " " + INITIAL_STACK_SYMBOL));

        PDATransition initialTransition = new PDATransition(
                INITIAL_STATE,
                "epsilon",
                INITIAL_STACK_SYMBOL,
                LOOP_STATE,
                push
        );

        pda.addTransition(initialTransition);
    }

    private void createGrammarTransitionsThreeState(PDA pda, CFG grammar) {
        for (ProductionRule rule : grammar.getRules()) {
            String leftSide = rule.getLeftSide();
            List<String> rightSide = rule.getRightSide();
            List<String> norm = normalizeTokens(rightSide);

            String stackPush;
            if (rule.isEpsilonProduction() || norm == null || norm.isEmpty()) {
                stackPush = "epsilon";
            } else {
                List<String> rev = new ArrayList<>(norm);
                Collections.reverse(rev);
                stackPush = String.join(" ", rev);
            }

            PDATransition grammarTransition = new PDATransition(
                    LOOP_STATE,
                    "epsilon",
                    leftSide,
                    LOOP_STATE,
                    stackPush
            );

            pda.addTransition(grammarTransition);
        }
    }

    private void createTerminalTransitionsThreeState(PDA pda, CFG grammar) {
        for (String terminal : grammar.getTerminals()) {
            if (terminal == null) continue;
            if (terminal.equalsIgnoreCase("epsilon") || terminal.equals("ε")) continue;

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

    private void createFinalTransitionThreeState(PDA pda) {
        PDATransition finalTransition = new PDATransition(
                LOOP_STATE,
                "epsilon",
                INITIAL_STACK_SYMBOL,
                THREESTATE_FINAL,
                "epsilon"
        );

        pda.addTransition(finalTransition);
    }

    // ------------------------------------
    // Per-nonterminal-state PDA generation
    // ------------------------------------
    private PDA convertPerNonterminalPDA(CFG grammar) {
        PDA pda = new PDA();

        // create base states
        pda.addState(INITIAL_STATE);
        pda.addState(PERNT_FINAL); // final for per-NT mode

        // create one state per non-terminal: s<NT>
        Map<String, String> ntToState = new LinkedHashMap<>();
        for (String nt : grammar.getNonTerminals()) {
            String sname = "s" + sanitizeStateName(nt);
            // ensure uniqueness if collision
            int suffix = 1;
            String candidate = sname;
            while (pda.getStates().contains(candidate)) {
                candidate = sname + "_" + suffix++;
            }
            sname = candidate;
            ntToState.put(nt, sname);
            pda.addState(sname);
        }

        // initial settings
        pda.setInitialState(INITIAL_STATE);
        pda.setInitialStackSymbol(INITIAL_STACK_SYMBOL);
        pda.addAcceptingState(PERNT_FINAL);

        // initial transition: q0, eps, Z0 -> s<Start>, Start Z0
        String startNT = grammar.getStartSymbol();
        String startState = ntToState.getOrDefault(startNT, INITIAL_STATE);
        String initialPush = (startNT == null || startNT.isEmpty()) ? INITIAL_STACK_SYMBOL : (startNT + " " + INITIAL_STACK_SYMBOL);

        pda.addTransition(new PDATransition(
                INITIAL_STATE,
                "epsilon",
                INITIAL_STACK_SYMBOL,
                startState,
                initialPush
        ));

        // for each production A -> alpha: (sA, eps, A) -> (sA, reverse(alpha))
        for (ProductionRule rule : grammar.getRules()) {
            String left = rule.getLeftSide();
            List<String> rhsRaw = rule.getRightSide();
            List<String> rhs = normalizeTokens(rhsRaw);

            String fromState = ntToState.getOrDefault(left, startState);

            String stackPush;
            if (rule.isEpsilonProduction() || rhs == null || rhs.isEmpty()) {
                stackPush = "epsilon";
            } else {
                List<String> rev = new ArrayList<>(rhs);
                Collections.reverse(rev);
                stackPush = String.join(" ", rev);
            }

            pda.addTransition(new PDATransition(
                    fromState,
                    "epsilon",
                    left,
                    fromState,
                    stackPush
            ));
        }

        // terminal transitions: for each terminal t, add (sX, t, t) -> (sX, epsilon) for every sX
        Set<String> terminals = grammar.getTerminals();
        for (String term : terminals) {
            if (term == null) continue;
            if (term.equalsIgnoreCase("epsilon") || term.equals("ε")) continue;

            for (String nt : ntToState.keySet()) {
                String sstate = ntToState.get(nt);
                pda.addTransition(new PDATransition(
                        sstate,
                        term,
                        term,
                        sstate,
                        "epsilon"
                ));
            }
        }

        // final transition: (sStart, eps, Z0) -> (qf, eps)
        pda.addTransition(new PDATransition(
                startState,
                "epsilon",
                INITIAL_STACK_SYMBOL,
                PERNT_FINAL,
                "epsilon"
        ));

        // debug print
        debugPrintPDA(pda);

        return pda;
    }

    // -----------------------
    // Helpers & utilities
    // -----------------------

    /**
     * Defensive helper: ensure a list of tokens is split into atomic tokens.
     * If a token contains multiple atomic pieces like "(E)" or "id)", this will
     * re-tokenize it so "(" and ")" are separate tokens and multi-char identifiers remain intact.
     *
     * @param tokens raw token list (may already be atomic)
     * @return normalized list of atomic tokens
     */
    private List<String> normalizeTokens(List<String> tokens) {
        List<String> normalized = new ArrayList<>();
        if (tokens == null) return normalized;

        for (String tok : tokens) {
            if (tok == null) continue;
            String trimmed = tok.trim();
            if (trimmed.isEmpty()) continue;

            Matcher m = TOKEN_PATTERN.matcher(trimmed);
            int found = 0;
            while (m.find()) {
                normalized.add(m.group());
                found++;
            }
            if (found == 0) {
                // fallback: if pattern didn't match (unlikely), keep original token
                normalized.add(trimmed);
            }
        }
        return normalized;
    }

    private String sanitizeStateName(String raw) {
        if (raw == null) return "X";
        // remove spaces and illegal chars for state naming
        return raw.replaceAll("\\s+", "_").replaceAll("[^A-Za-z0-9_]", "_");
    }

    private void debugPrintPDA(PDA pda) {
        System.out.println("=== PDA Transitions ===");
        for (PDATransition t : pda.getTransitions()) {
            try {
                System.out.println(t.getFromState() + " -- " + t.getLabel() + " --> " + t.getToState());
            } catch (Exception ex) {
                // fallback to toString
                System.out.println(t.toString());
            }
        }
        System.out.println("=======================");
    }

    // -----------------------
    // Validation & wrappers
    // -----------------------

    public boolean validateConversion(PDA pda) {
        if (pda == null) return false;
        return pda.isValid();
    }

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

        checkForConversionWarnings(grammar, warnings);

        PDA pda = convertCFGToPDA(grammar);

        if (pda == null || !pda.isValid()) {
            errors.add("Failed to generate valid PDA from grammar");
            if (pda != null) {
                errors.addAll(pda.getValidationErrors());
            }
        }

        return new ConversionResult(pda, errors, warnings);
    }

    private void checkForConversionWarnings(CFG grammar, List<String> warnings) {
        for (String nonTerminal : grammar.getNonTerminals()) {
            List<ProductionRule> rules = grammar.getRulesFor(nonTerminal);
            for (ProductionRule rule : rules) {
                List<String> rhs = normalizeTokens(rule.getRightSide());
                if (rhs != null && !rhs.isEmpty() && rhs.get(0).equals(nonTerminal)) {
                    warnings.add("Left recursion detected in rule: " + rule.toString() +
                            " - this may cause infinite loops in some parsing algorithms");
                }
            }
        }

        Set<String> unreachable = grammar.getUnreachableSymbols();
        if (!unreachable.isEmpty()) {
            warnings.add("Unreachable non-terminals found: " + unreachable +
                    " - these will not affect the PDA but indicate potential grammar issues");
        }

        checkForAmbiguousPatterns(grammar, warnings);
    }

    private void checkForAmbiguousPatterns(CFG grammar, List<String> warnings) {
        for (String nonTerminal : grammar.getNonTerminals()) {
            List<ProductionRule> rules = grammar.getRulesFor(nonTerminal);

            for (int i = 0; i < rules.size(); i++) {
                for (int j = i + 1; j < rules.size(); j++) {
                    ProductionRule rule1 = rules.get(i);
                    ProductionRule rule2 = rules.get(j);

                    List<String> rhs1 = normalizeTokens(rule1.getRightSide());
                    List<String> rhs2 = normalizeTokens(rule2.getRightSide());

                    if (!rhs1.isEmpty() && !rhs2.isEmpty()) {
                        String first1 = rhs1.get(0);
                        String first2 = rhs2.get(0);

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

    // -----------------------
    // Utility / analysis methods
    // -----------------------

    public String getConversionDescription(CFG grammar) {
        if (grammar == null) return "Cannot describe conversion: grammar is null";

        StringBuilder description = new StringBuilder();
        description.append("CFG to PDA Conversion Process:\n");
        description.append("============================\n\n");

        if (!usePerNonterminalStates) {
            description.append("1. Create three states: q0 (initial), q1 (loop), q2 (final)\n");
            description.append("2. Set initial stack symbol: Z0\n");
            description.append("3. Create initial transition: (q0, epsilon, Z0) -> (q1, ")
                    .append(grammar.getStartSymbol()).append(" ").append(INITIAL_STACK_SYMBOL).append(")\n\n");

            description.append("4. For each production rule, create epsilon transition:\n");
            for (ProductionRule rule : grammar.getRules()) {
                description.append("   Rule: ").append(rule.toString()).append("\n");
                description.append("   Transition: (q1, epsilon, ").append(rule.getLeftSide()).append(") -> (q1, ");

                List<String> rightSide = normalizeTokens(rule.getRightSide());
                if (rightSide == null || rightSide.isEmpty() || rule.isEpsilonProduction()) {
                    description.append("epsilon");
                } else {
                    List<String> rev = new ArrayList<>(rightSide);
                    Collections.reverse(rev);
                    description.append(String.join(" ", rev));
                }
                description.append(")\n\n");
            }

            description.append("5. For each terminal symbol, create matching transition:\n");
            for (String terminal : grammar.getTerminals()) {
                if (!terminal.equalsIgnoreCase("epsilon")) {
                    description.append("   Terminal: ").append(terminal).append("\n");
                    description.append("   Transition: (q1, ").append(terminal).append(", ").append(terminal).append(") -> (q1, epsilon)\n");
                }
            }

            description.append("\n6. Create final transition: (q1, epsilon, Z0) -> (q2, epsilon)\n");
        } else {
            description.append("Per-nonterminal-state PDA:\n");
            description.append(" - States: q0, s<NT> for each non-terminal, ").append(PERNT_FINAL).append("\n");
            description.append(" - Initial: (q0, epsilon, Z0) -> (sStart, Start Z0)\n");
            description.append(" - For A -> alpha: (sA, epsilon, A) -> (sA, reverse(alpha))\n");
            description.append(" - For terminals: (sA, a, a) -> (sA, epsilon)\n");
            description.append(" - Final: (sStart, epsilon, Z0) -> (").append(PERNT_FINAL).append(", epsilon)\n");
        }

        return description.toString();
    }

    public String analyzeGrammarComplexity(CFG grammar) {
        if (grammar == null) return "Cannot analyze: grammar is null";

        StringBuilder analysis = new StringBuilder();
        analysis.append("Grammar Complexity Analysis:\n");
        analysis.append("===========================\n\n");

        analysis.append("Basic Metrics:\n");
        analysis.append("- Non-terminals: ").append(grammar.getNonTerminals().size()).append("\n");
        analysis.append("- Terminals: ").append(grammar.getTerminals().size()).append("\n");
        analysis.append("- Production rules: ").append(grammar.getRules().size()).append("\n\n");

        int leftRecursiveRules = 0;
        int rightRecursiveRules = 0;
        int selfRecursiveNonTerminals = 0;

        for (String nonTerminal : grammar.getNonTerminals()) {
            List<ProductionRule> rules = grammar.getRulesFor(nonTerminal);
            boolean hasSelfRecursion = false;

            for (ProductionRule rule : rules) {
                List<String> rightSide = normalizeTokens(rule.getRightSide());
                if (!rightSide.isEmpty()) {
                    if (rightSide.get(0).equals(nonTerminal)) leftRecursiveRules++;
                    if (rightSide.get(rightSide.size() - 1).equals(nonTerminal)) rightRecursiveRules++;
                    if (rightSide.contains(nonTerminal)) hasSelfRecursion = true;
                }
            }
            if (hasSelfRecursion) selfRecursiveNonTerminals++;
        }

        analysis.append("Recursion Analysis:\n");
        analysis.append("- Left recursive rules: ").append(leftRecursiveRules).append("\n");
        analysis.append("- Right recursive rules: ").append(rightRecursiveRules).append("\n");
        analysis.append("- Self-recursive non-terminals: ").append(selfRecursiveNonTerminals).append("\n\n");

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

        analysis.append("\nRecommendations:\n");
        if (leftRecursiveRules > 0) analysis.append("- Consider eliminating left recursion for better parsing performance\n");
        if (selfRecursiveNonTerminals > grammar.getNonTerminals().size() / 2)
            analysis.append("- High recursion detected - ensure adequate stack space for PDA simulation\n");
        if (grammar.getUnreachableSymbols().size() > 0)
            analysis.append("- Remove unreachable symbols to simplify the grammar\n");

        return analysis.toString();
    }

    public String testPDAWithSamples(PDA pda, String[] testStrings) {
        if (pda == null) return "Cannot test: PDA is null";

        StringBuilder results = new StringBuilder();
        results.append("PDA Test Results:\n");
        results.append("================\n\n");

        results.append("Note: This is a theoretical analysis based on PDA structure.\n");
        results.append("Actual string acceptance would require full PDA simulation.\n\n");

        for (String testString : testStrings) {
            results.append("Test string: \"").append(testString).append("\"\n");
            results.append("  - Tokenizing input: ");

            List<String> tokens = tokenizeFromStringSafe(testString);
            results.append(tokens).append("\n");

            boolean validInput = true;
            Set<String> alphabet = pda.getInputAlphabet();
            for (String tok : tokens) {
                if (!alphabet.contains(tok)) {
                    validInput = false;
                    break;
                }
            }

            if (validInput || tokens.isEmpty()) {
                results.append("  - Valid (all tokens in input alphabet)\n");
                results.append("  - Theoretical result: Requires full simulation to determine acceptance\n");
            } else {
                results.append("  - Invalid (contains tokens not in input alphabet)\n");
                results.append("  - Theoretical result: REJECTED\n");
            }
            results.append("\n");
        }

        return results.toString();
    }

    private static List<String> tokenizeFromStringSafe(String s) {
        List<String> tokens = new ArrayList<>();
        if (s == null) return tokens;
        String trimmed = s.trim();
        if (trimmed.isEmpty()) return tokens;
        if (trimmed.equalsIgnoreCase("epsilon") || trimmed.equals("ε")) return tokens;

        Matcher m = TOKEN_PATTERN.matcher(trimmed);
        while (m.find()) {
            tokens.add(m.group());
        }
        return tokens;
    }

    // -----------------------
    // Result inner class
    // -----------------------
    public static class ConversionResult {
        private final PDA pda;
        private final List<String> errors;
        private final List<String> warnings;

        public ConversionResult(PDA pda, List<String> errors, List<String> warnings) {
            this.pda = pda;
            this.errors = new ArrayList<>(errors);
            this.warnings = new ArrayList<>(warnings);
        }

        public PDA getPDA() {
            return pda;
        }

        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }

        public List<String> getWarnings() {
            return new ArrayList<>(warnings);
        }

        public boolean isSuccessful() {
            return pda != null && errors.isEmpty();
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Conversion Result: ").append(isSuccessful() ? "SUCCESS" : "FAILED").append("\n");

            if (!errors.isEmpty()) {
                sb.append("Errors:\n");
                for (String error : errors) sb.append("  - ").append(error).append("\n");
            }

            if (!warnings.isEmpty()) {
                sb.append("Warnings:\n");
                for (String warning : warnings) sb.append("  - ").append(warning).append("\n");
            }

            return sb.toString();
        }
    }
}
