package org.opal.ast.statement;

import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class IfStatement extends AstNode {

  public IfStatement (Token token) {
    super(token);
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

}
