package org.opal.ast.declaration;

import org.opal.Visitor;
import org.opal.ast.AstNode;

public class RoutineBody extends AstNode {

  public RoutineBody() {
    super();
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

}
