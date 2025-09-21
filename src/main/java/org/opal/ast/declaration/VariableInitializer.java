package org.opal.ast.declaration;

import org.opal.Visitor;
import org.opal.ast.AstNode;

public class VariableInitializer extends AstNode {
  public VariableInitializer () {
    super();
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }
}
