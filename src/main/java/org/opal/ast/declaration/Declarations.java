package org.opal.ast.declaration;

import org.opal.Visitor;
import org.opal.ast.AstNode;

public class Declarations extends AstNode {

  public Declarations() {
    super();
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

}
