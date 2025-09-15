package org.opal.ast;

import org.opal.Visitor;

public class VariableTypeSpecifier extends AstNode {
  public VariableTypeSpecifier() {
    super();
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }
}
