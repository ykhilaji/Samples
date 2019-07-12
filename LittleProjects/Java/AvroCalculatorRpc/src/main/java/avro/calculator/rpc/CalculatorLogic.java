package avro.calculator.rpc;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CalculatorLogic {
    private final static Logger logger = LogManager.getLogger(CalculatorLogic.class);

    private static CalculatorVisitor visitor = new CalculatorVisitor();

    public static Double execute(String expression) {
        logger.info("Execute query: {}", expression);
        CalculatorLexer typeLexer = new CalculatorLexer(CharStreams.fromString(expression));
        CommonTokenStream commonTokenStream = new CommonTokenStream(typeLexer);
        CalculatorParser parser = new CalculatorParser(commonTokenStream);

        return visitor.visit(parser.expression());
    }

    static class CalculatorVisitor extends CalculatorBaseVisitor<Double> {
        @Override
        public Double visitQuery(CalculatorParser.QueryContext ctx) {
            return visit(ctx.expression());
        }

        @Override
        public Double visitDigit(CalculatorParser.DigitContext ctx) {
            Double digit = Double.parseDouble(ctx.SCIENTIFIC_NUMBER().getText());

            if (ctx.MINUS() != null) {
                return -digit;
            } else {
                return digit;
            }
        }

        @Override
        public Double visitLog(CalculatorParser.LogContext ctx) {
            double base = Double.parseDouble(ctx.root.getText());
            Double exponent = visit(ctx.exponent);

            return Math.log(exponent) / Math.log(base);
        }

        @Override
        public Double visitPlusOrMinus(CalculatorParser.PlusOrMinusContext ctx) {
            Double left = visit(ctx.left);
            Double right = visit(ctx.right);

            if (ctx.MINUS() != null) {
                return left - right;
            } else {
                return left + right;
            }
        }

        @Override
        public Double visitPow(CalculatorParser.PowContext ctx) {
            Double digit = visit(ctx.digit);
            Double exponent = visit(ctx.exponent);

            return Math.pow(digit, exponent);
        }

        @Override
        public Double visitParentheses(CalculatorParser.ParenthesesContext ctx) {
            return visit(ctx.inner);
        }

        @Override
        public Double visitMultOrDiv(CalculatorParser.MultOrDivContext ctx) {
            Double left = visit(ctx.left);
            Double right = visit(ctx.right);

            if (ctx.DIV() != null) {
                return left / right;
            } else {
                return left * right;
            }
        }

        @Override
        public Double visitErrorNode(ErrorNode node) {
            logger.error("Incorrect query");
            throw new IllegalArgumentException("Incorrect query");
        }
    }
}
