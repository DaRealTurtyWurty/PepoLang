// This file is automatically generated. Do not modify.
package dev.turtywurty.pepolang.parser;

import dev.turtywurty.pepolang.JavaGenerated;

@JavaGenerated
public abstract class Statement {
    public abstract <R> R accept(StatementVisitor<R> visitor);

    public static class ExpressionStatement extends Statement {
        private final Expression expression;

        public ExpressionStatement(Expression expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(StatementVisitor<R> visitor) {
            return visitor.visitExpressionStatement(this);
        }

        public Expression getExpression() {
            return this.expression;
        }
    }

    public static class PrintStatement extends Statement {
        private final Expression expression;

        public PrintStatement(Expression expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(StatementVisitor<R> visitor) {
            return visitor.visitPrintStatement(this);
        }

        public Expression getExpression() {
            return this.expression;
        }
    }
}
