package dev.turtywurty.pepolang.codeGeneration;

import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class SymbolTable {
    private final Stack<Map<String, Symbol>> scopes = new Stack<>();

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

        Map<String, Symbol> currentScope = this.scopes.peek();
        if (currentScope.containsKey(name))
            throw new IllegalStateException("Symbol with name '" + name + "' already exists in the current scope!");

        currentScope.put(name, symbol);
    }

    public Symbol lookup(String name) {
        for (int i = this.scopes.size() - 1; i >= 0; i--) {
            Map<String, Symbol> currentScope = this.scopes.get(i);
            if (currentScope.containsKey(name))
                return currentScope.get(name);
        }

        return null;
    }

    public Symbol lookupInCurrentScope(String name) {
        if (this.scopes.isEmpty())
            throw new IllegalStateException("Cannot lookup symbol in current scope when there are no scopes to lookup in!");

        return this.scopes.peek().get(name);
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
            for (Map.Entry<String, Symbol> entry : scopes.get(i).entrySet()) {
                System.out.println("    " + entry.getKey() + " -> " + entry.getValue());
            }
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
        LOCAL
    }
}
