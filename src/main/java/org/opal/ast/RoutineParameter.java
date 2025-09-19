package org.opal.ast;

import org.opal.Visitor;

public class RoutineParameter extends AstNode {

  public RoutineParameter () {
    super();
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

}
