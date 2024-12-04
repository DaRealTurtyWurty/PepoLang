package dev.turtywurty.pepolang.tooling;

//import dev.turtywurty.pepolang.lexer.Token;
//import dev.turtywurty.pepolang.lexer.TokenType;
//import dev.turtywurty.pepolang.parser.Expression;
//import dev.turtywurty.pepolang.parser.Visitor;

public class AstPrinter/* implements Visitor<String>*/ {
//    public static void main(String[] args) {
//        var expression = new Expression.Binary(
//                new Expression.Unary(
//                        new Token(TokenType.SUB, "-", 1),
//                        new Expression.Literal(123)
//                ),
//                new Token(TokenType.MUL, "*", 1),
//                new Expression.Grouping(
//                        new Expression.Literal(45.67)
//                )
//        );
//
//        System.out.println(AstPrinter.print(expression));
//    }
//
//    public static String print(Expression expr) {
//        return expr.accept(new AstPrinter());
//    }
//
//    @Override
//    public String visitGrouping(Expression.Grouping expression) {
//        return parenthesize("group", expression.getExpression());
//    }
//
//    @Override
//    public String visitLiteral(Expression.Literal expression) {
//        return expression.getValue() == null ? "null" : expression.getValue().toString();
//    }
//
//    @Override
//    public String visitUnary(Expression.Unary expression) {
//        return parenthesize(expression.getOperator().type().name(), expression.getRight());
//    }
//
//    @Override
//    public String visitBinary(Expression.Binary expression) {
//        return parenthesize(expression.getOperator().type().name(), expression.getLeft(), expression.getRight());
//    }
//
//    private String parenthesize(String name, Expression... expressions) {
//        StringBuilder builder = new StringBuilder();
//
//        builder.append("(").append(name);
//        for (Expression expr : expressions) {
//            builder.append(" ");
//            builder.append(expr.accept(this));
//        }
//        builder.append(")");
//
//        return builder.toString();
//    }
}
