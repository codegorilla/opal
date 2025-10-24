package org.opal.ast.expression;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class DeleteExpression extends AstNode {

  private boolean arrayFlag = false;

  public DeleteExpression (Token token) {
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

  public AstNode expression () {
    return getChild(0);
  }

  public void setArrayFlag () {
    arrayFlag = true;
  }

  public boolean getArrayFlag () {
    return arrayFlag;
  }

}
