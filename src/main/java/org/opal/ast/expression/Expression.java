package org.opal.ast.expression;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class Expression extends AstNode {

  private Expression subExpression = null;

  public Expression () {}

  public Expression (Token token) {
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

  public boolean hasSubExpression () {
    return subExpression != null;
  }

  public Expression getSubExpression () {
    return subExpression;
  }

  public void setSubExpression (Expression subExpression) {
    this.subExpression = subExpression;
  }


}
