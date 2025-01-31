package dev.turtywurty.pepolang.codeGeneration;

import dev.turtywurty.pepolang.lexer.Token;
import dev.turtywurty.pepolang.lexer.TokenType;
import dev.turtywurty.pepolang.parser.*;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.*;
import org.bytedeco.llvm.global.LLVM;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.*;

public class LLVMCodeGenerator {
    public static void generate(List<Statement> statements, Path outputPath) {
        LLVM.LLVMInitializeNativeTarget();
        LLVM.LLVMInitializeNativeAsmPrinter();

        LLVMContextRef context = LLVM.LLVMContextCreate();
        LLVMModuleRef module = LLVM.LLVMModuleCreateWithName("pepolang");
        LLVMBuilderRef builder = LLVM.LLVMCreateBuilderInContext(context);

        var visitor = new LLVMCodeGeneratorVisitor(context, module, builder);
        for (Statement statement : statements) {
            statement.accept(visitor);
        }

        ByteBuffer error = ByteBuffer.allocateDirect(1024);
        LLVM.LLVMPrintModuleToFile(module, outputPath.toString(), error);

        LLVM.LLVMDisposeBuilder(builder);
        LLVM.LLVMDisposeModule(module);
        LLVM.LLVMContextDispose(context);
    }

    public static class LLVMCodeGeneratorVisitor implements StatementVisitor<LLVMValueRef>, ExpressionVisitor<LLVMValueRef> {
        private final LLVMContextRef context;
        private final LLVMModuleRef module;
        private final LLVMBuilderRef builder;
        private final SymbolTable symbolTable = new SymbolTable();
        private final Stack<LoopBlock> loopBlocks = new Stack<>();

        public LLVMCodeGeneratorVisitor(LLVMContextRef context, LLVMModuleRef module, LLVMBuilderRef builder) {
            this.context = context;
            this.module = module;
            this.builder = builder;
        }

        @Override
        public LLVMValueRef visitAssign(Expression.Assign expression) {
            Token name = expression.getName();
            Expression value = expression.getValue();

            SymbolTable.Symbol symbol = symbolTable.lookup((String) name.value());
            if(symbol == null)
                throw new RuntimeException("Unknown variable: " + name.value());

            LLVMValueRef llvmValue = value.accept(this);
            LLVM.LLVMBuildStore(builder, llvmValue, symbol.llvmValue());

            return llvmValue;
        }

        @Override
        public LLVMValueRef visitBinary(Expression.Binary expression) {
            LLVMValueRef left = expression.getLeft().accept(this);
            LLVMValueRef right = expression.getRight().accept(this);

            return switch (expression.getOperator().type()) {
                case ADD -> LLVM.LLVMBuildAdd(builder, left, right, "addtmp");
                case SUB -> LLVM.LLVMBuildSub(builder, left, right, "subtmp");
                case MUL -> LLVM.LLVMBuildMul(builder, left, right, "multmp");
                case DIV -> LLVM.LLVMBuildSDiv(builder, left, right, "divtmp");
                case MOD -> LLVM.LLVMBuildSRem(builder, left, right, "modtmp");
                case EQUAL -> LLVM.LLVMBuildICmp(builder, LLVM.LLVMIntEQ, left, right, "eqtmp");
                case NOT_EQUAL -> LLVM.LLVMBuildICmp(builder, LLVM.LLVMIntNE, left, right, "neqtmp");
                case GT -> LLVM.LLVMBuildICmp(builder, LLVM.LLVMIntSGT, left, right, "gttmp");
                case GREATER_EQUAL -> LLVM.LLVMBuildICmp(builder, LLVM.LLVMIntSGE, left, right, "gtetmp");
                case LT -> LLVM.LLVMBuildICmp(builder, LLVM.LLVMIntSLT, left, right, "lttmp");
                case LESS_EQUAL -> LLVM.LLVMBuildICmp(builder, LLVM.LLVMIntSLE, left, right, "ltetmp");
                default -> throw new UnsupportedOperationException("Unknown binary operator: " + expression.getOperator().type());
            };
        }

