package dev.turtywurty.pepolang.interpreter;

import dev.turtywurty.pepolang.lexer.Token;
import dev.turtywurty.pepolang.parser.RuntimeError;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Environment enclosing;
    private final Map<String, PepoClass> classes = new HashMap<>();
    private final Map<String, Object> variables = new HashMap<>();
    private final Map<String, PepoCallable> functions = new HashMap<>();

    // local environment
    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }
    
    // global environment
    public Environment() {
        this(null);
    }

    public Environment getEnclosing() {
        return this.enclosing;
    }

    public void defineClass(String name, PepoClass value) {
        if (this.classes.containsKey(name) && classes.get(name) != null && value != null)
            throw new RuntimeError("Class with name '" + name + "' already defined!");

        this.classes.put(name, value);
    }

    public void defineVariable(String name, Object value) {
        if (this.variables.containsKey(name) && variables.get(name) != null && value != null)
            throw new RuntimeError("Variable with name '" + name + "' already defined!");

        this.variables.put(name, value);
    }

    public void defineFunction(String name, PepoCallable value) {
        if (this.functions.containsKey(name) && functions.get(name) != null && value != null)
            throw new RuntimeError("Function with name '" + name + "' already defined!");

        this.functions.put(name, value);
    }

    public Object getVariableAt(int distance, String name) {
        return ancestor(distance).getVariable(name);
    }

    public Object getFunctionAt(int distance, String name) {
        return ancestor(distance).getFunction(name);
    }

    public PepoClass getClassAt(int distance, String name) {
        return ancestor(distance).getClass(name);
    }

    public void assignVariableAt(int distance, Token token, Object value) {
        ancestor(distance).assignVariable(token, value);
    }

    private Environment ancestor(int distance) {
        Environment environment = this;
        for (int i = 0; i < distance; i++) {
            environment = environment.getEnclosing();
        }

        return environment;
    }

    public PepoClass getClass(String name) {
        if(this.classes.containsKey(name))
            return this.classes.get(name);
        else if(this.enclosing != null)
            return this.enclosing.getClass(name);

        throw new RuntimeError("Class with name '" + name + "' not defined!");
    }

    public Object getVariable(String name) {
        if(this.variables.containsKey(name))
            return this.variables.get(name);
        else if(this.enclosing != null)
            return this.enclosing.getVariable(name);

        throw new RuntimeError("Variable with name '" + name + "' not defined!");
    }

    public PepoCallable getFunction(String name) {
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
