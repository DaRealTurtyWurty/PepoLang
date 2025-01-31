package dev.turtywurty.pepolang.interpreter;

import dev.turtywurty.pepolang.lexer.Token;
import dev.turtywurty.pepolang.parser.Parameter;
import dev.turtywurty.pepolang.parser.Statement;

import java.util.List;
import java.util.Map;

public record PepoClass(String name, PepoClass superClass, Map<String, List<PepoFunction>> methods) {
    @Override
    public String toString() {
        return "PepoClass{" +
                "name='" + name + '\'' +
                ", superClass=" + superClass +
                ", methods=" + methods +
                '}';
    }

    public PepoFunction findMethod(String name) {
        if (this.methods.containsKey(name))
            return this.methods.get(name).getFirst();

        if (this.superClass != null)
            return this.superClass.findMethod(name);

        return null;
    }

    public static class PepoConstructor implements PepoCallable {
        private final Statement.ConstructorStatement declaration;
        private final Environment closure;

        public PepoConstructor(Statement.ConstructorStatement declaration, Environment closure) {
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
            int index = 0;
            for (Parameter parameter : this.declaration.getParameters()) {
                environment.defineVariable((String) parameter.name().value(), arguments.get(index++));
            }

            try {
                interpreter.executeBlock(this.declaration.getBody(), environment);
            } catch (Interpreter.Return ignored) {
                return this.closure.getVariableAt(0, "this");
            }

            return this.closure.getVariableAt(0, "this");
        }

        @Override
        public String toString() {
            return "<constructor " + this.declaration.getName().value() + ">";
        }
    }
}