        @Override
        public LLVMValueRef visitCall(Expression.Call expression) {
            Expression callee = expression.getCallee();
            List<Expression> arguments = expression.getArguments();

            LLVMValueRef llvmCallee = callee.accept(this);
            if(llvmCallee == null)
                throw new RuntimeException("Unknown function: " + callee);

            LLVMTypeRef llvmFunctionType = LLVM.LLVMTypeOf(llvmCallee);
            if(LLVM.LLVMGetTypeKind(llvmFunctionType) != LLVM.LLVMFunctionTypeKind)
                throw new RuntimeException("Callee is not a function type: " + callee);

            LLVMValueRef[] llvmArguments = arguments.stream()
                    .map(arg -> arg.accept(this))
                    .toArray(LLVMValueRef[]::new);

            return LLVM.LLVMBuildCall2(builder, llvmFunctionType, llvmCallee,
                    new PointerPointer<>(llvmArguments), llvmArguments.length, "calltmp");
        }

        @Override
        public LLVMValueRef visitNew(Expression.New expression) {
            Expression call = expression.getCall();

            LLVMValueRef constructor = call.accept(this);
            if(constructor == null)
                throw new RuntimeException("Unknown constructor: " + call);

            LLVMTypeRef constructorType = LLVM.LLVMTypeOf(constructor);
            if(LLVM.LLVMGetTypeKind(constructorType) != LLVM.LLVMFunctionTypeKind)
                throw new RuntimeException("Constructor is not a function type: " + call);

            LLVMTypeRef classType = LLVM.LLVMGetReturnType(constructorType);
            LLVMValueRef objectPtr = LLVM.LLVMBuildMalloc(builder, classType, "new_object");

            List<Expression> arguments = ((Expression.Call) call).getArguments();
            LLVMValueRef[] llvmArguments = arguments.stream()
                    .map(arg -> arg.accept(this))
                    .toArray(value -> new LLVMValueRef[value + 1]);
            System.arraycopy(llvmArguments, 0, llvmArguments, 1, llvmArguments.length - 1);
            llvmArguments[0] = objectPtr;

            LLVM.LLVMBuildCall2(builder, constructorType, constructor,
                    new PointerPointer<>(llvmArguments), llvmArguments.length, "");

            return objectPtr;
        }

        @Override
        public LLVMValueRef visitGet(Expression.Get expression) {
            Token name = expression.getName();
            Expression object = expression.getObject();

            LLVMValueRef objectPtr = object.accept(this);
            if(objectPtr == null)
                throw new RuntimeException("Unknown object reference: " + object);

            LLVMTypeRef objectType = LLVM.LLVMTypeOf(objectPtr);
            if(LLVM.LLVMGetTypeKind(objectType) != LLVM.LLVMPointerTypeKind)
                throw new RuntimeException("Object is not a pointer type: " + object);

            LLVMValueRef fieldPtr = LLVM.LLVMBuildStructGEP2(builder, objectType, objectPtr, 0, (String) name.value());
            return LLVM.LLVMBuildLoad2(builder, LLVM.LLVMTypeOf(fieldPtr), fieldPtr, name.value() + "_load");
        }

        @Override
        public LLVMValueRef visitSet(Expression.Set expression) {
            Token name = expression.getName();
            Expression object = expression.getObject();
            Expression value = expression.getValue();

            LLVMValueRef objectPtr = object.accept(this);
            if(objectPtr == null)
                throw new RuntimeException("Unknown object reference: " + object);

            LLVMTypeRef objectType = LLVM.LLVMTypeOf(objectPtr);
            if(LLVM.LLVMGetTypeKind(objectType) != LLVM.LLVMPointerTypeKind)
                throw new RuntimeException("Object is not a pointer type: " + object);

            LLVMValueRef llvmValue = value.accept(this);
            LLVMValueRef fieldPtr = LLVM.LLVMBuildStructGEP2(builder, objectType, objectPtr, 0, (String) name.value());
            LLVM.LLVMBuildStore(builder, llvmValue, fieldPtr);

            return llvmValue;
        }

