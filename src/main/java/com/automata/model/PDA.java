package com.automata.model;

import java.util.*;

/**
 * this represents a pushdown automaton
 * it's got states, input alphabet, stack alphabet, initial state,
 * initial stack symbol, accepting states, and transitions
 */
public class PDA {
    private Set<String> states;
    private Set<String> inputAlphabet;
    private Set<String> stackAlphabet;
    private String initialState;
    private String initialStackSymbol;
    private Set<String> acceptingStates;
    private List<PDATransition> transitions;
    
    /**
     * makes a new empty PDA
     */
    public PDA() {
        this.states = new HashSet<>();
        this.inputAlphabet = new HashSet<>();
        this.stackAlphabet = new HashSet<>();
        this.acceptingStates = new HashSet<>();
        this.transitions = new ArrayList<>();
        this.initialState = null;
        this.initialStackSymbol = null;
    }
    
    /**
     * Creates a PDA with specified components.
     * @param states Set of states
     * @param inputAlphabet Set of input symbols
     * @param stackAlphabet Set of stack symbols
     * @param initialState The initial state
     * @param initialStackSymbol The initial stack symbol
     * @param acceptingStates Set of accepting states
     * @param transitions List of transitions
     */
    public PDA(Set<String> states, Set<String> inputAlphabet, Set<String> stackAlphabet,
               String initialState, String initialStackSymbol, Set<String> acceptingStates,
               List<PDATransition> transitions) {
        this.states = new HashSet<>(states);
        this.inputAlphabet = new HashSet<>(inputAlphabet);
        this.stackAlphabet = new HashSet<>(stackAlphabet);
        this.initialState = initialState;
        this.initialStackSymbol = initialStackSymbol;
        this.acceptingStates = new HashSet<>(acceptingStates);
        this.transitions = new ArrayList<>(transitions);
    }
    
    /**
     * adds a state to the PDA
     * @param state the state you want to add
     */
    public void addState(String state) {
        if (state != null && !state.trim().isEmpty()) {
            states.add(state);
        }
    }
    
    /**
     * adds an input symbol to the alphabet
     * @param symbol the symbol to add
     */
    public void addInputSymbol(String symbol) {
        if (symbol != null && !symbol.trim().isEmpty()) {
            inputAlphabet.add(symbol);
        }
    }
    
    /**
     * Adds a stack symbol to the alphabet.
     * @param symbol The stack symbol to add
     */
    public void addStackSymbol(String symbol) {
        if (symbol != null && !symbol.trim().isEmpty()) {
            stackAlphabet.add(symbol);
        }
    }
    
    /**
     * Adds an accepting state.
     * @param state The accepting state to add
     */
    public void addAcceptingState(String state) {
        if (state != null && !state.trim().isEmpty()) {
            states.add(state);
            acceptingStates.add(state);
        }
    }
    
    /**
     * Adds an accepting state without automatically adding it to the states set (for validation testing).
     * @param state The accepting state to add
     */
    public void addAcceptingStateOnly(String state) {
        if (state != null && !state.trim().isEmpty()) {
            acceptingStates.add(state);
        }
    }
    
    /**
     * Adds a transition to the PDA.
     * @param transition The transition to add
     */
    public void addTransition(PDATransition transition) {
        if (transition != null && transition.isValid()) {
            transitions.add(transition);
            
            // automatically add states and symbols where they belong
            addState(transition.getFromState());
            addState(transition.getToState());
            
            if (!transition.isEpsilonTransition()) {
                addInputSymbol(transition.getInputSymbol());
            }
            
            if (transition.isPop()) {
                addStackSymbol(transition.getStackTop());
            }
            
            if (transition.isPush()) {
                for (char c : transition.getStackPush().toCharArray()) {
                    addStackSymbol(String.valueOf(c));
                }
            }
        }
    }
    
    /**
     * Adds a transition without automatically adding states (for validation testing).
     * @param transition The transition to add
     */
    public void addTransitionOnly(PDATransition transition) {
        if (transition != null && transition.isValid()) {
            transitions.add(transition);
        }
    }
    
    /**
     * Adds a transition using individual parameters.
     * @param fromState Source state
     * @param inputSymbol Input symbol
     * @param stackTop Stack top symbol
     * @param toState Destination state
     * @param stackPush Symbols to push
     */
    public void addTransition(String fromState, String inputSymbol, String stackTop,
                             String toState, String stackPush) {
        PDATransition transition = new PDATransition(fromState, inputSymbol, stackTop, toState, stackPush);
        addTransition(transition);
    }
    
    /**
     * Gets all transitions from a specific state.
     * @param state The state to find transitions from
     * @return List of transitions from the given state
     */
    public List<PDATransition> getTransitionsFrom(String state) {
        List<PDATransition> result = new ArrayList<>();
        for (PDATransition transition : transitions) {
            if (transition.getFromState().equals(state)) {
                result.add(transition);
            }
        }
        return result;
    }
    
