package org.opal.ast.expression;

import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

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