        @Override
        public LLVMValueRef visitThis(Expression.This expression) {
            SymbolTable.Symbol symbol = symbolTable.lookup("this");
            if(symbol == null)
                throw new RuntimeException("Unknown 'this' reference!");

            return symbol.llvmValue();
        }

        @Override
        public LLVMValueRef visitSuper(Expression.Super expression) {
            SymbolTable.Symbol symbol = symbolTable.lookup("super");
            if(symbol == null)
                throw new RuntimeException("Unknown 'super' reference!");

            return symbol.llvmValue();
        }

        @Override
        public LLVMValueRef visitGrouping(Expression.Grouping expression) {
            return expression.getExpression().accept(this);
        }

        @Override
        public LLVMValueRef visitLiteral(Expression.Literal expression) {
            return switch (expression.getValue()) {
                case Integer integerVal -> LLVM.LLVMConstInt(LLVM.LLVMInt32TypeInContext(context), integerVal, 0);
                case String stringVal -> LLVM.LLVMConstStringInContext(context, stringVal, stringVal.length(), 0);
                case Boolean booleanVal ->
                        LLVM.LLVMConstInt(LLVM.LLVMInt1TypeInContext(context), booleanVal ? 1 : 0, 0);
                case Double doubleVal -> LLVM.LLVMConstReal(LLVM.LLVMFloatTypeInContext(context), doubleVal);
                case Float floatVal -> LLVM.LLVMConstReal(LLVM.LLVMFloatTypeInContext(context), floatVal);
                case Long longVal -> LLVM.LLVMConstInt(LLVM.LLVMInt64TypeInContext(context), longVal, 0);
                case Byte byteVal -> LLVM.LLVMConstInt(LLVM.LLVMInt8TypeInContext(context), byteVal, 0);
                case Short shortVal -> LLVM.LLVMConstInt(LLVM.LLVMInt16TypeInContext(context), shortVal, 0);
                case Character charVal -> LLVM.LLVMConstInt(LLVM.LLVMInt8TypeInContext(context), charVal, 0);
                case null -> LLVM.LLVMConstNull(LLVM.LLVMInt32TypeInContext(context));
                default ->
                        throw new UnsupportedOperationException("Unknown literal type: " + expression.getValue().getClass());
            };
        }

        @Override
        public LLVMValueRef visitLogical(Expression.Logical expression) {
            LLVMValueRef left = expression.getLeft().accept(this);
            LLVMValueRef right = expression.getRight().accept(this);

            return switch (expression.getOperator().type()) {
                case AND -> LLVM.LLVMBuildAnd(builder, left, right, "andtmp");
                case OR -> LLVM.LLVMBuildOr(builder, left, right, "ortmp");
                default -> throw new UnsupportedOperationException("Unknown logical operator: " + expression.getOperator().type());
            };
        }

        @Override
        public LLVMValueRef visitUnary(Expression.Unary expression) {
            LLVMValueRef right = expression.getRight().accept(this);

            return switch (expression.getOperator().type()) {
                case NOT -> LLVM.LLVMBuildNot(builder, right, "nottmp");
                case SUB -> LLVM.LLVMBuildNeg(builder, right, "negtmp");
                default -> throw new UnsupportedOperationException("Unknown unary operator: " + expression.getOperator().type());
            };
        }