    /**
     * Gets all transitions to a specific state.
     * @param state The state to find transitions to
     * @return List of transitions to the given state
     */
    public List<PDATransition> getTransitionsTo(String state) {
        List<PDATransition> result = new ArrayList<>();
        for (PDATransition transition : transitions) {
            if (transition.getToState().equals(state)) {
                result.add(transition);
            }
        }
        return result;
    }
    
    /**
     * Checks if the PDA is valid.
     * @return true if the PDA is valid, false otherwise
     */
    public boolean isValid() {
        // gotta have at least one state
        if (states.isEmpty()) {
            return false;
        }
        
        // need an initial state
        if (initialState == null || !states.contains(initialState)) {
            return false;
        }
        
        // need an initial stack symbol
        if (initialStackSymbol == null || initialStackSymbol.trim().isEmpty()) {
            return false;
        }
        
        // all accepting states gotta be real states
        for (String acceptingState : acceptingStates) {
            if (!states.contains(acceptingState)) {
                return false;
            }
        }
        
        // all transitions gotta be valid and use real states
        for (PDATransition transition : transitions) {
            if (!transition.isValid()) {
                return false;
            }
            
            if (!states.contains(transition.getFromState()) ||
                !states.contains(transition.getToState())) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Gets validation errors for the PDA.
     * @return List of validation error messages
     */
    public List<String> getValidationErrors() {
        List<String> errors = new ArrayList<>();
        
        if (states.isEmpty()) {
            errors.add("PDA must have at least one state");
        }
        
        if (initialState == null) {
            errors.add("PDA must have an initial state");
        } else if (!states.contains(initialState)) {
            errors.add("Initial state '" + initialState + "' must be in the state set");
        }
        
        if (initialStackSymbol == null || initialStackSymbol.trim().isEmpty()) {
            errors.add("PDA must have an initial stack symbol");
        }
        
        for (String acceptingState : acceptingStates) {
            if (!states.contains(acceptingState)) {
                errors.add("Accepting state '" + acceptingState + "' must be in the state set");
            }
        }
        
        for (int i = 0; i < transitions.size(); i++) {
            PDATransition transition = transitions.get(i);
            if (!transition.isValid()) {
                errors.add("Transition " + (i + 1) + " is invalid: " + transition.toString());
            } else {
                if (!states.contains(transition.getFromState())) {
                    errors.add("Transition " + (i + 1) + " references undefined state: " + transition.getFromState());
                }
                if (!states.contains(transition.getToState())) {
                    errors.add("Transition " + (i + 1) + " references undefined state: " + transition.getToState());
                }
            }
        }
        
        return errors;
    }
    
    /**
     * Gets a formatted string representation of the PDA for display.
     * @return Formatted PDA description
     */
    public String getFormattedOutput() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Pushdown Automaton (PDA):\n");
        sb.append("========================\n\n");
        
        sb.append("States (Q): ").append(states).append("\n");
        sb.append("Input Alphabet (Sigma): ").append(inputAlphabet).append("\n");
        sb.append("Stack Alphabet (Gamma): ").append(stackAlphabet).append("\n");
        sb.append("Initial State (q0): ").append(initialState).append("\n");
        sb.append("Initial Stack Symbol (Z0): ").append(initialStackSymbol).append("\n");
        sb.append("Accepting States (F): ").append(acceptingStates).append("\n\n");
        
        sb.append("Transition Function (delta):\n");
        sb.append("------------------------\n");
        
        if (transitions.isEmpty()) {
            sb.append("No transitions defined.\n");
        } else {
            for (PDATransition transition : transitions) {
                sb.append(transition.toString()).append("\n");
            }
        }
        
        return sb.toString();
    }
    
    // Getters
    public Set<String> getStates() {
        return new HashSet<>(states);
    }
    
    public Set<String> getInputAlphabet() {
        return new HashSet<>(inputAlphabet);
    }
    
    public Set<String> getStackAlphabet() {
        return new HashSet<>(stackAlphabet);
    }
    
    public String getInitialState() {
        return initialState;
    }
    
    public String getInitialStackSymbol() {
        return initialStackSymbol;
    }
    
    public Set<String> getAcceptingStates() {
        return new HashSet<>(acceptingStates);
    }
    
    public List<PDATransition> getTransitions() {
        return new ArrayList<>(transitions);
    }
    
    // Setters
    public void setInitialState(String initialState) {
        this.initialState = initialState;
        // Don't automatically add to states - it should be explicitly added
    }
    
    public void setInitialStackSymbol(String initialStackSymbol) {
        this.initialStackSymbol = initialStackSymbol;
        if (initialStackSymbol != null) {
            addStackSymbol(initialStackSymbol);
        }
    }
    
    @Override
    public String toString() {
        return getFormattedOutput();
    }
}