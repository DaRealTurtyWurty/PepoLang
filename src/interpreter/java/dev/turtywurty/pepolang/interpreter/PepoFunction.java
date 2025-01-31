package dev.turtywurty.pepolang.interpreter;

import dev.turtywurty.pepolang.parser.Parameter;
import dev.turtywurty.pepolang.parser.Statement;

import java.util.List;

public class PepoFunction implements PepoCallable {
    private final Statement.FunctionStatement declaration;
    private final Environment closure;

    public PepoFunction(Statement.FunctionStatement declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
    }

    @Override
    public int arity() {
        return this.declaration.getParameters().size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(this.closure);
        List<Parameter> parameters = this.declaration.getParameters();
        for (int index = 0; index < parameters.size(); index++) {
            environment.defineVariable((String) parameters.get(index).name().value(), arguments.get(index));
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

    public PepoFunction bind(PepoInstance instance) {
        Environment environment = new Environment(this.closure);
        environment.defineVariable("this", instance);
        return new PepoFunction(this.declaration, environment);
    }
}
