package org.opal.ast;

import org.opal.Token;
import org.opal.Visitor;

public class ContinueStatement extends AstNode {

  public ContinueStatement (Token token) {
    super(token);
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }
  
}
