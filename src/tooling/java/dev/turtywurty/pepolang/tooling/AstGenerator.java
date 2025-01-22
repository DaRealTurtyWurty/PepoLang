package dev.turtywurty.pepolang.tooling;

import com.palantir.javapoet.*;
import dev.turtywurty.pepolang.CollectionUtility;
import dev.turtywurty.pepolang.JavaGenerated;
import dev.turtywurty.pepolang.StringUtility;
import dev.turtywurty.pepolang.lexer.Token;

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

        TreeMap<String, TreeMap<String, TypeName>> expressionTypes = CollectionUtility.createTreeMap(
                "Assign", CollectionUtility.createTreeMap(
                        "name", TypeName.get(Token.class),
                        "value", expression
                ),
                "Binary", CollectionUtility.createTreeMap(
                        "left", expression,
                        "operator", TypeName.get(Token.class),
                        "right", expression
                ),
                "Call", CollectionUtility.createTreeMap(
                        "callee", expression,
                        "paren", TypeName.get(Token.class),
                        "arguments", ParameterizedTypeName.get(ClassName.get(List.class), expression)
                ),
                "Grouping", CollectionUtility.createTreeMap(
                        "expression", expression
                ),
                "Literal", CollectionUtility.createTreeMap(
                        "value", TypeName.get(Object.class)
                ),
                "Logical", CollectionUtility.createTreeMap(
                        "left", expression,
                        "operator", TypeName.get(Token.class),
                        "right", expression
                ),
                "Unary", CollectionUtility.createTreeMap(
                        "operator", TypeName.get(Token.class),
                        "right", expression
                ),
                "Variable", CollectionUtility.createTreeMap(
                        "name", TypeName.get(Token.class)
                ),
                "Function", CollectionUtility.createTreeMap(
                        "name", TypeName.get(Token.class)
                )
        );

        TreeMap<String, TreeMap<String, TypeName>> statementTypes = CollectionUtility.createTreeMap(
                "BlockStatement", CollectionUtility.createTreeMap(
                        "statements", ParameterizedTypeName.get(ClassName.get(List.class), statement)
                ),
                "FunctionStatement", CollectionUtility.createTreeMap(
                        "name", TypeName.get(Token.class),
                        "returnType", TypeName.get(Token.class),
                        "parameters", ParameterizedTypeName.get(ClassName.get(Map.class), TypeName.get(Token.class), TypeName.get(Token.class)),
                        "body", ParameterizedTypeName.get(ClassName.get(List.class), statement)
                ),
                "ExpressionStatement", CollectionUtility.createTreeMap(
                        "expression", expression
                ),
                "IfStatement", CollectionUtility.createTreeMap(
                        "condition", expression,
                        "thenBranch", statement,
                        "elseBranch", statement
                ),
                "VariableStatement", CollectionUtility.createTreeMap(
                        "type", TypeName.get(Token.class),
                        "name", TypeName.get(Token.class),
                        "initializer", expression
                ),
                "AssignStatement", CollectionUtility.createTreeMap(
                        "name", TypeName.get(Token.class),
                        "value", expression
                ),
                "WhileStatement", CollectionUtility.createTreeMap(
                        "condition", expression,
                        "body", statement
                ),
                "BreakStatement", CollectionUtility.createTreeMap(),
                "ContinueStatement", CollectionUtility.createTreeMap(),
                "ReturnStatement", CollectionUtility.createTreeMap(
                        "keyword", TypeName.get(Token.class),
                        "value", expression
                )
        );

        defineAst(outputDir, "Expression", expressionTypes);
        defineVisitor(outputDir, "Expression", expressionTypes);
        defineAst(outputDir, "Statement", statementTypes);
        defineVisitor(outputDir, "Statement", statementTypes);
    }

    private static void defineVisitor(String outputDir, String baseName, TreeMap<String, TreeMap<String, TypeName>> types) throws IOException {
        TypeVariableName r = TypeVariableName.get("R");

        TypeSpec.Builder visitor = TypeSpec.interfaceBuilder(baseName + "Visitor")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(r)
                .addAnnotation(JavaGenerated.class);

        for (String type : types.sequencedKeySet()) {
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

    private static void defineAst(String outputDir, String name, TreeMap<String, TreeMap<String, TypeName>> types) throws IOException {
        List<TypeSpec> classes = new ArrayList<>();
        for (Map.Entry<String, TreeMap<String, TypeName>> entry : types.sequencedEntrySet()) {
            String className = entry.getKey();
            TreeMap<String, TypeName> fields = entry.getValue();
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

    private static TypeSpec defineType(String className, String baseName, TreeMap<String, TypeName> fields) {
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        for (Map.Entry<String, TypeName> field : fields.sequencedEntrySet()) {
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

        for (Map.Entry<String, TypeName> field : fields.sequencedEntrySet()) {
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
