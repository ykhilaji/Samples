package com.github.nryanov.calculator

import cats.effect.Sync
import cats.implicits._
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}
import org.antlr.v4.runtime.tree.ErrorNode
import org.slf4j.LoggerFactory

object ExpressionExecutor {
  private val logger = LoggerFactory.getLogger(classOf[CalculatorVisitorImpl])
  private val visitor = new CalculatorVisitorImpl

  def execute[F[_] : Sync](expression: String): F[Double] = for {
    _ <- Sync[F].delay(logger.debug("Create CalculatorLexer"))
    typeLexer <- Sync[F].delay(new CalculatorLexer(CharStreams.fromString(expression)))
    _ <- Sync[F].delay(logger.debug("Create CommonTokenStream"))
    commonTokenStream <- Sync[F].delay(new CommonTokenStream(typeLexer))
    _ <- Sync[F].delay(logger.debug("Create CalculatorParser"))
    parser <- Sync[F].delay(new CalculatorParser(commonTokenStream))
    _ <- Sync[F].delay(logger.info("Execute query: {}", expression))
    result <- Sync[F].delay(visitor.visit(parser.expression))
    _ <- Sync[F].delay(logger.info("Result: {}", result))
  } yield result

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
      val left = visit(ctx.left)
      val right = visit(ctx.right)

      // check it manually, otherwise result will be infinity
      if (right == 0) {
        throw new ArithmeticException("Division by zero")
      }

      left / right
    } else {
      visit(ctx.left) * visit(ctx.right)
    }

    override def visitErrorNode(node: ErrorNode): Double = {
      logger.error("Incorrect expression")
      throw new IllegalArgumentException("Incorrect expression")
    }
  }

}
