package dev.turtywurty.pepolang.semanticAnalysis;

import dev.turtywurty.pepolang.semanticAnalysis.symbol.ClassSymbol;
import dev.turtywurty.pepolang.semanticAnalysis.symbol.MethodSymbol;
import dev.turtywurty.pepolang.semanticAnalysis.symbol.Symbol;
import dev.turtywurty.pepolang.semanticAnalysis.symbol.VariableSymbol;

import java.util.*;
import java.util.function.Predicate;

public class SymbolTable {
    private final Deque<Map<String, List<Symbol>>> scopes = new ArrayDeque<>();

    public SymbolTable() {
        enterScope();
    }

    public void enterScope() {
        this.scopes.push(new HashMap<>());
    }

    public void exitScope() {
        if (this.scopes.isEmpty()) {
            System.err.println("Warning: Attempting to exit a scope when no scopes are present.");
            return;
        }

        if(this.scopes.size() == 1) {
            System.err.println("Warning: Attempting to exit the global scope. This may lead to undefined behavior.");
            return;
        }

        this.scopes.pop();
    }

    public void addSymbol(Symbol symbol) {
        if (!this.scopes.isEmpty()) {
            this.scopes.peek().computeIfAbsent(symbol.getName(), _ -> new ArrayList<>()).add(symbol);
        }
    }

    public List<Symbol> getSymbols(String name, Predicate<Symbol> predicate) {
        for (Map<String, List<Symbol>> scope : this.scopes) {
            List<Symbol> symbols = scope.get(name);
            if (symbols != null) {
                List<Symbol> filteredSymbols = new ArrayList<>();
                for (Symbol symbol : symbols) {
                    if (predicate.test(symbol)) {
                        filteredSymbols.add(symbol);
                    }
                }

                if (!filteredSymbols.isEmpty()) {
                    return filteredSymbols;
                }
            }
        }

        return Collections.emptyList();
    }

    public List<Symbol> getSymbols(String name) {
        return getSymbols(name, _ -> true);
    }

    public VariableSymbol getVariable(String name) {
        List<VariableSymbol> symbols = getSymbols(name, symbol -> symbol.getSymbolType() == SymbolType.VARIABLE)
                .stream()
                .filter(VariableSymbol.class::isInstance)
                .map(VariableSymbol.class::cast)
                .toList();
        if (!symbols.isEmpty()) {
            return symbols.getFirst();
        }

        return null;
    }

    public List<MethodSymbol> getMethods(String name) {
        return getSymbols(name, symbol -> symbol.getSymbolType() == SymbolType.METHOD)
                .stream()
                .filter(MethodSymbol.class::isInstance)
                .map(MethodSymbol.class::cast)
                .toList();
    }

    public List<Symbol> getFields(String name) {
        return getSymbols(name, symbol -> symbol.getSymbolType() == SymbolType.FIELD);
    }

    public ClassSymbol getClass(String name) {
        return getSymbols(name, symbol -> symbol.getSymbolType() == SymbolType.CLASS)
                .stream()
                .filter(ClassSymbol.class::isInstance)
                .map(ClassSymbol.class::cast)
                .findFirst()
                .orElse(null);
    }

    public boolean containsSymbol(String name, Predicate<Symbol> predicate) {
        return !getSymbols(name, predicate).isEmpty();
    }

    public boolean containsSymbol(String name) {
        return containsSymbol(name, symbol -> true);
    }

    public boolean containsSymbol(String name, SymbolType symbolType) {
        return containsSymbol(name, symbol -> symbol.getSymbolType() == symbolType);
    }

    /**
     * Checks if a symbol with the given name is defined ONLY in the current (top-most) scope.
     * Does not search in parent scopes.
     *
     * @param name The name of the symbol to check.
     * @return true if a symbol with the given name exists in the current scope, false otherwise.
     */
    public boolean isSymbolDefinedInCurrentScope(String name) {
        if (this.scopes.isEmpty() || name == null) {
            return false; // No scopes or no name to check
        }
        Map<String, List<Symbol>> currentScope = this.scopes.peek();
        // Check if the name exists as a key and the list of symbols for that name is not empty.
        List<Symbol> symbols = currentScope.get(name);
        return symbols != null && !symbols.isEmpty();
    }

    /**
     * Checks if a symbol with the given name and matching the predicate
     * is defined ONLY in the current (top-most) scope.
     * Does not search in parent scopes.
     *
     * @param name The name of the symbol to check.
     * @param predicate The predicate to test the symbol(s).
     * @return true if such a symbol exists in the current scope, false otherwise.
     */
    public boolean isSymbolDefinedInCurrentScope(String name, Predicate<Symbol> predicate) {
        if (this.scopes.isEmpty() || name == null || predicate == null) {
            return false;
        }
        Map<String, List<Symbol>> currentScope = this.scopes.peek();
        List<Symbol> symbolsInCurrentScope = currentScope.get(name);

        if (symbolsInCurrentScope != null) {
            for (Symbol symbol : symbolsInCurrentScope) {
                if (predicate.test(symbol)) {
                    return true; // Found a matching symbol in the current scope
                }
            }
        }
        return false; // No matching symbol found in the current scope
    }

    /**
     * Checks if a symbol with the given name and SymbolType
     * is defined ONLY in the current (top-most) scope.
     *
     * @param name The name of the symbol.
     * @param symbolType The type of the symbol.
     * @return true if such a symbol exists in the current scope, false otherwise.
     */
    public boolean isSymbolDefinedInCurrentScope(String name, SymbolType symbolType) {
        return isSymbolDefinedInCurrentScope(name, symbol -> symbol.getSymbolType() == symbolType);
    }
}
