package org.opal.ast.statement;

import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class UntilStatement extends AstNode {

  public UntilStatement (Token token) {
    super(token);
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

}
