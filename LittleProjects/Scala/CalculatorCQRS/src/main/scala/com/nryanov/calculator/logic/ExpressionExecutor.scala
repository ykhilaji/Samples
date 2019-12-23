package com.nryanov.calculator.logic

import cats.Id
import cats.effect.Sync
import cats.implicits._
import com.nryanov.calculator.logic.ExpressionExecutor.CalculatorVisitorImpl
import com.nryanov.calculator.{CalculatorBaseVisitor, CalculatorLexer, CalculatorParser}
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}
import org.pure4s.logger4s.LazyLogging
import org.pure4s.logger4s.cats.Logger

class ExpressionExecutor[F[_] : Sync] extends LazyLogging {
  private val visitor = new CalculatorVisitorImpl

  def execute(expression: String): F[Double] = for {
    _ <- Logger[F].info("Calculate expression: ", expression)
    typeLexer = new CalculatorLexer(CharStreams.fromString(expression))
    commonTokenStream = new CommonTokenStream(typeLexer)
    parser = new CalculatorParser(commonTokenStream)
    _ <- Logger[F].info("Execute: ", expression)
    result <- Sync[F].delay(visitor.visit(parser.expression))
    _ <- Logger[F].info("Result: ", result)
  } yield result
}

object ExpressionExecutor {
  def apply[F[_] : Sync](): ExpressionExecutor[F] = new ExpressionExecutor()

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

    override def visitFactorial(ctx: CalculatorParser.FactorialContext): Double = {
      val digit = visit(ctx.base)

      if (digit < 0) {
        throw new ArithmeticException("Negative factorial")
      }

      if (digit == 0) {
        1
      } else {
        // probably, not the best approach
        if (digit.toLong.toDouble - digit == 0) {
          naturalNumberFactorial(digit)
        } else {
          scientificNumberFactorial(digit)
        }
      }
    }

    private def naturalNumberFactorial(d: Double): Double = naturalNumberFactorial0(d.toLong).toDouble

    private def naturalNumberFactorial0(d: Long): Long =
      (1, 1).tailRecM[Id, (Long, Long)] {
        case (state, next) if next <= d => Left((state * next, next + 1))
        case (state, next) if next > d => Right((state, next))
      }._1

    private def scientificNumberFactorial(d: Double): Double = Math.sqrt(2 * Math.PI * d) * Math.pow(d / Math.E, d)

    override def visitErrorNode(node: ErrorNode): Double = {
      throw new IllegalArgumentException("Incorrect expression")
    }
  }

}
