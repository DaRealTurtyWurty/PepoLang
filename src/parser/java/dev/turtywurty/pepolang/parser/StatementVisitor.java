// This file is automatically generated. Do not modify.
package dev.turtywurty.pepolang.parser;

import dev.turtywurty.pepolang.JavaGenerated;
import dev.turtywurty.pepolang.parser.Statement.AssignStatement;
import dev.turtywurty.pepolang.parser.Statement.BlockStatement;
import dev.turtywurty.pepolang.parser.Statement.BreakStatement;
import dev.turtywurty.pepolang.parser.Statement.ContinueStatement;
import dev.turtywurty.pepolang.parser.Statement.ExpressionStatement;
import dev.turtywurty.pepolang.parser.Statement.FunctionStatement;
import dev.turtywurty.pepolang.parser.Statement.IfStatement;
import dev.turtywurty.pepolang.parser.Statement.ReturnStatement;
import dev.turtywurty.pepolang.parser.Statement.VariableStatement;
import dev.turtywurty.pepolang.parser.Statement.WhileStatement;

@JavaGenerated
public interface StatementVisitor<R> {
    R visitAssignStatement(AssignStatement statement);

    R visitBlockStatement(BlockStatement statement);

    R visitBreakStatement(BreakStatement statement);

    R visitContinueStatement(ContinueStatement statement);

    R visitExpressionStatement(ExpressionStatement statement);

    R visitFunctionStatement(FunctionStatement statement);

    R visitIfStatement(IfStatement statement);

    R visitReturnStatement(ReturnStatement statement);

    R visitVariableStatement(VariableStatement statement);

    R visitWhileStatement(WhileStatement statement);
}
