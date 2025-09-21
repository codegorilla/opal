package org.opal.ast.declaration;

import org.opal.Visitor;
import org.opal.ast.AstNode;

public class RoutineParameter extends AstNode {

  public RoutineParameter () {
    super();
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

}
