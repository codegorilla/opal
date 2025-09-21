package org.opal.ast.declaration;

import org.opal.Visitor;
import org.opal.ast.AstNode;

public class Modifiers extends AstNode {

  public Modifiers() {
    super();
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

}
