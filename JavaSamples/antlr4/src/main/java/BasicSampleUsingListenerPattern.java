import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.Stack;

public class BasicSampleUsingListenerPattern {
    public static void main(String[] args) {
        CalculatorLexer lexer = new CalculatorLexer(CharStreams.fromString("1 + (2 * 3 - 4 / 2) + 5 ^ 2"));
        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        CalculatorParser parser = new CalculatorParser(commonTokenStream);

        ParseTree tree = parser.expression();
        ParseTreeWalker walker = new ParseTreeWalker();

        NodeListener listener = new NodeListener();
        walker.walk(listener, tree);

        System.out.println(listener.result());
    }

    static class NodeListener extends CalculatorBaseListener {
        Stack<Double> stack = new Stack<>();

        @Override
        public void exitLiteral(CalculatorParser.LiteralContext ctx) {
            stack.push(Double.parseDouble(ctx.NUMBER().getText()));
        }

        @Override
        public void exitPow(CalculatorParser.PowContext ctx) {
            double right = stack.pop();
            double left = stack.pop();

            stack.push(Math.pow(left, right));
        }

        @Override
        public void exitMultdiv(CalculatorParser.MultdivContext ctx) {
            if ("*".equals(ctx.op.getText())) {
                double right = stack.pop();
                double left = stack.pop();

                stack.push(left * right);

            } else if ("/".equals(ctx.op.getText())) {
                double right = stack.pop();
                double left = stack.pop();

                stack.push(left / right);
            }
        }

        @Override
        public void exitPlusminus(CalculatorParser.PlusminusContext ctx) {
            if ("+".equals(ctx.op.getText())) {
                double right = stack.pop();
                double left = stack.pop();

                stack.push(left + right);

            } else if ("-".equals(ctx.op.getText())) {
                double right = stack.pop();
                double left = stack.pop();

                stack.push(left - right);
            }
        }

        public double result() {
            return stack.pop();
        }
    }
}
