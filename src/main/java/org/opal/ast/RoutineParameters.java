package org.opal.ast;

import org.opal.Token;
import org.opal.Visitor;

public class RoutineParameters extends AstNode {

  public RoutineParameters () {
    super();
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

}
