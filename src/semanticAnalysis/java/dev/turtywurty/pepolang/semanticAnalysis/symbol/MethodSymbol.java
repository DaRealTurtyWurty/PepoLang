package dev.turtywurty.pepolang.semanticAnalysis.symbol;

import dev.turtywurty.pepolang.lexer.Token;
import dev.turtywurty.pepolang.semanticAnalysis.Either;
import dev.turtywurty.pepolang.semanticAnalysis.PrimitiveType;
import dev.turtywurty.pepolang.semanticAnalysis.SymbolType;

import java.util.List;
import java.util.Objects;

public class MethodSymbol extends Symbol implements HasReturnType {
    private final Either<PrimitiveType, String> returnType;
    private final List<VariableSymbol> parameters;

    public MethodSymbol(String name, PrimitiveType returnType, List<VariableSymbol> parameters) {
        super(SymbolType.METHOD, name);
        this.returnType = Either.left(returnType);
        this.parameters = parameters;
    }

    public MethodSymbol(String name, String returnType, List<VariableSymbol> parameters) {
        super(SymbolType.METHOD, name);
        this.returnType = Either.right(returnType);
        this.parameters = parameters;
    }

    public MethodSymbol(String name, Token returnType, List<VariableSymbol> parameters) {
        super(SymbolType.METHOD, name);
        this.returnType = tokenToEither(returnType);
        this.parameters = parameters;
    }

    @Override
    public Either<PrimitiveType, String> getReturnType() {
        return this.returnType;
    }

    public List<VariableSymbol> getParameters() {
        return this.parameters;
    }

    public boolean matches(List<VariableSymbol> parameters) {
        if (this.parameters.size() != parameters.size())
            return false;

        for (int i = 0; i < this.parameters.size(); i++) {
            if (!this.parameters.get(i).getReturnType().equals(parameters.get(i).getReturnType()))
                return false;
        }

        return true;
    }

    public boolean matches(List<VariableSymbol> parameters, Either<PrimitiveType, String> returnType) {
        return matches(parameters) && Objects.equals(this.returnType, returnType);
    }

    public boolean matches(MethodSymbol methodSymbol) {
        return matches(methodSymbol.getParameters(), methodSymbol.getReturnType());
    }
}
