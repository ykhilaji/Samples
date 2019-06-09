import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class BasicSampleUsingVisitorPattern {
    public static void main(String[] args) {
        CalculatorLexer lexer = new CalculatorLexer(CharStreams.fromString("1 + (2 * 3 - 4 / 2) + 5 ^ 2"));
        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        CalculatorParser parser = new CalculatorParser(commonTokenStream);
        NodeVisitor visitor = new NodeVisitor();

        double result = visitor.visit(parser.expression());
        System.out.println(result);

    }

    static class NodeVisitor extends CalculatorBaseVisitor<Double> {
        @Override
        public Double visitLiteral(CalculatorParser.LiteralContext ctx) {
            return Double.parseDouble(ctx.NUMBER().getText());
        }

        @Override
        public Double visitPow(CalculatorParser.PowContext ctx) {
            return Math.pow(visit(ctx.left), visit(ctx.right));
        }

        @Override
        public Double visitParentheses(CalculatorParser.ParenthesesContext ctx) {
            return visit(ctx.inner);
        }

        @Override
        public Double visitMultdiv(CalculatorParser.MultdivContext ctx) {
            if ("*".equals(ctx.op.getText())) {
                return visit(ctx.left) * visit(ctx.right);
            } else {
                return visit(ctx.left) / visit(ctx.right);
            }
        }

        @Override
        public Double visitPlusminus(CalculatorParser.PlusminusContext ctx) {
            if ("+".equals(ctx.op.getText())) {
                return visit(ctx.left) + visit(ctx.right);
            } else {
                return visit(ctx.left) - visit(ctx.right);
            }
        }
    }
}
