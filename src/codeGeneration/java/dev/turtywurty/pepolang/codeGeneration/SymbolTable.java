package dev.turtywurty.pepolang.codeGeneration;

import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.*;

public class SymbolTable {
    private final Stack<Map<String, List<Symbol>>> scopes = new Stack<>();

    public SymbolTable() {
        enterScope();
    }

    public void enterScope() {
        this.scopes.push(new HashMap<>());
    }

    public void exitScope() {
        if (this.scopes.isEmpty())
            throw new IllegalStateException("Cannot exit scope when there are no scopes to exit!");

        this.scopes.pop();
    }

    public void insert(String name, Symbol symbol) {
        if (this.scopes.isEmpty())
            throw new IllegalStateException("Cannot insert symbol when there are no scopes to insert into!");

        Map<String, List<Symbol>> currentScope = this.scopes.peek();
        if (currentScope.containsKey(name))
            throw new IllegalStateException("Symbol with name '" + name + "' already exists in the current scope!");

        currentScope.computeIfAbsent(name, k -> new ArrayList<>()).add(symbol);
    }

    public Symbol lookup(String name, SymbolCategory category) {
        for (int i = this.scopes.size() - 1; i >= 0; i--) {
            Map<String, List<Symbol>> currentScope = this.scopes.get(i);
            if (currentScope.containsKey(name)) {
                List<Symbol> symbols = currentScope.get(name);
                for (Symbol symbol : symbols) {
                    if (category.contains(symbol.type()))
                        return symbol;
                }
            }
        }

        return null;
    }

    public Symbol lookupInCurrentScope(String name, SymbolCategory category) {
        if (this.scopes.isEmpty())
            throw new IllegalStateException("Cannot lookup symbol in current scope when there are no scopes to lookup in!");

        Map<String, List<Symbol>> currentScope = this.scopes.peek();
        if (currentScope.containsKey(name)) {
            List<Symbol> symbols = currentScope.get(name);
            for (Symbol symbol : symbols) {
                if (category.contains(symbol.type()))
                    return symbol;
            }
        }

        return null;
    }

    public boolean isInCurrentScope(String name) {
        if (this.scopes.isEmpty())
            throw new IllegalStateException("Cannot check if symbol is in current scope when there are no scopes to check in!");

        return this.scopes.peek().containsKey(name);
    }

    public void print() {
        System.out.println("Symbol Table:");
        for (int i = 0; i < scopes.size(); i++) {
            System.out.println("Scope " + i + ":");
            for (Map.Entry<String, List<Symbol>> entry : scopes.get(i).entrySet()) {
                System.out.println(entry.getKey() + ":");
                for (Symbol symbol : entry.getValue()) {
                    System.out.println("\t" + symbol);
                }
            }
        }
    }

    public enum SymbolCategory {
        VARIABLE(SymbolType.VARIABLE, SymbolType.FIELD, SymbolType.PARAMETER, SymbolType.LOCAL),
        FUNCTION(SymbolType.FUNCTION, SymbolType.METHOD, SymbolType.CONSTRUCTOR),
        CLASS(SymbolType.CLASS);

        private final List<SymbolType> types = new ArrayList<>();

        SymbolCategory(SymbolType... types) {
            this.types.addAll(Arrays.asList(types));
        }

        public boolean contains(SymbolType type) {
            return this.types.contains(type);
        }
    }

    public record Symbol(String name, SymbolTable.SymbolType type, LLVMValueRef llvmValue) {
        @Override
        public String toString() {
            return "Symbol{" +
                    "name='" + name + '\'' +
                    ", type=" + type +
                    ", llvmValue=" + llvmValue +
                    '}';
        }
    }

    public enum SymbolType {
        VARIABLE,
        FUNCTION,
        CLASS,
        METHOD,
        CONSTRUCTOR,
        FIELD,
        PARAMETER,
        LOCAL;
    }
}
