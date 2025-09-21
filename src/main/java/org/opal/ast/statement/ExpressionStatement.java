package org.opal.ast.statement;

import org.opal.Visitor;
import org.opal.ast.AstNode;

public class ExpressionStatement extends AstNode {

  public ExpressionStatement() {
    super();
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }
  
}
