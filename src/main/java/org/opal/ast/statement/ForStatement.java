package org.opal.ast.statement;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class ForStatement extends AstNode {

  public ForStatement (Token token) {
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

  public AstNode forBody () {
    return getChild(2);
  }

}
