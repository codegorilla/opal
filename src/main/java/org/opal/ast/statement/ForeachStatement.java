package org.opal.ast.statement;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class ForeachStatement extends AstNode {

  public ForeachStatement (Token token) {
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

  public AstNode name () {
    return getChild(0);
  }

  public AstNode expression () {
    return getChild(1);
  }

  public AstNode foreachBody () {
    return getChild(2);
  }

}