        @Override
        public LLVMValueRef visitVariable(Expression.Variable expression) {
            SymbolTable.Symbol symbol = symbolTable.lookup((String) expression.getName().value());
            if (symbol == null) {
                throw new RuntimeException("Unknown variable: " + expression.getName().value());
            }

            LLVMValueRef variable = symbol.llvmValue();
            LLVMTypeRef variableType = LLVM.LLVMTypeOf(variable);

            return LLVM.LLVMBuildLoad2(builder, variableType, variable, expression.getName().value() + "_load");
        }

        @Override
        public LLVMValueRef visitFunction(Expression.Function expression) {
            SymbolTable.Symbol functionSymbol = symbolTable.lookup((String) expression.getName().value());

            if (functionSymbol == null)
                throw new RuntimeException("Unknown function: " + expression.getName().value());

            return functionSymbol.llvmValue();
        }

        @Override
        public LLVMValueRef visitBlockStatement(Statement.BlockStatement statement) {
            this.symbolTable.enterScope();

            for (Statement stmt : statement.getStatements()) {
                stmt.accept(this);
            }

            this.symbolTable.exitScope();
            return null;
        }

        @Override
        public LLVMValueRef visitFunctionStatement(Statement.FunctionStatement statement) {
            Token name = statement.getName();
            Token returnType = statement.getReturnType();
            List<Parameter> parameters = statement.getParameters();
            List<Statement> body = statement.getBody();

            LLVMTypeRef llvmReturnType = mapType(returnType);
            LLVMTypeRef[] llvmParameterTypes = parameters.stream()
                    .map(Parameter::type)
                    .map(this::mapType)
                    .toArray(LLVMTypeRef[]::new);

            LLVMTypeRef llvmFunctionType = LLVM.LLVMFunctionType(llvmReturnType, new PointerPointer<>(llvmParameterTypes), llvmParameterTypes.length, 0); // TODO: Add support for variadic functions
            LLVMValueRef llvmFunction = LLVM.LLVMAddFunction(module, (String) name.value(), llvmFunctionType);

            this.symbolTable.enterScope();

            int paramIndex = 0;
            for (Parameter parameter : parameters) {
                String parameterName = (String) parameter.name().value();
                LLVMValueRef llvmParameter = LLVM.LLVMGetParam(llvmFunction, paramIndex++);
                this.symbolTable.insert(parameterName, new SymbolTable.Symbol(parameterName, SymbolTable.SymbolType.PARAMETER, llvmParameter));
            }

            LLVMBasicBlockRef entry = LLVM.LLVMAppendBasicBlock(llvmFunction, "entry");
            LLVM.LLVMPositionBuilderAtEnd(builder, entry);

            for (Statement stmt : body) {
                stmt.accept(this);
            }

            if(llvmReturnType.equals(LLVM.LLVMVoidType())) {
                LLVM.LLVMBuildRetVoid(builder);
            } else {
                LLVM.LLVMBuildRet(builder, LLVM.LLVMConstNull(llvmReturnType));
            }

            this.symbolTable.exitScope();

            return llvmFunction;
        }

        @Override
        public LLVMValueRef visitVariableStatement(Statement.VariableStatement statement) {
            Token name = statement.getName();
            String variableName = (String) name.value();
            Token type = statement.getType();
            @Nullable Expression initializer = statement.getInitializer();

            LLVMTypeRef llvmType = mapType(type);
            LLVMValueRef allocatedVariable = LLVM.LLVMBuildAlloca(this.builder, llvmType, variableName);

            if(initializer != null) {
                LLVMValueRef llvmInitializer = initializer.accept(this);
                LLVM.LLVMBuildStore(this.builder, llvmInitializer, allocatedVariable);
            }

            this.symbolTable.insert(variableName, new SymbolTable.Symbol(variableName, SymbolTable.SymbolType.VARIABLE, allocatedVariable));

            return allocatedVariable;
        }

        @Override
        public LLVMValueRef visitExpressionStatement(Statement.ExpressionStatement statement) {
            return statement.getExpression().accept(this);
        }

