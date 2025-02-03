package dev.turtywurty.pepolang.semanticAnalysis.symbol;

import dev.turtywurty.pepolang.semanticAnalysis.Either;
import dev.turtywurty.pepolang.semanticAnalysis.PrimitiveType;

public interface ReturnTypeSymbol {
    Either<PrimitiveType, String> getReturnType();
}
