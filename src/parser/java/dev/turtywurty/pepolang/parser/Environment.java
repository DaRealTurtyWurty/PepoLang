package dev.turtywurty.pepolang.parser;

import dev.turtywurty.pepolang.lexer.Token;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> values = new HashMap<>();

    public void define(String name, Object value) {
        if (this.values.containsKey(name))
            throw new RuntimeError("Variable with name '" + name + "' already defined!");

        this.values.put(name, value);
    }

    public Object get(String name) {
        if (!this.values.containsKey(name))
            throw new RuntimeError("Variable with name '" + name + "' not defined!");

        return this.values.get(name);
    }

    public void assign(Token token, Object value) {
        String name = token.value().toString();
        if(this.values.containsKey(name)) {
            values.put(name, value);
            return;
        }

        throw new RuntimeError(token, "Variable with name '" + name + "' not defined!");
    }
}
