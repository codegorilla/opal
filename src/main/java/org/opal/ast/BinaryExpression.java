package org.opal.ast;

import org.opal.Token;
import org.opal.Visitor;

public class BinaryExpression extends AstNode {

  public BinaryExpression (Token token) {
    super(token);
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

  public AstNode getLeft () {
    return getChild(0);
  }

  public AstNode getRight () {
    return getChild(1);
  }

}
