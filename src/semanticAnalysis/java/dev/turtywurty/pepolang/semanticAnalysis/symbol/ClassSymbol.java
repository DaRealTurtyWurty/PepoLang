package dev.turtywurty.pepolang.semanticAnalysis.symbol;

import dev.turtywurty.pepolang.semanticAnalysis.Either;
import dev.turtywurty.pepolang.semanticAnalysis.PrimitiveType;
import dev.turtywurty.pepolang.semanticAnalysis.SymbolType;

import java.util.List;

public class ClassSymbol extends Symbol {
    private final List<VariableSymbol> staticFields;
    private final List<MethodSymbol> staticMethods;
    private final List<VariableSymbol> fields;
    private final List<MethodSymbol> methods;
    private final List<MethodSymbol> constructors;

    public ClassSymbol(String name, List<VariableSymbol> staticFields, List<MethodSymbol> staticMethods, List<VariableSymbol> fields, List<MethodSymbol> methods, List<MethodSymbol> constructors) {
        super(SymbolType.CLASS, name);
        this.staticFields = staticFields;
        this.staticMethods = staticMethods;
        this.fields = fields;
        this.methods = methods;
        this.constructors = constructors;
    }

    public List<VariableSymbol> getStaticFields() {
        return staticFields;
    }

    public List<MethodSymbol> getStaticMethods() {
        return staticMethods;
    }

    public List<VariableSymbol> getFields() {
        return fields;
    }

    public List<MethodSymbol> getMethods() {
        return methods;
    }

    public List<MethodSymbol> getConstructors() {
        return constructors;
    }

    public MethodSymbol findConstructor(List<Either<PrimitiveType, String>> argumentTypes) {
        for (MethodSymbol constructor : this.constructors) {
            if (!constructor.getName().equals(this.getName()))
                continue;

            List<VariableSymbol> parameters = constructor.getParameters();
            if (parameters.size() == argumentTypes.size()) {
                boolean match = true;
                for (int i = 0; i < argumentTypes.size(); i++) {
                    Either<PrimitiveType, String> expectedType = parameters.get(i).getReturnType();
                    Either<PrimitiveType, String> actualType = argumentTypes.get(i);
                    if (!expectedType.equals(actualType)) {
                        // TODO: Add more sophisticated type checking if you support inheritance/implicit conversions
                        match = false;
                        break;
                    }
                }
                if (match) {
                    return constructor;
                }
            }
        }

        return null;
    }

    public VariableSymbol findField(String name) {
        // Check instance fields
        for (VariableSymbol field : this.fields) {
            if (field.getName().equals(name)) {
                return field;
            }
        }

        // Check static fields (if allowing access via instance, or if context is known)
        for (VariableSymbol staticField : this.staticFields) {
            if (staticField.getName().equals(name)) {
                // Consider if this access is valid (e.g., instance.staticField)
                return staticField;
            }
        }

        // TODO: search in superclass(es)
        return null;
    }

    public MethodSymbol findMethod(String name) {
        // Check instance methods
        for (MethodSymbol method : this.methods) {
            if (method.getName().equals(name)) {
                // For simplicity, returning the first match.
                // If you have method overloading and need to resolve a specific one,
                // this method might need to return a list or take parameter types.
                // However, for `Expression.Get`, you're fetching the method group or a non-overloaded method.
                return method;
            }
        }

        // Check static methods
        for (MethodSymbol staticMethod : this.staticMethods) {
            if (staticMethod.getName().equals(name)) {
                return staticMethod;
            }
        }

        // TODO: search in superclass(es)
        return null;
    }
}
