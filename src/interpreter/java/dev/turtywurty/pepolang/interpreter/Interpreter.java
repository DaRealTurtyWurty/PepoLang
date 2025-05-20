package dev.turtywurty.pepolang.interpreter;

import dev.turtywurty.pepolang.lexer.Token;
import dev.turtywurty.pepolang.lexer.TokenType;
import dev.turtywurty.pepolang.parser.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Interpreter implements ExpressionVisitor<Object>, StatementVisitor<Void> {
    private final Environment globals = new Environment();
    private final Map<Expression, Integer> localVariables = new HashMap<>();
    private final Map<Expression, Integer> localFunctions = new HashMap<>();
    private final Map<Expression, Integer> localClasses = new HashMap<>();

    private Environment environment = globals;

    public Interpreter() {
        this.globals.defineFunction("print", new PepoCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                Object value = arguments.getFirst();
                System.out.println(stringify(value));
                return null;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        this.globals.defineFunction("time", new PepoCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return System.currentTimeMillis();
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        this.globals.defineFunction("random", new PepoCallable() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                Object min = arguments.get(0);
                Object max = arguments.get(1);

                if (!(min instanceof Number minNum && max instanceof Number maxNum))
                    throw new RuntimeError(null, "Both arguments must be numbers!");

                return Math.random() * (maxNum.doubleValue() - minNum.doubleValue()) + minNum.doubleValue();
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        this.globals.defineFunction("sqrt", new PepoCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                Object value = arguments.getFirst();

                if (!(value instanceof Number num))
                    throw new RuntimeError(null, "Argument must be a number!");

                return Math.sqrt(num.doubleValue());
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        this.globals.defineFunction("input", new PepoCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                try {
                    String prompt = (String) arguments.getFirst();
                    System.out.print(prompt);
                    return new Scanner(System.in).nextLine();
                } catch (Exception e) {
                    throw new RuntimeError(null, "Error reading input: " + e.getMessage());
                }
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });
    }

    public void interpret(List<Statement> statements) {
        try {
            for (Statement statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            throw new RuntimeException("Something went wrong in the interpreter!", error);
        }
    }

    private void execute(Statement statement) {
        statement.accept(this);
    }

    private String stringify(Object object) {
        if (object == null) return "null";
        return object.toString();
    }

    @Override
    public Object visitAssign(Expression.Assign expression) {
        Object value = evaluate(expression.getValue());

        Integer distance = localVariables.get(expression);
        if (distance != null) {
            environment.assignVariableAt(distance, expression.getName(), value);
        } else {
            globals.assignVariable(expression.getName(), value);
        }

        return value;
    }

    @Override
    public Object visitBinary(Expression.Binary expression) {
        Object left = evaluate(expression.getLeft());
        Object right = evaluate(expression.getRight());

        switch (expression.getOperator().type()) {
            case SUB -> {
                checkNumberOperands(expression.getOperator(), left, right);
                if (left instanceof Number leftNum && right instanceof Number rightNum)
                    return leftNum.doubleValue() - rightNum.doubleValue();

                return null;
            }
            case ADD -> {
                if (left instanceof Number leftNum && right instanceof Number rightNum)
                    return leftNum.doubleValue() + rightNum.doubleValue();

                if (left instanceof String leftStr && right instanceof String rightStr)
                    return leftStr + rightStr;

                // If one of the operands is a string, convert the other to a string
                if (left instanceof String leftStr)
                    return leftStr + stringify(right);

                if (right instanceof String rightStr)
                    return stringify(left) + rightStr;

                throw new RuntimeError(expression.getOperator(), "Operands must be two numbers or two strings.");
            }
            case MUL -> {
                checkNumberOperands(expression.getOperator(), left, right);
                if (left instanceof Number leftNum && right instanceof Number rightNum)
                    return leftNum.doubleValue() * rightNum.doubleValue();

                return null;
            }
            case DIV -> {
                checkNumberOperands(expression.getOperator(), left, right);
                if (left instanceof Number leftNum && right instanceof Number rightNum) {
                    if (rightNum.doubleValue() == 0)
                        throw new RuntimeError(expression.getOperator(), "Cannot divide by zero!");

                    return leftNum.doubleValue() / rightNum.doubleValue();
                }

                return null;
            }
            case MOD -> {
                checkNumberOperands(expression.getOperator(), left, right);
                if (left instanceof Number leftNum && right instanceof Number rightNum)
                    return leftNum.doubleValue() % rightNum.doubleValue();

                return null;
            }
            case GT -> {
                checkNumberOperands(expression.getOperator(), left, right);
                if (left instanceof Number leftNum && right instanceof Number rightNum)
                    return leftNum.doubleValue() > rightNum.doubleValue();

                return null;
            }
            case GREATER_EQUAL -> {
                checkNumberOperands(expression.getOperator(), left, right);
                if (left instanceof Number leftNum && right instanceof Number rightNum)
                    return leftNum.doubleValue() >= rightNum.doubleValue();

                return null;
            }
            case LT -> {
                checkNumberOperands(expression.getOperator(), left, right);
                if (left instanceof Number leftNum && right instanceof Number rightNum)
                    return leftNum.doubleValue() < rightNum.doubleValue();

                return null;
            }
            case LESS_EQUAL -> {
                checkNumberOperands(expression.getOperator(), left, right);
                if (left instanceof Number leftNum && right instanceof Number rightNum)
                    return leftNum.doubleValue() <= rightNum.doubleValue();

                return null;
            }
            case EQUAL -> {
                if (left == null && right == null) return true;
                if (left == null) return false;

                if (left instanceof Number leftNum && right instanceof Number rightNum)
                    return Double.compare(leftNum.doubleValue(), rightNum.doubleValue()) == 0;

                return left.equals(right);
            }
            case NOT_EQUAL -> {
                if (left == null && right == null) return false;
                if (left == null) return true;

                if (left instanceof Number leftNum && right instanceof Number rightNum)
                    return Double.compare(leftNum.doubleValue(), rightNum.doubleValue()) != 0;

                return !left.equals(right);
            }
        }

        return null; // Unreachable
    }

    @Override
    public Object visitCall(Expression.Call expression) {
        Object callee = evaluate(expression.getCallee());

        List<Object> arguments = new ArrayList<>();
        for (Expression argument : expression.getArguments()) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof PepoCallable function))
            throw new RuntimeError(expression.getParen(), "Only functions can be called.");

        if (arguments.size() != function.arity())
            throw new RuntimeError(expression.getParen(), "Expected " + function.arity() + " arguments but got " + arguments.size() + ".");

        return function.call(this, arguments);
    }

    @Override
    public Object visitNew(Expression.New expression) {
        Object call = evaluate(expression.getCall());

        if (!(call instanceof Expression.Call callExpression))
            throw new RuntimeError(expression.getKeyword(), "Expected a call expression!");

        Object callee = evaluate(callExpression.getCallee());
        if (!(callee instanceof PepoClass clazz))
            throw new RuntimeError(expression.getKeyword(), "Expected a class!");

        List<Object> arguments = new ArrayList<>();
        for (Expression argument : callExpression.getArguments()) {
            arguments.add(evaluate(argument));
        }

//        PepoFunction constructor = clazz.findMethod("constructor");
//        if (constructor == null)
//            throw new RuntimeError(expression.getKeyword(), "Expected a constructor!");
//
//        return constructor.call(this, arguments);
        return null;
    }

    @Override
    public Object visitGet(Expression.Get expression) {
        Object object = evaluate(expression.getObject());
        if (object instanceof PepoInstance instance)
            return instance.get(expression.getName());

        throw new RuntimeError(expression.getName(), "Only instances have properties.");
    }

    @Override
    public Object visitSet(Expression.Set expression) {
        Object object = evaluate(expression.getObject());
        if (!(object instanceof PepoInstance instance))
            throw new RuntimeError(expression.getName(), "Only instances have fields.");

        Object value = evaluate(expression.getValue());
        instance.set(expression.getName(), value);
        return value;
    }

    @Override
    public Object visitThis(Expression.This expression) {
        return lookUpVariable(expression.getKeyword(), expression);
    }

    @Override
    public Object visitSuper(Expression.Super expression) {
        int distance = this.localVariables.get(expression);
        PepoClass superclass = this.environment.getClassAt(distance, "super");
        PepoInstance object = (PepoInstance) this.environment.getVariableAt(distance - 1, "this");

        PepoFunction method = superclass.findMethod((String) expression.getMethod().value());
        if (method == null)
            throw new RuntimeError(expression.getMethod(), "Undefined property '" + expression.getMethod().value() + "'.");

        return method.bind(object);
    }

    @Override
    public Object visitGrouping(Expression.Grouping expression) {
        return evaluate(expression.getExpression());
    }

    @Override
    public Object visitLiteral(Expression.Literal expression) {
        return expression.getValue();
    }

    @Override
    public Object visitLogical(Expression.Logical expression) {
        Object left = evaluate(expression.getLeft());

        if (expression.getOperator().type() == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expression.getRight());
    }

    @Override
    public Object visitUnary(Expression.Unary expression) {
        Object right = evaluate(expression.getRight());

        return switch (expression.getOperator().type()) {
            case SUB -> -(double) right;
            case ADD -> right;
            case NOT -> !isTruthy(right);
            default -> null;
        };
    }

    @Override
    public Object visitVariable(Expression.Variable expression) {
        return lookUpVariable(expression.getName(), expression);
    }

    private Object lookUpVariable(Token name, Expression expression) {
        Integer distance = localVariables.get(expression);
        if (distance != null) {
            return environment.getVariableAt(distance, (String) name.value());
        } else {
            return globals.getVariable((String) name.value());
        }
    }

    @Override
    public Object visitFunction(Expression.Function expression) {
        return lookUpFunction(expression.getName(), expression);
    }

    private Object lookUpFunction(Token name, Expression expression) {
        Integer distance = localFunctions.get(expression);
        if (distance != null) {
            return environment.getFunctionAt(distance, (String) name.value());
        } else {
            return globals.getFunction((String) name.value());
        }
    }

    @Override
    public Object visitExtends(Expression.Extends expression) {
        return lookUpClass(expression.getName(), expression);
    }

    private Object lookUpClass(Token name, Expression expression) {
        Integer distance = localClasses.get(expression);
        if (distance != null) {
            return environment.getClassAt(distance, (String) name.value());
        } else {
            return globals.getClass((String) name.value());
        }
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Number) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Number && right instanceof Number) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private boolean isTruthy(Object object) {
        if (object == null)
            return false;

        if (object instanceof Boolean bool)
            return bool;

        return true;
    }

    public Object evaluate(Expression expression) {
        return expression.accept(this);
    }

    @Override
    public Void visitAssignStatement(Statement.AssignStatement statement) {
        Object value = evaluate(statement.getValue());
        environment.assignVariable(statement.getName(), value);
        return null;
    }

    @Override
    public Void visitBlockStatement(Statement.BlockStatement statement) {
        executeBlock(statement.getStatements(), new Environment(environment));
        return null;
    }

    @Override
    public Void visitBreakStatement(Statement.BreakStatement statement) {
        throw new Break();
    }

    @Override
    public Void visitContinueStatement(Statement.ContinueStatement statement) {
        throw new Continue();
    }

    public void executeBlock(List<Statement> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Statement statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitExpressionStatement(Statement.ExpressionStatement statement) {
        evaluate(statement.getExpression());
        return null;
    }

    @Override
    public Void visitFunctionStatement(Statement.FunctionStatement statement) {
        PepoFunction function = new PepoFunction(statement, this.environment);
        this.environment.defineFunction(statement.getName().value().toString(), function);
        return null;
    }

    @Override
    public Void visitIfStatement(Statement.IfStatement statement) {
        if (isTruthy(evaluate(statement.getCondition()))) {
            execute(statement.getThenBranch());
        } else if (statement.getElseBranch() != null) {
            execute(statement.getElseBranch());
        }

        return null;
    }

    @Override
    public Void visitReturnStatement(Statement.ReturnStatement statement) {
        Object value = null;
        if (statement.getValue() != null) {
            value = evaluate(statement.getValue());
        }

        throw new Return(value);
    }

    @Override
    public Void visitVariableStatement(Statement.VariableStatement statement) {
        Object value = null;
        if (statement.getInitializer() != null) {
            value = evaluate(statement.getInitializer());
        }

        environment.defineVariable((String) statement.getName().value(), value); // TODO: Handle types
        return null;
    }

    @Override
    public Void visitClassStatement(Statement.ClassStatement statement) {
        String className = (String) statement.getName().value();

        PepoClass superclass = null;
        @Nullable Expression.Extends superclassExpr = statement.getSuperclass();
        if (superclassExpr != null) {
            Object superclassName = evaluate(superclassExpr);
            if (!(superclassName instanceof PepoClass superClass))
                throw new RuntimeError(statement.getName(), "Superclass must be a class.");

            superclass = superClass;
        }

        this.environment.defineClass(className, null);

        if (superclass != null) {
            this.environment = new Environment(this.environment);
            this.environment.defineVariable("super", superclass);
        }

        Map<String, List<PepoFunction>> methods = new HashMap<>();
        for (Statement.FunctionStatement method : statement.getMethods()) {
            methods.computeIfAbsent(method.getName().value().toString(), k -> new ArrayList<>())
                    .add(new PepoFunction(method, this.environment));
        }

        PepoClass clazz = new PepoClass(className, superclass, methods);
        if (superclass != null)
            this.environment = this.environment.getEnclosing();

        this.environment.defineClass(className, clazz);
        this.environment.defineVariable("this", clazz);
        return null;
    }

    @Override
    public Void visitConstructorStatement(Statement.ConstructorStatement statement) {
        String className = (String) statement.getName().value();
        this.environment.defineFunction(className, new PepoClass.PepoConstructor(statement, this.environment));
        return null;
    }

    @Override
    public Void visitWhileStatement(Statement.WhileStatement statement) {
        while (isTruthy(evaluate(statement.getCondition()))) {
            try {
                execute(statement.getBody());
            } catch (Break ignored) {
                break;
            } catch (Continue ignored) {
            }
        }

        return null;
    }

    public Environment getGlobals() {
        return this.globals;
    }

    protected void resolve(Expression expression, int depth) {
        if(expression instanceof Expression.Variable variable) {
            this.localVariables.put(variable, depth);
        } else if(expression instanceof Expression.Assign assign) {
            this.localVariables.put(assign, depth);
        } else if(expression instanceof Expression.Function function) {
            this.localFunctions.put(function, depth);
        } else if(expression instanceof Expression.Extends clazz) {
            this.localClasses.put(clazz, depth);
        }
    }

    public static class RuntimeError extends RuntimeException {
        final Token token;

        RuntimeError(Token token, String message) {
            super(message);
            this.token = token;
        }

        RuntimeError(String message) {
            this(null, message);
        }
    }

    public static class Break extends RuntimeException {
        Break() {
            super(null, null, false, false);
        }
    }

    public static class Continue extends RuntimeException {
        Continue() {
            super(null, null, false, false);
        }
    }

    public static class Return extends RuntimeException {
        public final Object value;

        Return(Object value) {
            super(null, null, false, false);
            this.value = value;
        }
    }
}