        @Override
        public LLVMValueRef visitIfStatement(Statement.IfStatement statement) {
            Expression condition = statement.getCondition();
            Statement thenBranch = statement.getThenBranch();
            Statement elseBranch = statement.getElseBranch();

            LLVMValueRef llvmCondition = condition.accept(this);
            if (!LLVM.LLVMTypeOf(llvmCondition).equals(LLVM.LLVMInt1Type())) {
                llvmCondition = LLVM.LLVMBuildICmp(builder, LLVM.LLVMIntNE,
                        llvmCondition,
                        LLVM.LLVMConstNull(LLVM.LLVMTypeOf(llvmCondition)),
                        "condition_cast");
            }

            LLVMValueRef function = LLVM.LLVMGetBasicBlockParent(LLVM.LLVMGetInsertBlock(builder));
            LLVMBasicBlockRef thenBlock = LLVM.LLVMAppendBasicBlock(function, "then");
            LLVMBasicBlockRef elseBlock = (elseBranch != null) ? LLVM.LLVMAppendBasicBlock(function, "else") : null;
            LLVMBasicBlockRef mergeBlock = LLVM.LLVMAppendBasicBlock(function, "merge");

            LLVM.LLVMBuildCondBr(builder, llvmCondition, thenBlock, elseBlock != null ? elseBlock : mergeBlock);

            LLVM.LLVMPositionBuilderAtEnd(builder, thenBlock);
            this.symbolTable.enterScope();
            thenBranch.accept(this);
            this.symbolTable.exitScope();
            LLVM.LLVMBuildBr(builder, mergeBlock);

            if (elseBranch != null) {
                LLVM.LLVMPositionBuilderAtEnd(builder, elseBlock);
                this.symbolTable.enterScope();
                elseBranch.accept(this);
                this.symbolTable.exitScope();
                LLVM.LLVMBuildBr(builder, mergeBlock);
            }

            LLVM.LLVMPositionBuilderAtEnd(builder, mergeBlock);
            return null;
        }

        @Override
        public LLVMValueRef visitExtends(Expression.Extends expression) {
            Token name = expression.getName();
            SymbolTable.Symbol symbol = symbolTable.lookup((String) name.value());
            if(symbol == null || symbol.type() != SymbolTable.SymbolType.CLASS)
                throw new RuntimeException("Unknown class: " + name.value());

            return symbol.llvmValue();
        }

