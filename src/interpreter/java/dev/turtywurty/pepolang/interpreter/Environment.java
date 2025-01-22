package dev.turtywurty.pepolang.interpreter;

import dev.turtywurty.pepolang.lexer.Token;
import dev.turtywurty.pepolang.parser.RuntimeError;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Environment enclosing;
    private final Map<String, Object> variables = new HashMap<>();
    private final Map<String, Object> functions = new HashMap<>();

    // local environment
    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }
    
    // global environment
    public Environment() {
        this.enclosing = null;
    }

    public void defineVariable(String name, Object value) {
        if (this.variables.containsKey(name))
            throw new RuntimeError("Variable with name '" + name + "' already defined!");

        this.variables.put(name, value);
    }

    public void defineFunction(String name, Object value) {
        if (this.functions.containsKey(name))
            throw new RuntimeError("Function with name '" + name + "' already defined!");

        this.functions.put(name, value);
    }

    public Object getVariable(String name) {
        if(this.variables.containsKey(name))
            return this.variables.get(name);
        else if(this.enclosing != null)
            return this.enclosing.getVariable(name);

        throw new RuntimeError("Variable with name '" + name + "' not defined!");
    }

    public Object getFunction(String name) {
        if(this.functions.containsKey(name))
            return this.functions.get(name);
        else if(this.enclosing != null)
            return this.enclosing.getFunction(name);

        throw new RuntimeError("Function with name '" + name + "' not defined!");
    }

    public void assignVariable(Token token, Object value) {
        String name = token.value().toString();
        if(this.variables.containsKey(name)) {
            variables.put(name, value);
            return;
        }

        if(this.enclosing != null) {
            this.enclosing.assignVariable(token, value);
            return;
        }

        throw new RuntimeError(token, "Variable with name '" + name + "' not defined!");
    }
}
