package dev.turtywurty.pepolang.tooling;

import com.palantir.javapoet.*;
import dev.turtywurty.pepolang.CollectionUtility;
import dev.turtywurty.pepolang.JavaGenerated;
import dev.turtywurty.pepolang.StringUtility;
import dev.turtywurty.pepolang.lexer.Token;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class AstGenerator {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }

        String outputDir = args[0];

        var expression = ClassName.get("dev.turtywurty.pepolang.parser", "Expression");
        var statement = ClassName.get("dev.turtywurty.pepolang.parser", "Statement");

        TypeName token = TypeName.get(Token.class);
        LinkedHashMap<String, LinkedHashMap<String, TypeName>> expressionTypes = CollectionUtility.createLinkedHashMap(
                "Assign", CollectionUtility.createLinkedHashMap(
                        "name", token,
                        "value", expression
                ),
                "Binary", CollectionUtility.createLinkedHashMap(
                        "left", expression,
                        "operator", token,
                        "right", expression
                ),
                "Call", CollectionUtility.createLinkedHashMap(
                        "callee", expression,
                        "paren", token,
                        "arguments", ParameterizedTypeName.get(ClassName.get(List.class), expression)
                ),
                "New", CollectionUtility.createLinkedHashMap(
                        "keyword", token,
                        "call", expression
                ),
                "Get", CollectionUtility.createLinkedHashMap(
                        "object", expression,
                        "name", token
                ),
                "Set", CollectionUtility.createLinkedHashMap(
                        "object", expression,
                        "name", token,
                        "value", expression
                ),
                "This", CollectionUtility.createLinkedHashMap(
                        "keyword", token
                ),
                "Super", CollectionUtility.createLinkedHashMap(
                        "keyword", token,
                        "method", token
                ),
                "Grouping", CollectionUtility.createLinkedHashMap(
                        "expression", expression
                ),
                "Literal", CollectionUtility.createLinkedHashMap(
                        "value", TypeName.get(Object.class)
                ),
                "Logical", CollectionUtility.createLinkedHashMap(
                        "left", expression,
                        "operator", token,
                        "right", expression
                ),
                "Unary", CollectionUtility.createLinkedHashMap(
                        "operator", token,
                        "right", expression
                ),
                "Variable", CollectionUtility.createLinkedHashMap(
                        "name", token
                ),
                "Function", CollectionUtility.createLinkedHashMap(
                        "name", token
                ),
                "Extends", CollectionUtility.createLinkedHashMap(
                        "name", token
                )
        );

        LinkedHashMap<String, LinkedHashMap<String, TypeName>> statementTypes = CollectionUtility.createLinkedHashMap(
                "BlockStatement", CollectionUtility.createLinkedHashMap(
                        "statements", ParameterizedTypeName.get(ClassName.get(List.class), statement)
                ),
                "FunctionStatement", CollectionUtility.createLinkedHashMap(
                        "name", token,
                        "returnType", token,
                        "parameters", ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get("dev.turtywurty.pepolang.parser", "Parameter")),
                        "body", ParameterizedTypeName.get(ClassName.get(List.class), statement)
                ),
                "VariableStatement", CollectionUtility.createLinkedHashMap(
                        "type", token,
                        "name", token,
                        "initializer", expression
                ),
                "ClassStatement", CollectionUtility.createLinkedHashMap(
                        "name", token,
                        "superclass", expression.nestedClass("Extends").annotated(AnnotationSpec.builder(ClassName.get(Nullable.class)).build()),
                        "constructors", ParameterizedTypeName.get(ClassName.get(List.class), statement.nestedClass("ConstructorStatement")),
                        "methods", ParameterizedTypeName.get(ClassName.get(List.class), statement.nestedClass("FunctionStatement")),
                        "fields", ParameterizedTypeName.get(ClassName.get(List.class), statement.nestedClass("VariableStatement")),
                        "staticMethods", ParameterizedTypeName.get(ClassName.get(List.class), statement.nestedClass("FunctionStatement")),
                        "staticFields", ParameterizedTypeName.get(ClassName.get(List.class), statement.nestedClass("VariableStatement"))
                ),
                "ConstructorStatement", CollectionUtility.createLinkedHashMap(
                        "name", token,
                        "parameters", ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get("dev.turtywurty.pepolang.parser", "Parameter")),
                        "body", ParameterizedTypeName.get(ClassName.get(List.class), statement)
                ),
                "ExpressionStatement", CollectionUtility.createLinkedHashMap(
                        "expression", expression
                ),
                "IfStatement", CollectionUtility.createLinkedHashMap(
                        "condition", expression,
                        "thenBranch", statement,
                        "elseBranch", statement
                ),
                "AssignStatement", CollectionUtility.createLinkedHashMap(
                        "name", token,
                        "value", expression
                ),
                "WhileStatement", CollectionUtility.createLinkedHashMap(
                        "condition", expression,
                        "body", statement
                ),
                "BreakStatement", CollectionUtility.createLinkedHashMap(),
                "ContinueStatement", CollectionUtility.createLinkedHashMap(),
                "ReturnStatement", CollectionUtility.createLinkedHashMap(
                        "keyword", token,
                        "value", expression
                )
        );

        defineAst(outputDir, "Expression", expressionTypes);
        defineVisitor(outputDir, "Expression", expressionTypes);
        defineAst(outputDir, "Statement", statementTypes);
        defineVisitor(outputDir, "Statement", statementTypes);
    }

    private static void defineVisitor(String outputDir, String baseName, LinkedHashMap<String, LinkedHashMap<String, TypeName>> types) throws IOException {
        TypeVariableName r = TypeVariableName.get("R");

        TypeSpec.Builder visitor = TypeSpec.interfaceBuilder(baseName + "Visitor")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(r)
                .addAnnotation(JavaGenerated.class);

        for (String type : types.keySet()) {
            visitor.addMethod(MethodSpec.methodBuilder("visit" + type)
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addParameter(ClassName.get("dev.turtywurty.pepolang.parser." + baseName, type), baseName.toLowerCase(Locale.ROOT))
                    .returns(r)
                    .build());
        }

        JavaFile javaFile = JavaFile.builder("dev.turtywurty.pepolang.parser", visitor.build())
                .skipJavaLangImports(true)
                .indent("    ")
                .addFileComment("This file is automatically generated. Do not modify.")
                .build();

        javaFile.writeTo(System.out);
        javaFile.writeTo(Paths.get(outputDir));
    }

    private static void defineAst(String outputDir, String name, LinkedHashMap<String, LinkedHashMap<String, TypeName>> types) throws IOException {
        List<TypeSpec> classes = new ArrayList<>();
        for (Map.Entry<String, LinkedHashMap<String, TypeName>> entry : types.entrySet()) {
            String className = entry.getKey();
            LinkedHashMap<String, TypeName> fields = entry.getValue();
            classes.add(defineType(className, name, fields));
        }

        MethodSpec accept = MethodSpec.methodBuilder("accept")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addTypeVariable(TypeVariableName.get("R"))
                .addParameter(ClassName.get("dev.turtywurty.pepolang.parser", name + "Visitor<R>"), "visitor")
                .returns(TypeVariableName.get("R"))
                .build();

        TypeSpec baseType = TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addTypes(classes)
                .addMethod(accept)
                .addAnnotation(JavaGenerated.class)
                .build();

        JavaFile javaFile = JavaFile.builder("dev.turtywurty.pepolang.parser", baseType)
                .skipJavaLangImports(true)
                .indent("    ")
                .addFileComment("This file is automatically generated. Do not modify.")
                .build();

        javaFile.writeTo(System.out);
        javaFile.writeTo(Paths.get(outputDir));
    }

    private static TypeSpec defineType(String className, String baseName, LinkedHashMap<String, TypeName> fields) {
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        for (Map.Entry<String, TypeName> field : fields.entrySet()) {
            constructor.addParameter(field.getValue(), field.getKey());
            constructor.addStatement("this.$L = $L", field.getKey(), field.getKey());
        }

        MethodSpec accept = MethodSpec.methodBuilder("accept")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addTypeVariable(TypeVariableName.get("R"))
                .addParameter(ClassName.get("dev.turtywurty.pepolang.parser", baseName + "Visitor<R>"), "visitor")
                .returns(TypeVariableName.get("R"))
                .addStatement("return visitor.visit$L(this)", className)
                .build();

        TypeSpec.Builder clazz = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addMethod(constructor.build())
                .addMethod(accept)
                .superclass(ClassName.get("dev.turtywurty.pepolang.parser", baseName));

        for (Map.Entry<String, TypeName> field : fields.entrySet()) {
            clazz.addField(field.getValue(), field.getKey(), Modifier.PRIVATE, Modifier.FINAL);
            clazz.addMethod(MethodSpec.methodBuilder("get" + StringUtility.capitalize(field.getKey()))
                    .addModifiers(Modifier.PUBLIC)
                    .returns(field.getValue())
                    .addStatement("return this.$L", field.getKey())
                    .build());
        }

        return clazz.build();
    }
}
