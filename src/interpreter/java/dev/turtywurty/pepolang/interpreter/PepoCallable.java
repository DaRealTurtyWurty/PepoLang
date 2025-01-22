package dev.turtywurty.pepolang.interpreter;

import java.util.List;

public interface PepoCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
