package org.opal.ast;

import org.opal.Token;
import org.opal.Visitor;

public class BooleanLiteral extends AstNode {

  public BooleanLiteral (Token token) {
    super(token);
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

}
