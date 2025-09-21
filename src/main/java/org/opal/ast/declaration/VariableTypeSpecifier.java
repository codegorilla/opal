package org.opal.ast.declaration;

import org.opal.Visitor;
import org.opal.ast.AstNode;

public class VariableTypeSpecifier extends AstNode {
  public VariableTypeSpecifier() {
    super();
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }
}