        @Override
        public LLVMValueRef visitClassStatement(Statement.ClassStatement statement) {
//            Token name = statement.getName();
//            List<Statement.VariableStatement> fields = statement.getFields();
//            List<Statement.FunctionStatement> methods = statement.getMethods();
//            List<Statement.ConstructorStatement> constructors = statement.getConstructors();
//            Expression.Extends superclass = statement.getSuperclass();
//            List<Statement.VariableStatement> staticFields = statement.getStaticFields();
//            List<Statement.FunctionStatement> staticMethods = statement.getStaticMethods();
//
//            LLVMTypeRef classType = LLVM.LLVMStructCreateNamed(context, (String) name.value());
//
//            List<LLVMTypeRef> fieldTypes = new ArrayList<>();
//
//            LLVMTypeRef superclassType = null;
//            if(superclassType != null) {
//                superclassType = (LLVMTypeRef) (Object) superclass.accept(this); //  TODO: This won't work but idk how to fix it rn
//                fieldTypes.add(superclassType);
//            }
//
//            for (Statement.VariableStatement field : fields) {
//                fieldTypes.add(mapType(field.getType()));
//                //
//            }
//
//            PointerPointer<LLVMTypeRef> fieldTypesArray = new PointerPointer<>(fieldTypes.toArray(new LLVMTypeRef[0]));
//            LLVM.LLVMStructSetBody(classType, fieldTypesArray, fieldTypes.size(), 0);
//
//            LLVM.LLVMAddGlobal(module, classType, (String) name.value());
//
//            for (Statement.ConstructorStatement constructor : constructors) {
//                LLVMTypeRef constructorType = mapFunctionType(constructor);
//                LLVMValueRef constructorFunction = LLVM.LLVMAddFunction(module, name.value() + "_ctor", constructorType);
//                this.symbolTable.enterScope();
//                constructor.accept(this);
//                this.symbolTable.exitScope();
//
//                LLVM.LLVMSetLinkage(constructorFunction, LLVM.LLVMPrivateLinkage);
//            }
//
//            for (Statement.FunctionStatement method : methods) {
//                LLVMTypeRef methodType = mapFunctionType(method);
//                LLVMValueRef methodFunction = LLVM.LLVMAddFunction(module, name.value() + "_" + method.getName().value(), methodType);
//                this.symbolTable.enterScope();
//                method.accept(this);
//                this.symbolTable.exitScope();
//
//                LLVM.LLVMSetLinkage(methodFunction, LLVM.LLVMPrivateLinkage);
//            }
//
//            for (Statement.VariableStatement staticField : staticFields) {
//                LLVMTypeRef staticFieldType = mapType(staticField.getType());
//                LLVMValueRef staticFieldPtr = LLVM.LLVMBuildAlloca(builder, staticFieldType, (String) staticField.getName().value());
//                this.symbolTable.insert((String) staticField.getName().value(), new SymbolTable.Symbol((String) staticField.getName().value(), SymbolTable.SymbolType.VARIABLE, staticFieldPtr));
//            }

            return null;
        }

        @Override
        public LLVMValueRef visitConstructorStatement(Statement.ConstructorStatement statement) {
            Token name = statement.getName();
            List<Parameter> parameters = statement.getParameters();
            List<Statement> body = statement.getBody();
            
            LLVMValueRef function = LLVM.LLVMGetBasicBlockParent(LLVM.LLVMGetInsertBlock(builder));
            LLVMBasicBlockRef entry = LLVM.LLVMAppendBasicBlock(function, "entry");
            LLVM.LLVMPositionBuilderAtEnd(builder, entry);
            
            this.symbolTable.enterScope();
            int paramIndex = 0;
            for (Parameter parameter : parameters) {
                String parameterName = (String) parameter.name().value();
                LLVMValueRef llvmParameter = LLVM.LLVMGetParam(function, paramIndex++);
                this.symbolTable.insert(parameterName, new SymbolTable.Symbol(parameterName, SymbolTable.SymbolType.PARAMETER, llvmParameter));
            }
            
            for (Statement stmt : body) {
                stmt.accept(this);
            }
            
            LLVM.LLVMBuildRetVoid(builder);
            this.symbolTable.exitScope();
            
            return null;
        }

        @Override
        public LLVMValueRef visitAssignStatement(Statement.AssignStatement statement) {
            Token name = statement.getName();
            Expression value = statement.getValue();

            SymbolTable.Symbol symbol = symbolTable.lookup((String) name.value());
            if(symbol == null)
                throw new RuntimeException("Unknown variable: " + name.value());

            LLVMValueRef llvmValue = value.accept(this);
            LLVM.LLVMBuildStore(builder, llvmValue, symbol.llvmValue());

            return llvmValue;
        }

