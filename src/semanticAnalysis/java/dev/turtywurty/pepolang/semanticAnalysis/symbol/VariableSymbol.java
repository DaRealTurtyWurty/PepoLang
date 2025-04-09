package dev.turtywurty.pepolang.semanticAnalysis.symbol;

import dev.turtywurty.pepolang.lexer.Token;
import dev.turtywurty.pepolang.semanticAnalysis.Either;
import dev.turtywurty.pepolang.semanticAnalysis.PrimitiveType;
import dev.turtywurty.pepolang.semanticAnalysis.SymbolType;

public class VariableSymbol extends Symbol implements HasReturnType {
    private final Either<PrimitiveType, String> type;

    public VariableSymbol(String name, PrimitiveType type) {
        super(SymbolType.VARIABLE, name);
        this.type = Either.left(type);
    }

    public VariableSymbol(String name, String type) {
        super(SymbolType.VARIABLE, name);
        this.type = Either.right(type);
    }

    public VariableSymbol(String name, Token token) {
        super(SymbolType.VARIABLE, name);
        this.type = tokenToEither(token);
    }

    @Override
    public Either<PrimitiveType, String> getReturnType() {
        return this.type;
    }
}
