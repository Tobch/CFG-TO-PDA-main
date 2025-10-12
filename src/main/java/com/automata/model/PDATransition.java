package com.automata.model;

import java.util.Objects;

/**
 * represents a transition in a pushdown automaton
 * it goes (fromState, inputSymbol, stackTop) -> (toState, stackPush)
 * stackPush is what gets pushed on the stack (empty means just pop)
 */
public class PDATransition {
    private String fromState;
    private String inputSymbol;
    private String stackTop;
    private String toState;
    private String stackPush;
    
    /**
     * makes a new PDA transition
     * @param fromState where we start
     * @param inputSymbol what we read (use "epsilon" for epsilon transitions)
     * @param stackTop what needs to be on top of stack
     * @param toState where we go
     * @param stackPush what to push on stack (empty means just pop)
     */
    public PDATransition(String fromState, String inputSymbol, String stackTop, 
                        String toState, String stackPush) {
        this.fromState = fromState;
        this.inputSymbol = inputSymbol;
        this.stackTop = stackTop;
        this.toState = toState;
        this.stackPush = stackPush;
    }
    
    /**
     * checks if this is an epsilon transition (doesn't read input)
     * @return true if it's epsilon
     */
    public boolean isEpsilonTransition() {
        return inputSymbol == null ||
               inputSymbol.equals("ε") || inputSymbol.equalsIgnoreCase("epsilon") || inputSymbol.isEmpty();
    }
    
    /**
     * checks if this transition pops something from the stack
     * @return true if it pops
     */
    public boolean isPop() {
        return stackTop != null && !stackTop.isEmpty() && !stackTop.equalsIgnoreCase("epsilon");
    }
    
    /**
     * Checks if this transition pushes to the stack.
     * @return true if stackPush is not empty
     */
    public boolean isPush() {
        return stackPush != null && !stackPush.isEmpty() && !stackPush.equalsIgnoreCase("epsilon");
    }
    
    /**
     * Gets the net effect on stack size.
     * @return positive for net push, negative for net pop, 0 for no change
     */
    public int getStackEffect() {
        int popCount = isPop() ? 1 : 0;
        int pushCount = 0;
        if (isPush()) {
            // count tokens rather than characters
            String[] tokens = stackPush.trim().split("\\s+");
            pushCount = tokens.length;
        }
        return pushCount - popCount;
    }
    
    /**
     * checks if this transition makes sense
     * @return true if it's valid
     */
    public boolean isValid() {
        // state names can't be null or empty
        if (fromState == null || fromState.trim().isEmpty() ||
            toState == null || toState.trim().isEmpty()) {
            return false;
        }
        
        // input symbol gotta be defined (can be epsilon though)
        if (inputSymbol == null) {
            return false;
        }
        
        // stack symbols can be null/empty for epsilon
        return true;
    }
    
    // Getters
    public String getFromState() {
        return fromState;
    }
    
    public String getInputSymbol() {
        return inputSymbol;
    }
    
    public String getStackTop() {
        return stackTop;
    }
    
    public String getToState() {
        return toState;
    }
    
    public String getStackPush() {
        return stackPush;
    }
    
    // Setters
    public void setFromState(String fromState) {
        this.fromState = fromState;
    }
    
    public void setInputSymbol(String inputSymbol) {
        this.inputSymbol = inputSymbol;
    }
    
    public void setStackTop(String stackTop) {
        this.stackTop = stackTop;
    }
    
    public void setToState(String toState) {
        this.toState = toState;
    }
    
    public void setStackPush(String stackPush) {
        this.stackPush = stackPush;
    }

    /**
     * Returns a compact label suitable for diagram edges.
     * Example outputs:
     *  - "ε, pop:Z0 -> push:E Z0"
     *  - "id, pop:id -> push:ε"
     *
     * PDADiagramPanel can call this to obtain the text for an edge.
     */
    public String getLabel() {
        // normalize input symbol
        String in;
        if (inputSymbol == null || inputSymbol.isEmpty() || inputSymbol.equals("ε") || inputSymbol.equalsIgnoreCase("epsilon")) {
            in = "ε";
        } else {
            in = inputSymbol;
        }
        
        // normalize pop symbol
        String pop;
        if (stackTop == null || stackTop.isEmpty() || stackTop.equals("ε") || stackTop.equalsIgnoreCase("epsilon")) {
            pop = "ε";
        } else {
            pop = stackTop;
        }
        
        // normalize push string to space-separated tokens
        String push;
        if (stackPush == null || stackPush.isEmpty() || stackPush.equals("ε") || stackPush.equalsIgnoreCase("epsilon")) {
            push = "ε";
        } else {
            String[] tokens = stackPush.trim().split("\\s+");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < tokens.length; i++) {
                if (i > 0) sb.append(" ");
                sb.append(tokens[i]);
            }
            push = sb.toString();
        }
        
        return String.format("%s, pop:%s -> push:%s", in, pop, push);
        
    }

    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("delta(").append(fromState).append(", ");
        
        // input symbol
        if (inputSymbol == null || inputSymbol.isEmpty() || inputSymbol.equals("ε") || inputSymbol.equalsIgnoreCase("epsilon")) {
            sb.append("epsilon");
        } else {
            sb.append(inputSymbol);
        }
        
        sb.append(", ");
        
        // stack top
        if (stackTop == null || stackTop.isEmpty() || stackTop.equals("ε") || stackTop.equalsIgnoreCase("epsilon")) {
            sb.append("epsilon");
        } else {
            sb.append(stackTop);
        }
        
        sb.append(") = (").append(toState).append(", ");
        
        // Stack push - normalize by splitting tokens and joining with a single space so e.g. ") E (" prints cleanly
        if (stackPush == null || stackPush.isEmpty() || stackPush.equals("ε") || stackPush.equalsIgnoreCase("epsilon")) {
            sb.append("epsilon");
        } else {
            String[] tokens = stackPush.trim().split("\\s+");
            StringBuilder pushFormatted = new StringBuilder();
            for (int i = 0; i < tokens.length; i++) {
                if (i > 0) pushFormatted.append(" ");
                pushFormatted.append(tokens[i]);
            }
            sb.append(pushFormatted.toString());
        }
        
        sb.append(")");
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        PDATransition that = (PDATransition) obj;
        return Objects.equals(fromState, that.fromState) &&
               Objects.equals(inputSymbol, that.inputSymbol) &&
               Objects.equals(stackTop, that.stackTop) &&
               Objects.equals(toState, that.toState) &&
               Objects.equals(stackPush, that.stackPush);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(fromState, inputSymbol, stackTop, toState, stackPush);
    }
}
