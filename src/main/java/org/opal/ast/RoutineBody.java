package org.opal.ast;

import org.opal.Visitor;

public class RoutineBody extends AstNode {

  public RoutineBody() {
    super();
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

}
