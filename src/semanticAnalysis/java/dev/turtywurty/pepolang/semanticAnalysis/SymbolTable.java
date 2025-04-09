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
        return getSymbols(name, symbol -> true);
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
}
