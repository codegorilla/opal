package org.opal.ast;

import org.opal.Token;
import org.opal.Visitor;

public class ExpressionStatement extends AstNode {

  public ExpressionStatement() {
    super();
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }
  
}
