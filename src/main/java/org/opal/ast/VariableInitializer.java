package org.opal.ast;

import org.opal.Visitor;

public class VariableInitializer extends AstNode {
  public VariableInitializer () {
    super();
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }
}
