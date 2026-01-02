package org.opal.ast.expression;

import org.opal.ResultVisitor;
import org.opal.Visitor;

// Converts its operand type to int64 or uint64. This node is only ever created
// during semantic analysis. It is not created as a result of parsing.

public class ImplicitConvertExpression extends Expression {

  private Expression operand = null;

  public ImplicitConvertExpression () {}

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

  @Override
  public <T> T accept (ResultVisitor<T> v) {
    return v.visit(this);
  }

  public Expression getOperand () {
    return operand;
  }

  public void setOperand (Expression operand) {
    this.operand = operand;
  }

}
