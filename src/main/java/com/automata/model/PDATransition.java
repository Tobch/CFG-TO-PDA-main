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
        return inputSymbol.equals("ε") || inputSymbol.equals("epsilon") || inputSymbol.isEmpty();
    }
    
    /**
     * checks if this transition pops something from the stack
     * @return true if it pops
     */
    public boolean isPop() {
        return stackTop != null && !stackTop.isEmpty() && !stackTop.equals("epsilon");
    }
    
    /**
     * Checks if this transition pushes to the stack.
     * @return true if stackPush is not empty
     */
    public boolean isPush() {
        return stackPush != null && !stackPush.isEmpty() && !stackPush.equals("epsilon");
    }
    
    /**
     * Gets the net effect on stack size.
     * @return positive for net push, negative for net pop, 0 for no change
     */
    public int getStackEffect() {
        int popCount = isPop() ? 1 : 0;
        int pushCount = isPush() ? stackPush.length() : 0;
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
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("delta(").append(fromState).append(", ");
        
        // input symbol
        if (inputSymbol.isEmpty() || inputSymbol.equals("ε") || inputSymbol.equals("epsilon")) {
            sb.append("epsilon");
        } else {
            sb.append(inputSymbol);
        }
        
        sb.append(", ");
        
        // stack top
        if (stackTop == null || stackTop.isEmpty() || stackTop.equals("ε")) {
            sb.append("epsilon");
        } else {
            sb.append(stackTop);
        }
        
        sb.append(") = (").append(toState).append(", ");
        
        // Stack push
        if (stackPush == null || stackPush.isEmpty() || stackPush.equals("ε")) {
            sb.append("epsilon");
        } else {
            sb.append(stackPush);
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