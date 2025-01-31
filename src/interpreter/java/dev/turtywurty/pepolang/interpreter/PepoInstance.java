package dev.turtywurty.pepolang.interpreter;

import dev.turtywurty.pepolang.lexer.Token;

import java.util.HashMap;
import java.util.Map;

public class PepoInstance {
    private final PepoClass clazz;
    private final Map<String, Object> fields = new HashMap<>();

    public PepoInstance(PepoClass clazz) {
        this.clazz = clazz;
    }

    @Override
    public String toString() {
        return this.clazz.name() + " instance";
    }

    public Object get(Token name) {
        if (this.fields.containsKey((String) name.value())) {
            return this.fields.get((String) name.value());
        }

        PepoFunction method = this.clazz.findMethod((String) name.value());
        if (method != null)
            return method.bind(this);

        throw new Interpreter.RuntimeError(name, "Undefined property '" + name.value() + "'.");
    }

    public void set(Token name, Object value) {
        this.fields.put((String) name.value(), value);
    }
}
