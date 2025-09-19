package org.opal.ast;

import org.opal.Visitor;

public class RoutineReturnType extends AstNode {

  public RoutineReturnType() {
    super();
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

}
