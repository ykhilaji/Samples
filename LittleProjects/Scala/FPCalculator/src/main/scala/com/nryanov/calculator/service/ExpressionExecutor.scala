package com.nryanov.calculator.service

import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}
import org.pure4s.logger4s.LazyLogging
import org.pure4s.logger4s.cats.Logger
import com.nryanov.calculator.{CalculatorBaseVisitor, CalculatorLexer, CalculatorParser}
import com.nryanov.calculator.service.ExpressionExecutor.CalculatorVisitorImpl
import cats.effect.Sync
import cats.implicits._

class ExpressionExecutor extends LazyLogging {
  private val visitor = new CalculatorVisitorImpl

  def execute[F[_] : Sync](expression: String): F[Double] = for {
    _ <- Logger[F].info("Calculate expression: ", expression)
    typeLexer = new CalculatorLexer(CharStreams.fromString(expression))
    commonTokenStream = new CommonTokenStream(typeLexer)
    parser = new CalculatorParser(commonTokenStream)
    _ <- Logger[F].debug("Execute: ", expression)
    result <- Sync[F].delay(visitor.visit(parser.expression))
    _ <- Logger[F].debug("Result: ", result)
  } yield result
}

object ExpressionExecutor {
  def apply(): ExpressionExecutor = new ExpressionExecutor()

  class CalculatorVisitorImpl extends CalculatorBaseVisitor[Double] {
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
      throw new IllegalArgumentException("Incorrect expression")
    }
  }

}
