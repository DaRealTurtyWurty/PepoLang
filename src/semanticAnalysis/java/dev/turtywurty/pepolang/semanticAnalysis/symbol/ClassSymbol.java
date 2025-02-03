package dev.turtywurty.pepolang.semanticAnalysis.symbol;

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
}
