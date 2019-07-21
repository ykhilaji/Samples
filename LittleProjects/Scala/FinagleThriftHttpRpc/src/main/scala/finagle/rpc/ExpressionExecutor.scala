package finagle.rpc

import org.antlr.v4.runtime.tree.ErrorNode
import org.apache.logging.log4j.LogManager
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

object ExpressionExecutor {
  private val logger = LogManager.getLogger(classOf[CalculatorVisitorImpl])
  private val visitor = new CalculatorVisitorImpl

  def execute(expression: String): Double = {
    logger.info("Execute query: {}", expression)
    val typeLexer: CalculatorLexer = new CalculatorLexer(CharStreams.fromString(expression))
    val commonTokenStream: CommonTokenStream = new CommonTokenStream(typeLexer)
    val parser: CalculatorParser = new CalculatorParser(commonTokenStream)

    visitor.visit(parser.expression)
  }

  private class CalculatorVisitorImpl extends CalculatorBaseVisitor[Double] {
    override def visitQuery(ctx: CalculatorParser.QueryContext): Double = visit(ctx.expression())

    override def visitCosOrSin(ctx: CalculatorParser.CosOrSinContext): Double = if (ctx.COS() != null) {
      Math.cos(visit(ctx.inner))
    } else {
      Math.sin(visit(ctx.inner))
    }

    override def visitDigit(ctx: CalculatorParser.DigitContext): Double = if (ctx.MINUS() != null) {
      -ctx.SCIENTIFIC_NUMBER().getText.toDouble
    } else {
      ctx.SCIENTIFIC_NUMBER().getText.toDouble
    }

    override def visitLog(ctx: CalculatorParser.LogContext): Double = Math.log(visit(ctx.exponent)) / Math.log(ctx.root.getText.toDouble)

    override def visitSqrt(ctx: CalculatorParser.SqrtContext): Double = Math.sqrt(visit(ctx.inner))

    override def visitPlusOrMinus(ctx: CalculatorParser.PlusOrMinusContext): Double = if (ctx.MINUS() != null) {
      visit(ctx.left) - visit(ctx.right)
    } else {
      visit(ctx.left) + visit(ctx.right)
    }

    override def visitPow(ctx: CalculatorParser.PowContext): Double = Math.pow(visit(ctx.digit), visit(ctx.exponent))

    override def visitParentheses(ctx: CalculatorParser.ParenthesesContext): Double = visit(ctx.inner)

    override def visitMultOrDiv(ctx: CalculatorParser.MultOrDivContext): Double = if (ctx.DIV() != null) {
      visit(ctx.left) / visit(ctx.right)
    } else {
      visit(ctx.left) * visit(ctx.right)
    }

    override def visitErrorNode(node: ErrorNode): Double = {
      logger.error("Incorrect expression")
      throw new IllegalArgumentException("Incorrect expression")
    }
  }

}
