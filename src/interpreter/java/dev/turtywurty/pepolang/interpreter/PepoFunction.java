package dev.turtywurty.pepolang.interpreter;

import dev.turtywurty.pepolang.lexer.Token;
import dev.turtywurty.pepolang.parser.Statement;

import java.util.List;
import java.util.Map;

public class PepoFunction implements PepoCallable {
    private final Statement.FunctionStatement declaration;

    public PepoFunction(Statement.FunctionStatement declaration) {
        this.declaration = declaration;
    }

    @Override
    public int arity() {
        return this.declaration.getParameters().size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(interpreter.getGlobals());
        int index = 0;
        for (Map.Entry<Token, Token> entry : this.declaration.getParameters().entrySet()) {
            environment.defineVariable(entry.getKey().value().toString(), arguments.get(index++));
        }

        try {
            interpreter.executeBlock(this.declaration.getBody(), environment);
        } catch (Interpreter.Return returnValue) {
            return returnValue.value;
        }

        return null;
    }

    @Override
    public String toString() {
        return "<fn " + this.declaration.getName().value() + ">";
    }
}
