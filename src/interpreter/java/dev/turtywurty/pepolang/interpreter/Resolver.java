package dev.turtywurty.pepolang.interpreter;

import dev.turtywurty.pepolang.lexer.Token;
import dev.turtywurty.pepolang.parser.*;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

public class Resolver implements ExpressionVisitor<Void>, StatementVisitor<Void> {
    private final Interpreter interpreter;
    private final Stack<HashMap<String, Boolean>> variableScopes = new Stack<>();
    private final Stack<HashMap<String, Boolean>> functionScopes = new Stack<>();
    private final Stack<HashMap<String, Boolean>> classScopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;
    private ClassType currentClass = ClassType.NONE;

    public Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    @Override
    public Void visitBlockStatement(Statement.BlockStatement statement) {
        beginScope();
        resolve(statement.getStatements());
        endScope();
        return null;
    }

    @Override
    public Void visitVariableStatement(Statement.VariableStatement statement) {
        declareVariable(statement.getName());

        if (statement.getInitializer() != null) {
            resolve(statement.getInitializer());
        }

        defineVariable(statement.getName());
        return null;
    }

    @Override
    public Void visitVariable(Expression.Variable expression) {
        if (!this.variableScopes.isEmpty()) {
            HashMap<String, Boolean> scope = this.variableScopes.peek();
            if (scope.get((String) expression.getName().value()) == Boolean.FALSE) {
                throw new Interpreter.RuntimeError(expression.getName(), "Cannot read local variable in its own initializer.");
            }
        }

        resolveLocalVariable(expression, expression.getName());
        return null;
    }

    @Override
    public Void visitFunction(Expression.Function expression) {
        if(!this.functionScopes.isEmpty()) {
            HashMap<String, Boolean> scope = this.functionScopes.peek();
            if (scope.get((String) expression.getName().value()) == Boolean.FALSE) {
                throw new Interpreter.RuntimeError(expression.getName(), "Cannot read local function in its own initializer.");
            }
        }

        resolveLocalFunction(expression);
        return null;
    }

    @Override
    public Void visitAssign(Expression.Assign expression) {
        resolve(expression.getValue());
        resolveLocalVariable(expression, expression.getName());
        return null;
    }

