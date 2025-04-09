package dev.turtywurty.pepolang.semanticAnalysis.symbol;

import dev.turtywurty.pepolang.semanticAnalysis.Either;
import dev.turtywurty.pepolang.semanticAnalysis.PrimitiveType;
import dev.turtywurty.pepolang.semanticAnalysis.SymbolType;

public class ValueSymbol extends Symbol implements HasReturnType {
    private final Either<PrimitiveType, String> returnType;

    public ValueSymbol(Either<PrimitiveType, String> returnType) {
        super(SymbolType.VALUE, null);
        this.returnType = returnType;
    }

    @Override
    public Either<PrimitiveType, String> getReturnType() {
        return this.returnType;
    }
}