        @Override
        public LLVMValueRef visitWhileStatement(Statement.WhileStatement statement) {
            Expression condition = statement.getCondition();
            Statement body = statement.getBody();

            LLVMValueRef function = LLVM.LLVMGetBasicBlockParent(LLVM.LLVMGetInsertBlock(builder));
            LLVMBasicBlockRef conditionBlock = LLVM.LLVMAppendBasicBlock(function, "condition");
            LLVMBasicBlockRef bodyBlock = LLVM.LLVMAppendBasicBlock(function, "body");
            LLVMBasicBlockRef mergeBlock = LLVM.LLVMAppendBasicBlock(function, "merge");

            this.loopBlocks.push(new LoopBlock(conditionBlock, bodyBlock, mergeBlock));

            LLVM.LLVMBuildBr(builder, conditionBlock);

            LLVM.LLVMPositionBuilderAtEnd(builder, conditionBlock);
            LLVMValueRef llvmCondition = condition.accept(this);
            if (!LLVM.LLVMTypeOf(llvmCondition).equals(LLVM.LLVMInt1Type())) {
                llvmCondition = LLVM.LLVMBuildICmp(builder, LLVM.LLVMIntNE,
                        llvmCondition,
                        LLVM.LLVMConstNull(LLVM.LLVMTypeOf(llvmCondition)),
                        "condition_cast");
            }

            LLVM.LLVMBuildCondBr(builder, llvmCondition, bodyBlock, mergeBlock);

            LLVM.LLVMPositionBuilderAtEnd(builder, bodyBlock);
            this.symbolTable.enterScope();
            body.accept(this);
            this.symbolTable.exitScope();
            LLVM.LLVMBuildBr(builder, conditionBlock);

            LLVM.LLVMPositionBuilderAtEnd(builder, mergeBlock);

            this.loopBlocks.pop();
            return null;
        }

        @Override
        public LLVMValueRef visitBreakStatement(Statement.BreakStatement statement) {
            if(!this.loopBlocks.isEmpty()) {
                LLVM.LLVMBuildBr(builder, this.loopBlocks.peek().mergeBlock());
            } else {
                throw new RuntimeException("Cannot use 'break' outside of a loop!");
            }

            return null;
        }

        @Override
        public LLVMValueRef visitContinueStatement(Statement.ContinueStatement statement) {
            if(!this.loopBlocks.isEmpty()) {
                LLVM.LLVMBuildBr(builder, this.loopBlocks.peek().conditionBlock());
            } else {
                throw new RuntimeException("Cannot use 'continue' outside of a loop!");
            }

            return null;
        }

        @Override
        public LLVMValueRef visitReturnStatement(Statement.ReturnStatement statement) {
            Expression value = statement.getValue();

            if(value != null) {
                LLVMValueRef llvmValue = value.accept(this);
                LLVM.LLVMBuildRet(builder, llvmValue);
            } else {
                LLVM.LLVMBuildRetVoid(builder);
            }

            return null;
        }
        
        private LLVMTypeRef mapType(Token type) {
            if(type.type() != TokenType.IDENTIFIER) {
                return switch (type.type()) {
                    case KEYWORD_INT -> LLVM.LLVMInt32TypeInContext(this.context);
                    case KEYWORD_STRING -> LLVM.LLVMPointerType(LLVM.LLVMInt8TypeInContext(this.context), 0);
                    case KEYWORD_BOOL -> LLVM.LLVMInt1TypeInContext(this.context);
                    case KEYWORD_DOUBLE -> LLVM.LLVMFloatTypeInContext(this.context);
                    case KEYWORD_FLOAT -> LLVM.LLVMDoubleTypeInContext(this.context);
                    case KEYWORD_LONG -> LLVM.LLVMInt64TypeInContext(this.context);
                    case KEYWORD_BYTE, KEYWORD_CHAR -> LLVM.LLVMInt8TypeInContext(this.context);
                    case KEYWORD_SHORT -> LLVM.LLVMInt16TypeInContext(this.context);
                    case KEYWORD_VOID -> LLVM.LLVMVoidTypeInContext(this.context);
                    default -> throw new UnsupportedOperationException("Unknown type: " + type.type());
                };
            } else {
                return LLVM.LLVMPointerType(LLVM.LLVMStructCreateNamed(this.context, (String) type.value()), 0);
            }
        }
    }

    public record LoopBlock(LLVMBasicBlockRef conditionBlock, LLVMBasicBlockRef bodyBlock, LLVMBasicBlockRef mergeBlock) {}
}