    @Override
    public Void visitFunctionStatement(Statement.FunctionStatement statement) {
        declareFunction(statement.getName());
        defineFunction(statement.getName());

        resolveFunction(statement, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitExpressionStatement(Statement.ExpressionStatement statement) {
        resolve(statement.getExpression());
        return null;
    }

    @Override
    public Void visitIfStatement(Statement.IfStatement statement) {
        resolve(statement.getCondition());
        resolve(statement.getThenBranch());

        if (statement.getElseBranch() != null) {
            resolve(statement.getElseBranch());
        }

        return null;
    }

    @Override
    public Void visitReturnStatement(Statement.ReturnStatement statement) {
        if (this.currentFunction == FunctionType.NONE)
            throw new Interpreter.RuntimeError(statement.getKeyword(), "Cannot return from top-level code.");

        if (statement.getValue() != null) {
            if (this.currentFunction == FunctionType.CONSTRUCTOR)
                throw new Interpreter.RuntimeError(statement.getKeyword(), "Cannot return a value from a constructor.");

            resolve(statement.getValue());
        }

        return null;
    }

    @Override
    public Void visitWhileStatement(Statement.WhileStatement statement) {
        resolve(statement.getCondition());
        resolve(statement.getBody());
        return null;
    }

    @Override
    public Void visitBinary(Expression.Binary expression) {
        resolve(expression.getLeft());
        resolve(expression.getRight());
        return null;
    }

    @Override
    public Void visitCall(Expression.Call expression) {
        resolve(expression.getCallee());

        for (Expression argument : expression.getArguments()) {
            resolve(argument);
        }

        return null;
    }

    @Override
    public Void visitGrouping(Expression.Grouping expression) {
        resolve(expression.getExpression());
        return null;
    }

    @Override
    public Void visitLiteral(Expression.Literal expression) {
        return null;
    }

    @Override
    public Void visitLogical(Expression.Logical expression) {
        resolve(expression.getLeft());
        resolve(expression.getRight());
        return null;
    }

    @Override
    public Void visitUnary(Expression.Unary expression) {
        resolve(expression.getRight());
        return null;
    }

    @Override
    public Void visitBreakStatement(Statement.BreakStatement statement) {
        if (this.currentFunction == FunctionType.NONE)
            throw new Interpreter.RuntimeError("Cannot break outside of a loop.");

        return null;
    }

    @Override
    public Void visitContinueStatement(Statement.ContinueStatement statement) {
        if (this.currentFunction == FunctionType.NONE)
            throw new Interpreter.RuntimeError("Cannot continue outside of a loop.");

        return null;
    }

    @Override
    public Void visitGet(Expression.Get expression) {
        resolve(expression.getObject());
        return null;
    }

    @Override
    public Void visitClassStatement(Statement.ClassStatement statement) {
        ClassType enclosingClass = this.currentClass;
        this.currentClass = ClassType.CLASS;

        declareClass(statement.getName());
        defineClass(statement.getName());

        if(statement.getSuperclass() != null) {
            if(Objects.equals(statement.getName().value(), statement.getSuperclass().getName().value())) {
                throw new Interpreter.RuntimeError(statement.getSuperclass().getName(), "A class cannot inherit from itself.");
            }

            resolve(statement.getSuperclass());
            this.currentClass = ClassType.SUBCLASS;

            beginScope();
            this.variableScopes.peek().put("super", true);
        }

        beginScope();
        this.variableScopes.peek().put("this", true);

        for (Statement.ConstructorStatement constructor : statement.getConstructors()) {
            resolveConstructor(constructor);
        }

        for (Statement.FunctionStatement method : statement.getMethods()) {
            resolveFunction(method, FunctionType.METHOD);
        }

        endScope();

        if(statement.getSuperclass() != null) {
            endScope();
        }

        this.currentClass = enclosingClass;

        return null;
    }

    @Override
    public Void visitSet(Expression.Set expression) {
        resolve(expression.getValue());
        resolve(expression.getObject());
        return null;
    }

    @Override
    public Void visitThis(Expression.This expression) {
        if (this.currentFunction == FunctionType.NONE || this.currentClass == ClassType.NONE)
            throw new Interpreter.RuntimeError(expression.getKeyword(), "Cannot use 'this' outside of a class method.");

        resolveLocalVariable(expression, expression.getKeyword());
        return null;
    }

    @Override
    public Void visitAssignStatement(Statement.AssignStatement statement) {
        resolve(statement.getValue());
        resolveLocalVariable(statement.getValue(), statement.getName());
        return null;
    }

    @Override
    public Void visitSuper(Expression.Super expression) {
        if (this.currentClass == ClassType.NONE)
            throw new Interpreter.RuntimeError(expression.getKeyword(), "Cannot use 'super' outside of a class.");

        if (this.currentClass != ClassType.SUBCLASS)
            throw new Interpreter.RuntimeError(expression.getKeyword(), "Cannot use 'super' in a class with no superclass.");

        resolveLocalVariable(expression, expression.getKeyword());
        return null;
    }

    @Override
    public Void visitExtends(Expression.Extends expression) {
        resolve(expression);
        return null;
    }

    @Override
    public Void visitConstructorStatement(Statement.ConstructorStatement statement) {
        resolveConstructor(statement);
        return null;
    }

    @Override
    public Void visitNew(Expression.New expression) {
        resolve(expression.getCall());
        return null;
    }

    public void resolve(List<Statement> statements) {
        for (Statement statement : statements) {
            resolve(statement);
        }
    }

    protected void resolve(Statement statement) {
        statement.accept(this);
    }

    protected void resolve(Expression expression) {
        expression.accept(this);
    }

    private void beginScope() {
        this.functionScopes.push(new HashMap<>());
        this.variableScopes.push(new HashMap<>());
        this.classScopes.push(new HashMap<>());
    }

    private void endScope() {
        this.variableScopes.pop();
        this.functionScopes.pop();
        this.classScopes.pop();
    }

    private void declareVariable(Token name) {
        declareVariable((String) name.value());
    }

    private void declareVariable(String name) {
        if (this.variableScopes.isEmpty()) return;

        HashMap<String, Boolean> scope = this.variableScopes.peek();
        if(scope.containsKey(name)) {
            throw new Interpreter.RuntimeError("Variable with name '" + name + "' already declared in this scope.");
        }

        scope.put(name, false);
    }

    private void declareFunction(Token name) {
        declareFunction((String) name.value());
    }

    private void declareFunction(String name) {
        if (this.functionScopes.isEmpty()) return;

        HashMap<String, Boolean> scope = this.functionScopes.peek();
        if(scope.containsKey(name)) {
            throw new Interpreter.RuntimeError("Function with name '" + name + "' already declared in this scope.");
        }

        scope.put(name, false);
    }

    private void declareClass(Token name) {
        declareClass((String) name.value());
    }

    private void declareClass(String name) {
        if (this.classScopes.isEmpty()) return;

        HashMap<String, Boolean> scope = this.classScopes.peek();
        if(scope.containsKey(name)) {
            throw new Interpreter.RuntimeError("Class with name '" + name + "' already declared in this scope.");
        }

        scope.put(name, false);
    }

    private void defineVariable(Token name) {
        defineVariable((String) name.value());
    }

    private void defineVariable(String name) {
        if (this.variableScopes.isEmpty()) return;

        HashMap<String, Boolean> scope = this.variableScopes.peek();
        scope.put(name, true);
    }

    private void defineFunction(Token name) {
        defineFunction((String) name.value());
    }

    private void defineFunction(String name) {
        if (this.functionScopes.isEmpty()) return;

        HashMap<String, Boolean> scope = this.functionScopes.peek();
        scope.put(name, true);
    }

    private void defineClass(Token name) {
        defineClass((String) name.value());
    }

    private void defineClass(String name) {
        if (this.classScopes.isEmpty()) return;

        HashMap<String, Boolean> scope = this.classScopes.peek();
        scope.put(name, true);
    }

    private void resolveLocalVariable(Expression expression, Token name) {
        for (int i = this.variableScopes.size() - 1; i >= 0; i--) {
            if (this.variableScopes.get(i).containsKey((String) name.value())) {
                this.interpreter.resolve(expression, this.variableScopes.size() - 1 - i);
                return;
            }
        }
    }

    private void resolveLocalFunction(Expression.Function expression) {
        for (int i = this.functionScopes.size() - 1; i >= 0; i--) {
            if (this.functionScopes.get(i).containsKey((String) expression.getName().value())) {
                this.interpreter.resolve(expression, this.functionScopes.size() - 1 - i);
                return;
            }
        }
    }

    private void resolveConstructor(Statement.ConstructorStatement statement) {
        FunctionType enclosingFunction = this.currentFunction;
        this.currentFunction = FunctionType.CONSTRUCTOR;

        beginScope();

        for (Parameter param : statement.getParameters()) {
            declareVariable(param.name());
            defineVariable(param.name());
        }

        resolve(statement.getBody());
        endScope();

        this.currentFunction = enclosingFunction;
    }

    private void resolveFunction(Statement.FunctionStatement statement, FunctionType type) {
        FunctionType enclosingFunction = this.currentFunction;
        this.currentFunction = type;

        beginScope();

        for (Parameter param : statement.getParameters()) {
            declareVariable(param.name());
            defineVariable(param.name());
        }

        resolve(statement.getBody());
        endScope();

        this.currentFunction = enclosingFunction;
    }
}
