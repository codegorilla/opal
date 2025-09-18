package org.opal.ast;

import org.opal.Token;
import org.opal.Visitor;

public class RoutineCall extends AstNode {

  public RoutineCall (Token token) {
    super(token);
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

}
