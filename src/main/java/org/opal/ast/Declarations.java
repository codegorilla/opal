package org.opal.ast;

import org.opal.Visitor;

public class Declarations extends AstNode {

  public Declarations() {
    super();
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

}
