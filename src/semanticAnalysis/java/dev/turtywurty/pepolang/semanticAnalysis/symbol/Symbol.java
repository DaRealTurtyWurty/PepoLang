package dev.turtywurty.pepolang.semanticAnalysis.symbol;

import dev.turtywurty.pepolang.lexer.Token;
import dev.turtywurty.pepolang.semanticAnalysis.Either;
import dev.turtywurty.pepolang.semanticAnalysis.PrimitiveType;
import dev.turtywurty.pepolang.semanticAnalysis.SymbolType;

public abstract class Symbol {
    private final SymbolType symbolType;
    private final String name;

    public Symbol(SymbolType symbolType, String name) {
        this.symbolType = symbolType;
        this.name = name;
    }

    public SymbolType getSymbolType() {
        return this.symbolType;
    }

    public String getName() {
        return this.name;
    }

    public static Either<PrimitiveType, String> tokenToEither(Token token) {
        return token.type().isTypeKeyword() ?
                Either.left(PrimitiveType.fromTokenType(token.type())) :
                Either.right((String) token.value());
    }
}