package org.opal.ast.expression;

import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class This extends AstNode {

  public This (Token token) {
    super(token);
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

}
