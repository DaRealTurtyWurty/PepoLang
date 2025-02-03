package dev.turtywurty.pepolang.semanticAnalysis;

import dev.turtywurty.pepolang.lexer.Token;
import dev.turtywurty.pepolang.lexer.TokenType;
import dev.turtywurty.pepolang.parser.*;
import dev.turtywurty.pepolang.semanticAnalysis.symbol.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SemanticAnalyzer implements StatementVisitor<Symbol>, ExpressionVisitor<Symbol> {
    private final List<Statement> statements;
    private final SymbolTable symbolTable;

    private final List<SemanticException> errors = new ArrayList<>();

    public SemanticAnalyzer(List<Statement> statements) {
        this.statements = statements;
        this.symbolTable = new SymbolTable();

        for (Statement statement : statements) {
            statement.accept(this);
        }
    }

    @Override
    public Symbol visitBlockStatement(Statement.BlockStatement statement) {
        this.symbolTable.enterScope();
        for (Statement stmt : statement.getStatements()) {
            stmt.accept(this);
        }
        this.symbolTable.exitScope();
        return null;
    }

    @Override
    public Symbol visitFunctionStatement(Statement.FunctionStatement statement) {
        return handleMethod(this, statement);
    }

    @Override
    public Symbol visitVariableStatement(Statement.VariableStatement statement) {
        Token name = statement.getName();
        Token type = statement.getType();
        Expression initializer = statement.getInitializer();

        if (type.type() == TokenType.KEYWORD_VOID) {
            throw error(type, "Cannot use 'void' as a variable type!");
        }

        String nameValue = (String) name.value();

        if (this.symbolTable.containsSymbol(nameValue, SymbolType.VARIABLE)) {
            throw error(name, "Variable with name '" + name.value() + "' already exists in this scope!");
        }

        var symbol = new VariableSymbol(nameValue, type);

        this.symbolTable.addSymbol(symbol);

        if (initializer != null) {
            initializer.accept(this);
        }

        return null;
    }

    @Override
    public Symbol visitExpressionStatement(Statement.ExpressionStatement statement) {
        statement.getExpression().accept(this);
        return null;
    }

    @Override
    public Symbol visitIfStatement(Statement.IfStatement statement) {
        Expression condition = statement.getCondition();
        Statement thenBranch = statement.getThenBranch();
        Statement elseBranch = statement.getElseBranch();

        condition.accept(this);
        thenBranch.accept(this);
        if (elseBranch != null) {
            elseBranch.accept(this);
        }

        return null;
    }

    @Override
    public Symbol visitAssignStatement(Statement.AssignStatement statement) {
        Token name = statement.getName();
        Expression value = statement.getValue();

        String nameValue = (String) name.value();

        if (!this.symbolTable.containsSymbol(nameValue, SymbolType.VARIABLE)) {
            throw error(name, "Variable with name '" + name.value() + "' does not exist in this scope!");
        }

        value.accept(this);
        return null;
    }

    @Override
    public Symbol visitWhileStatement(Statement.WhileStatement statement) {
        Expression condition = statement.getCondition();
        Statement body = statement.getBody();

        condition.accept(this);
        body.accept(this);
        return null;
    }

    @Override
    public Symbol visitBreakStatement(Statement.BreakStatement statement) {
        return null;
    }

    @Override
    public Symbol visitContinueStatement(Statement.ContinueStatement statement) {
        return null;
    }

    @Override
    public Symbol visitReturnStatement(Statement.ReturnStatement statement) {
        Expression value = statement.getValue();

        if (value != null) {
            return value.accept(this);
        }

        return null;
    }

    @Override
    public Symbol visitClassStatement(Statement.ClassStatement statement) {
        Token name = statement.getName();

        List<Statement.VariableStatement> staticFields = statement.getStaticFields();
        List<Statement.FunctionStatement> staticMethods = statement.getStaticMethods();
        List<Statement.VariableStatement> fields = statement.getFields();
        List<Statement.FunctionStatement> methods = statement.getMethods();
        List<Statement.ConstructorStatement> constructors = statement.getConstructors();

        String nameValue = (String) name.value();
        if (this.symbolTable.containsSymbol(nameValue, SymbolType.CLASS)) {
            throw error(name, "Class with name '" + name.value() + "' already exists in this scope!");
        }

        this.symbolTable.enterScope();

        List<VariableSymbol> staticFieldsSymbols = new ArrayList<>();
        for (Statement.VariableStatement staticField : staticFields) {
            staticFieldsSymbols.add(handleField(this, staticField));
        }

        List<MethodSymbol> staticMethodsSymbols = new ArrayList<>();
        for (Statement.FunctionStatement staticMethod : staticMethods) {
            staticMethodsSymbols.add(handleMethod(this, staticMethod));
        }

        List<VariableSymbol> fieldsSymbols = new ArrayList<>();
        for (Statement.VariableStatement field : fields) {
            fieldsSymbols.add(handleField(this, field));
        }

        List<MethodSymbol> methodsSymbols = new ArrayList<>();
        for (Statement.FunctionStatement method : methods) {
            methodsSymbols.add(handleMethod(this, method));
        }

        List<MethodSymbol> constructorsSymbols = new ArrayList<>();
        for (Statement.ConstructorStatement constructor : constructors) {
            constructorsSymbols.add(handleConstructor(this, constructor));
        }

        this.symbolTable.exitScope();

        var symbol = new ClassSymbol(nameValue, staticFieldsSymbols, staticMethodsSymbols, fieldsSymbols, methodsSymbols, constructorsSymbols);
        this.symbolTable.addSymbol(symbol);

        return symbol;
    }

    @Override
    public Symbol visitConstructorStatement(Statement.ConstructorStatement statement) {
        return handleConstructor(this, statement);
    }

    private static VariableSymbol handleField(SemanticAnalyzer semanticAnalyzer, Statement.VariableStatement field) {
        Token fieldName = field.getName();
        Token fieldType = field.getType();
        Expression initializer = field.getInitializer();

        if (fieldType.type() == TokenType.KEYWORD_VOID) {
            throw semanticAnalyzer.error(fieldType, "Cannot use 'void' as a field type!");
        }

        String fieldNameValue = (String) fieldName.value();
        if (semanticAnalyzer.symbolTable.containsSymbol(fieldNameValue, SymbolType.FIELD)) {
            throw semanticAnalyzer.error(fieldName, "Field with name '" + fieldName.value() + "' already exists in this scope!");
        }

        VariableSymbol symbol = new VariableSymbol(fieldNameValue, fieldType);
        semanticAnalyzer.symbolTable.addSymbol(symbol);

        if (initializer != null) {
            initializer.accept(semanticAnalyzer);
        }

        return symbol;
    }

    private static MethodSymbol handleMethod(SemanticAnalyzer semanticAnalyzer, Statement.FunctionStatement method) {
        Token methodName = method.getName();
        Token returnType = method.getReturnType();
        List<Parameter> parameters = method.getParameters();
        List<Statement> body = method.getBody();

        List<VariableSymbol> parametersSymbols = new ArrayList<>();
        for (Parameter parameter : parameters) {
            Token paramName = parameter.name();
            Token paramType = parameter.type();

            if (paramType.type() == TokenType.KEYWORD_VOID) {
                throw semanticAnalyzer.error(paramType, "Cannot use 'void' as a parameter type!");
            }

            String paramNameValue = (String) paramName.value();

            if (semanticAnalyzer.symbolTable.containsSymbol(paramNameValue, SymbolType.VARIABLE)) {
                throw semanticAnalyzer.error(paramName, "Variable with name '" + paramName.value() + "' already exists in this scope!");
            }

            var symbol = new VariableSymbol(paramNameValue, paramType);
            parametersSymbols.add(symbol);
        }

        String nameValue = (String) methodName.value();
        var symbol = new MethodSymbol(nameValue, returnType, parametersSymbols);

        List<MethodSymbol> methods = semanticAnalyzer.symbolTable.getMethods(nameValue);
        if (methods != null) {
            boolean methodAlreadyOverloaded = false;
            for (MethodSymbol otherMethod : methods) {
                if (otherMethod.matches(symbol.getParameters())) {
                    semanticAnalyzer.error(methodName, "Method with name '" + methodName.value() + "' already exists in this scope!");
                    methodAlreadyOverloaded = true;
                }
            }

            if (methodAlreadyOverloaded)
                throw new SemanticException(methodName, "Method with name '" + methodName.value() + "' already exists in this scope!");
        }

        semanticAnalyzer.symbolTable.addSymbol(symbol);

        semanticAnalyzer.symbolTable.enterScope();
        for (VariableSymbol parameter : parametersSymbols) {
            semanticAnalyzer.symbolTable.addSymbol(parameter);
        }

        for (Statement stmt : body) {
            stmt.accept(semanticAnalyzer);
        }

        semanticAnalyzer.symbolTable.exitScope();
        return symbol;
    }

    private static MethodSymbol handleConstructor(SemanticAnalyzer semanticAnalyzer, Statement.ConstructorStatement constructor) {
        Token constructorName = constructor.getName();
        List<Parameter> parameters = constructor.getParameters();

        ClassSymbol classSymbol = semanticAnalyzer.symbolTable.getClass((String) constructorName.value());
        if (classSymbol == null) {
            throw semanticAnalyzer.error(constructorName, "Class with name '" + constructorName.value() + "' does not exist in this scope!");
        }

        List<VariableSymbol> paramSymbols = new ArrayList<>();
        for (Parameter param : parameters) {
            Token paramName = param.name();
            Token paramType = param.type();
            VariableSymbol paramSymbol = new VariableSymbol((String) paramName.value(), paramType);
            paramSymbols.add(paramSymbol);
        }

        List<MethodSymbol> constructors = semanticAnalyzer.symbolTable.getSymbols(
                (String) constructorName.value(),
                symbol -> {
                    if (symbol.getSymbolType() != SymbolType.METHOD)
                        return false;

                    if (symbol instanceof MethodSymbol methSymbol &&
                            methSymbol.getReturnType().isLeft() &&
                            methSymbol.getReturnType().getLeft() == PrimitiveType.VOID)
                        return methSymbol.matches(paramSymbols);

                    return false;
                }).stream().map(MethodSymbol.class::cast).toList();

        if (!constructors.isEmpty()) {
            throw semanticAnalyzer.error(constructorName, "Constructor " + getMethodSignature(constructors.getFirst()) + " already exists in this scope!");
        }

        MethodSymbol constructorSymbol = new MethodSymbol((String) constructorName.value(), PrimitiveType.VOID, paramSymbols);
        semanticAnalyzer.symbolTable.addSymbol(constructorSymbol);

        semanticAnalyzer.symbolTable.enterScope();
        for (VariableSymbol paramSymbol : paramSymbols) {
            semanticAnalyzer.symbolTable.addSymbol(paramSymbol);
        }

        for (Statement stmt : constructor.getBody()) {
            stmt.accept(semanticAnalyzer);
        }

        semanticAnalyzer.symbolTable.exitScope();
        return constructorSymbol;
    }

    private static String getMethodSignature(MethodSymbol method) {
        StringBuilder signature = new StringBuilder();
        signature.append((String) method.getReturnType().map(
                primitiveType -> primitiveType.name().toLowerCase(Locale.ROOT),
                classType -> classType));
        signature.append(" ");
        signature.append(method.getName());
        signature.append("(");
        for (int i = 0; i < method.getParameters().size(); i++) {
            VariableSymbol parameter = method.getParameters().get(i);
            signature.append((String) parameter.getReturnType().map(
                    primitiveType -> primitiveType.name().toLowerCase(Locale.ROOT),
                    classType -> classType));
            if (i < method.getParameters().size() - 1) {
                signature.append(", ");
            }
        }
        signature.append(")");
        return signature.toString();
    }

    private SemanticException error(Token token, String message) {
        var exception = new SemanticException(token, message);
        this.errors.add(exception);
        return exception;
    }

    @Override
    public Symbol visitAssign(Expression.Assign expression) {
        Token name = expression.getName();
        Expression value = expression.getValue();

        String nameValue = (String) name.value();
        if (!this.symbolTable.containsSymbol(nameValue, SymbolType.VARIABLE)) {
            throw error(name, "Variable with name '" + name.value() + "' does not exist in this scope!");
        }

        value.accept(this);
        return this.symbolTable.getVariable(nameValue);
    }

    @Override
    public Symbol visitBinary(Expression.Binary expression) {
        Token operator = expression.getOperator();
        Expression left = expression.getLeft();
        Expression right = expression.getRight();

        Symbol leftSymbol = left.accept(this);
        Symbol rightSymbol = right.accept(this);

        if (leftSymbol == null || rightSymbol == null) {
            throw new SemanticException(operator, "Invalid binary expression: one or more operands are null.");
        }

        if(!(leftSymbol instanceof ReturnTypeSymbol leftReturnTypeSymbol) || !(rightSymbol instanceof ReturnTypeSymbol rightReturnTypeSymbol)) {
            throw new SemanticException(operator, "Invalid binary expression: one or more operands are not of type ReturnTypeSymbol.");
        }

        Either<PrimitiveType, String> leftType = leftReturnTypeSymbol.getReturnType();
        Either<PrimitiveType, String> rightType = rightReturnTypeSymbol.getReturnType();

        Either<PrimitiveType, String> type = checkBinaryExpression(operator, leftType, rightType);
        return null; // TODO: i think this should return some special symbol
    }

    @Override
    public Symbol visitCall(Expression.Call expression) {
        Expression callee = expression.getCallee();
        List<Expression> arguments = expression.getArguments();

        return null; // TODO
    }

    @Override
    public Symbol visitNew(Expression.New expression) {
        Expression call = expression.getCall();

        return null; // TODO
    }

    @Override
    public Symbol visitGet(Expression.Get expression) {
        Expression object = expression.getObject();
        Token name = expression.getName();

        return null; // TODO
    }

    @Override
    public Symbol visitSet(Expression.Set expression) {
        Expression object = expression.getObject();
        Token name = expression.getName();
        Expression value = expression.getValue();

        return null; // TODO
    }

    @Override
    public Symbol visitThis(Expression.This expression) {
        return null;
    }

    @Override
    public Symbol visitSuper(Expression.Super expression) {
        return null;
    }

    @Override
    public Symbol visitGrouping(Expression.Grouping expression) {
        Expression expr = expression.getExpression();
        return expr.accept(this);
    }

    @Override
    public Symbol visitLiteral(Expression.Literal expression) {
        Object value = expression.getValue();

        return null; // TODO
    }

    @Override
    public Symbol visitLogical(Expression.Logical expression) {
        Token operator = expression.getOperator();
        Expression left = expression.getLeft();
        Expression right = expression.getRight();

        return null; // TODO
    }

    @Override
    public Symbol visitUnary(Expression.Unary expression) {
        Token operator = expression.getOperator();
        Expression right = expression.getRight();

        return null; // TODO
    }

    @Override
    public Symbol visitVariable(Expression.Variable expression) {
        Token name = expression.getName();

        String nameValue = (String) name.value();
        if (!this.symbolTable.containsSymbol(nameValue, SymbolType.VARIABLE)) {
            throw error(name, "Variable with name '" + name.value() + "' does not exist in this scope!");
        }

        return this.symbolTable.getVariable(nameValue);
    }

    @Override
    public Symbol visitFunction(Expression.Function expression) {
        Token name = expression.getName();

        String nameValue = (String) name.value();
        if (!this.symbolTable.containsSymbol(nameValue, SymbolType.METHOD)) {
            throw error(name, "Method with name '" + name.value() + "' does not exist in this scope!");
        }

        return null;
    }

    @Override
    public Symbol visitExtends(Expression.Extends expression) {
        Token superClass = expression.getName();

        String superClassValue = (String) superClass.value();
        if (!this.symbolTable.containsSymbol(superClassValue, SymbolType.CLASS)) {
            throw error(superClass, "Class with name '" + superClass.value() + "' does not exist in this scope!");
        }

        return this.symbolTable.getClass(superClassValue);
    }

    private static Either<PrimitiveType, String> checkBinaryExpression(Token operator, Either<PrimitiveType, String> leftType, Either<PrimitiveType, String> rightType) {
        // TODO: Kill me now

        throw new SemanticException(operator, "Invalid binary expression: " + leftType + " " + operator + " " + rightType);
    }
}
