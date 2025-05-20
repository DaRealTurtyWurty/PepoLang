package dev.turtywurty.pepolang.semanticAnalysis;

import dev.turtywurty.pepolang.lexer.Token;
import dev.turtywurty.pepolang.lexer.TokenType;
import dev.turtywurty.pepolang.parser.*;
import dev.turtywurty.pepolang.semanticAnalysis.symbol.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SemanticAnalyzer implements StatementVisitor<Symbol>, ExpressionVisitor<Symbol> {
    private final List<Statement> statements;
    private final SymbolTable symbolTable;

    private final List<SemanticException> errors = new ArrayList<>();

    public SemanticAnalyzer(List<Statement> statements) {
        this.statements = statements;
        this.symbolTable = new SymbolTable();
        this.symbolTable.addSymbol(new MethodSymbol("print", PrimitiveType.VOID, List.of(new VariableSymbol("value", PrimitiveType.STRING))));
        this.symbolTable.addSymbol(new MethodSymbol("time", PrimitiveType.LONG, List.of()));
        this.symbolTable.addSymbol(new MethodSymbol("randomDouble", PrimitiveType.DOUBLE, List.of(new VariableSymbol("min", PrimitiveType.DOUBLE), new VariableSymbol("max", PrimitiveType.DOUBLE))));
        this.symbolTable.addSymbol(new MethodSymbol("randomInt", PrimitiveType.INT, List.of(new VariableSymbol("min", PrimitiveType.INT), new VariableSymbol("max", PrimitiveType.INT))));
        this.symbolTable.addSymbol(new MethodSymbol("sqrt", PrimitiveType.DOUBLE, List.of(new VariableSymbol("value", PrimitiveType.DOUBLE))));
        this.symbolTable.addSymbol(new MethodSymbol("input", PrimitiveType.STRING, List.of(new VariableSymbol("prompt", PrimitiveType.STRING))));
        this.symbolTable.addSymbol(new MethodSymbol("parseInt", PrimitiveType.INT, List.of(new VariableSymbol("value", PrimitiveType.STRING))));
        this.symbolTable.addSymbol(new MethodSymbol("parseDouble", PrimitiveType.DOUBLE, List.of(new VariableSymbol("value", PrimitiveType.STRING))));
        this.symbolTable.addSymbol(new MethodSymbol("sleep", PrimitiveType.VOID, List.of(new VariableSymbol("milliseconds", PrimitiveType.LONG))));
    }

    public void analyze() {
        for (Statement statement : this.statements) {
            try {
                statement.accept(this);
            } catch (Exception ignored) {
            }
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
        Token nameToken = statement.getName();
        String className = (String) nameToken.value();
        if (this.symbolTable.isSymbolDefinedInCurrentScope(className)) {
            throw error(nameToken, "Class with name '" + className + "' already exists in this scope!");
        }

        var classSymbol = new ClassSymbol(className, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        this.symbolTable.addSymbol(classSymbol);

        this.symbolTable.enterScope();

        var thisSymbol = new VariableSymbol("this", className);
        this.symbolTable.addSymbol(thisSymbol);

        List<VariableSymbol> staticFields = new ArrayList<>();
        for (Statement.VariableStatement staticField : statement.getStaticFields()) {
            staticFields.add(handleField(this, staticField));
        }

        classSymbol.getStaticFields().addAll(staticFields);

        List<MethodSymbol> staticMethods = new ArrayList<>();
        for (Statement.FunctionStatement staticMethod : statement.getStaticMethods()) {
            staticMethods.add(handleMethod(this, staticMethod));
        }

        classSymbol.getStaticMethods().addAll(staticMethods);

        List<VariableSymbol> fields = new ArrayList<>();
        for (Statement.VariableStatement field : statement.getFields()) {
            fields.add(handleField(this, field));
        }
        classSymbol.getFields().addAll(fields);

        List<MethodSymbol> methods = new ArrayList<>();
        for (Statement.FunctionStatement method : statement.getMethods()) {
            methods.add(handleMethod(this, method));
        }

        classSymbol.getMethods().addAll(methods);

        List<MethodSymbol> constructors = new ArrayList<>();
        for (Statement.ConstructorStatement constructor : statement.getConstructors()) {
            constructors.add(handleConstructor(this, constructor, classSymbol));
        }

        classSymbol.getConstructors().addAll(constructors);

        this.symbolTable.exitScope();
        return classSymbol;
    }

    @Override
    public Symbol visitConstructorStatement(Statement.ConstructorStatement statement) {
        return handleConstructor(this, statement, null);
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
            throw error(operator, "Invalid binary expression: one or more operands are null.");
        }

        if (!(leftSymbol instanceof HasReturnType leftReturnTypeSymbol) || !(rightSymbol instanceof HasReturnType rightReturnTypeSymbol)) {
            throw error(operator, "Invalid binary expression: one or more operands are not of type ReturnTypeSymbol.");
        }

        Either<PrimitiveType, String> leftType = leftReturnTypeSymbol.getReturnType();
        Either<PrimitiveType, String> rightType = rightReturnTypeSymbol.getReturnType();

        if (leftType.isLeft() && rightType.isLeft()) {
            PrimitiveType type = TypeChecker.checkBinaryExpression(operator, leftType.getLeft(), rightType.getLeft());
            return new ValueSymbol(Either.left(type));
        }

        if ((leftType.isLeft() && leftType.getLeft() == PrimitiveType.STRING) || (rightType.isLeft() && rightType.getLeft() == PrimitiveType.STRING)) {
            if (operator.type() == TokenType.ADD) {
                return new ValueSymbol(Either.left(PrimitiveType.STRING));
            }

            throw error(operator, "Invalid binary expression: operator '" + operator.type().name() + "' cannot be used with strings.");
        }

        // TODO: Handle operator overloading maybe (?)
        throw error(operator, "Invalid binary expression: operator '" + operator.type().name() + "' cannot be used with types '" + leftType + "' and '" + rightType + "'.");
    }

    @Override
    public Symbol visitCall(Expression.Call expression) {
        Expression callee = expression.getCallee();
        List<Expression> arguments = expression.getArguments();

        Symbol calleeSymbol = callee.accept(this);
        if (calleeSymbol == null) {
            throw error(expression.getParen(), "Invalid call expression: callee is null.");
        }

        if (!(calleeSymbol instanceof MethodSymbol methodSymbol)) {
            throw error(expression.getParen(), "Invalid call expression: callee is not of type MethodSymbol.");
        }

        List<VariableSymbol> parameters = methodSymbol.getParameters();
        if (parameters.size() != arguments.size()) {
            throw error(expression.getParen(), "Method '%s' has %d parameters, but %d arguments were provided.".formatted(methodSymbol.getName(), parameters.size(), arguments.size()));
        }

        for (int i = 0; i < parameters.size(); i++) { // TODO: Copilot wrote this, confirm that it's correct lol
            VariableSymbol parameter = parameters.get(i);
            Expression argument = arguments.get(i);
            Symbol argumentSymbol = argument.accept(this);

            if (argumentSymbol == null) {
                throw error(expression.getParen(), "Invalid call expression: argument is null.");
            }

            if (!(argumentSymbol instanceof HasReturnType argumentReturnTypeSymbol)) {
                throw error(expression.getParen(), "Invalid call expression: argument is not of type ReturnTypeSymbol.");
            }

            Either<PrimitiveType, String> argumentType = argumentReturnTypeSymbol.getReturnType();
            Either<PrimitiveType, String> parameterType = parameter.getReturnType();

            if (argumentType.isLeft() && parameterType.isLeft()) {
                if (argumentType.getLeft() != parameterType.getLeft()) {
                    throw error(expression.getParen(), "Invalid call expression: expected argument of type '" + parameterType.getLeft() + "', but got argument of type '" + argumentType.getLeft() + "'.");
                }
            } else if (argumentType.isRight() && parameterType.isRight()) {
                if (!argumentType.getRight().equals(parameterType.getRight())) {
                    throw error(expression.getParen(), "Invalid call expression: expected argument of type '" + parameterType.getRight() + "', but got argument of type '" + argumentType.getRight() + "'.");
                }
            } else {
                throw error(expression.getParen(), "Invalid call expression: argument and parameter types do not match.");
            }
        }

        return methodSymbol;
    }

    @Override
    public Symbol visitNew(Expression.New expression) {
        Expression callLikeExpr = expression.getCall();
        if (!(callLikeExpr instanceof Expression.Call call)) {
            throw error(expression.getKeyword(), "Expected a constructor call after 'new', but got '%s'.".formatted(callLikeExpr));
        }

        Expression callee = call.getCallee();
        String className;
        Token classNameToken;

        if (callee instanceof Expression.Function function) {
            classNameToken = function.getName();
            className = (String) classNameToken.value();
        } else if (callee instanceof Expression.Variable variable) {
            classNameToken = variable.getName();
            className = (String) classNameToken.value();
        } else {
            throw error(expression.getKeyword(), "Expected a class name after 'new', but got '%s'.".formatted(callee));
        }

        ClassSymbol foundSymbol = this.symbolTable.getClass(className);
        if (foundSymbol == null) {
            throw error(classNameToken, "Class with name '%s' does not exist in this scope!".formatted(className));
        }

        List<Either<PrimitiveType, String>> argumentTypes = new ArrayList<>();
        for (Expression argExpr : call.getArguments()) {
            Symbol argSymbol = argExpr.accept(this);
            if (!(argSymbol instanceof HasReturnType argReturnTypeSymbol)) {
                throw error(call.getParen(), "Argument in constructor call does not have a resolvable type.");
            }

            argumentTypes.add(argReturnTypeSymbol.getReturnType());
        }

        MethodSymbol constructorSymbol = foundSymbol.findConstructor(argumentTypes);
        if (constructorSymbol == null) {
            String argTypesString = argumentTypes.stream()
                    .map(type -> type.map(PrimitiveType::toString, s -> s))
                    .collect(Collectors.joining(", "));

            throw error(classNameToken, "No matching constructor found for class '%s' with argument types (%s).".formatted(className, argTypesString));
        }

        return constructorSymbol;
    }

    @Override
    public Symbol visitGet(Expression.Get expression) {
        Expression object = expression.getObject();
        Token nameToken = expression.getName();
        String memberName = (String) nameToken.value();

        Symbol objectSymbol = object.accept(this);
        if (objectSymbol == null) {
            String objectExprStr = (object instanceof Expression.Variable variable) ?
                    "'" + variable.getName().value() + "'" :
                    "object expression";
            throw error(nameToken, "Invalid get expression: " + objectExprStr + " could not be resolved.");
        }

        if (!(objectSymbol instanceof HasReturnType objectTypeProvider)) {
            throw error(nameToken, "Invalid get expression: object '" + objectSymbol.getName() + "' does not have a retrievable type.");
        }

        Either<PrimitiveType, String> objectType = objectTypeProvider.getReturnType();
        if(objectType.isLeft()) {
            throw error(nameToken, "Cannot access property '" + memberName + "' on primitive type '" + objectType.getLeft() + "'.");
        }

        String className = objectType.getRight();
        ClassSymbol classSymbol = this.symbolTable.getClass(className);
        if(classSymbol == null) {
            throw error(nameToken, "Class with name '%s' does not exist in this scope!".formatted(className));
        }

        VariableSymbol fieldSymbol = classSymbol.findField(memberName);
        if(fieldSymbol != null) {
            return fieldSymbol; // TODO: Static and visibility checks
        }

        MethodSymbol methodSymbol = classSymbol.findMethod(memberName);
        if(methodSymbol != null) {
            return methodSymbol; // TODO: Static and visibility checks
        }

        throw error(nameToken, "Class '%s' does not have a member with name '%s'.".formatted(className, memberName));
    }

    @Override
    public Symbol visitSet(Expression.Set expression) {
        Expression object = expression.getObject();
        Token name = expression.getName();
        Expression value = expression.getValue();

        Symbol objectSymbol = object.accept(this);
        if (objectSymbol == null) {
            throw error(name, "Invalid set expression: object is null.");
        }

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

        return switch (value) {
            case Integer _ -> new ValueSymbol(Either.left(PrimitiveType.INT));
            case Double _ -> new ValueSymbol(Either.left(PrimitiveType.DOUBLE));
            case Boolean _ -> new ValueSymbol(Either.left(PrimitiveType.BOOL));
            case String _ -> new ValueSymbol(Either.left(PrimitiveType.STRING));
            case Float _ -> new ValueSymbol(Either.left(PrimitiveType.FLOAT));
            case Long _ -> new ValueSymbol(Either.left(PrimitiveType.LONG));
            case Short _ -> new ValueSymbol(Either.left(PrimitiveType.SHORT));
            case Byte _ -> new ValueSymbol(Either.left(PrimitiveType.BYTE));
            case Character _ -> new ValueSymbol(Either.left(PrimitiveType.CHAR));
            case null, default ->
                    throw error(null, "Invalid literal expression: value is not of a valid type. Value: " + value);
        };
    }

    @Override
    public Symbol visitLogical(Expression.Logical expression) {
        Token operator = expression.getOperator();
        Expression left = expression.getLeft();
        Expression right = expression.getRight();

        Symbol leftSymbol = left.accept(this);
        Symbol rightSymbol = right.accept(this);

        if (leftSymbol == null || rightSymbol == null) {
            throw error(operator, "Invalid logical expression: one or more operands are null.");
        }

        if (!(leftSymbol instanceof HasReturnType leftReturnTypeSymbol) || !(rightSymbol instanceof HasReturnType rightReturnTypeSymbol)) {
            throw error(operator, "Invalid logical expression: one or more operands are not of type ReturnTypeSymbol.");
        }

        Either<PrimitiveType, String> leftType = leftReturnTypeSymbol.getReturnType();
        Either<PrimitiveType, String> rightType = rightReturnTypeSymbol.getReturnType();

        if (leftType.isLeft() && rightType.isLeft()) {
            if (operator.type() == TokenType.OR || operator.type() == TokenType.AND) {
                if (leftType.getLeft() == PrimitiveType.BOOL && rightType.getLeft() == PrimitiveType.BOOL) {
                    return new ValueSymbol(Either.left(PrimitiveType.BOOL));
                }
            }
        }

        throw error(operator, "Invalid logical expression: operator '" + operator.type().name() + "' cannot be used with types '" + leftType + "' and '" + rightType + "'.");
    }

    @Override
    public Symbol visitUnary(Expression.Unary expression) {
        Token operator = expression.getOperator();
        Expression right = expression.getRight();

        Symbol rightSymbol = right.accept(this);
        if (rightSymbol == null) {
            throw error(operator, "Invalid unary expression: right operand is null.");
        }

        if (!(rightSymbol instanceof HasReturnType rightReturnTypeSymbol)) {
            throw error(operator, "Invalid unary expression: right operand does not have a type.");
        }

        Either<PrimitiveType, String> rightType = rightReturnTypeSymbol.getReturnType();

        if (rightType.isLeft()) {
            PrimitiveType type = TypeChecker.checkUnaryExpression(operator, rightType.getLeft());
            return new ValueSymbol(Either.left(type));
        }

        if (rightType.isRight()) {
            throw error(operator, "Invalid unary expression: right operand type is invalid.");
        }

        throw error(operator, "Invalid unary expression: right operand type is invalid.");
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

        return this.symbolTable.getMethods(nameValue).getFirst();
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

    private static VariableSymbol handleField(SemanticAnalyzer semanticAnalyzer, Statement.VariableStatement field) {
        Token fieldName = field.getName();
        Token fieldType = field.getType();
        Expression initializer = field.getInitializer();

        if (fieldType.type() == TokenType.KEYWORD_VOID) {
            throw semanticAnalyzer.error(fieldType, "Cannot use 'void' as a field type!");
        }

        String fieldNameValue = (String) fieldName.value();
        if (semanticAnalyzer.symbolTable.isSymbolDefinedInCurrentScope(fieldNameValue, SymbolType.FIELD)) {
            throw semanticAnalyzer.error(fieldName, "Field with name '" + fieldName.value() + "' already exists in this scope!");
        }

        var symbol = new VariableSymbol(fieldNameValue, fieldType);
        semanticAnalyzer.symbolTable.addSymbol(symbol);

        if (initializer != null) {
            initializer.accept(semanticAnalyzer);
            // TODO: Type check initializer against field type
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

//        List<MethodSymbol> methods = semanticAnalyzer.symbolTable.getMethods(nameValue);
//        if (methods != null) {
//            boolean methodAlreadyOverloaded = false;
//            for (MethodSymbol otherMethod : methods) {
//                if (otherMethod.matches(symbol.getParameters())) {
//                    semanticAnalyzer.error(methodName, "Method with name '" + methodName.value() + "' already exists in this scope!");
//                    methodAlreadyOverloaded = true;
//                }
//            }
//
//            if (methodAlreadyOverloaded)
//                throw error(methodName, "Method with name '" + methodName.value() + "' already exists in this scope!");
//        }

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

    private static MethodSymbol handleConstructor(SemanticAnalyzer semanticAnalyzer, Statement.ConstructorStatement constructor, ClassSymbol ownerClass) {
        List<Parameter> parameters = constructor.getParameters();

        List<VariableSymbol> paramSymbols = new ArrayList<>();
        for (Parameter param : parameters) {
            Token paramName = param.name();
            Token paramType = param.type();
            if (paramType.type() == TokenType.KEYWORD_VOID) {
                throw semanticAnalyzer.error(paramType, "Cannot use 'void' as a parameter type!");
            }

            var paramSymbol = new VariableSymbol((String) paramName.value(), paramType);
            paramSymbols.add(paramSymbol);
        }

        var constructorSymbol = new MethodSymbol(ownerClass.getName(), PrimitiveType.VOID, paramSymbols);
        semanticAnalyzer.symbolTable.addSymbol(constructorSymbol);

        semanticAnalyzer.symbolTable.enterScope();
        for (VariableSymbol param : paramSymbols) {
            semanticAnalyzer.symbolTable.addSymbol(param);
        }

        List<Statement> body = constructor.getBody();
        for (Statement stmt : body) {
            stmt.accept(semanticAnalyzer);
        }

        semanticAnalyzer.symbolTable.exitScope();
        return constructorSymbol;
    }

    private static String getMethodSignature(MethodSymbol method) {
        var signature = new StringBuilder();
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

    public SemanticException error(Token token, String message) {
        var exception = new SemanticException(token, message);
        this.errors.add(exception);
        return exception;
    }

    public boolean hadError() {
        return !this.errors.isEmpty();
    }

    public List<SemanticException> getErrors() {
        return this.errors;
    }
}
