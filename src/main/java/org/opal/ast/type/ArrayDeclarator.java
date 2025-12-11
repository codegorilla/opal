package org.opal.ast.type;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;
import org.opal.ast.expression.Expression;

public class ArrayDeclarator extends Declarator {

  private Expression expression = null;

  public ArrayDeclarator (Token token) {
    super(token);
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

  @Override
  public <T> T accept (ResultVisitor<T> v) {
    return v.visit(this);
  }

  public Expression getExpression () {
    return expression;
  }

  public void setExpression (Expression expression) {
    this.expression = expression;
  }

}
