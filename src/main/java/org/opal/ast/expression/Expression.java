package org.opal.ast.expression;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;
import org.opal.type.Type;

public class Expression extends AstNode {

  private Expression subExpression = null;

  // Attributes
  private Type type = null;

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

  public Type getType () {
    return type;
  }

  public void setType (Type type) {
    this.type = type;
  }

}
