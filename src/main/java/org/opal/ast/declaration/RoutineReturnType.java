package org.opal.ast.declaration;

import org.opal.Visitor;
import org.opal.ast.AstNode;

public class RoutineReturnType extends AstNode {

  public RoutineReturnType() {
    super();
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

}
