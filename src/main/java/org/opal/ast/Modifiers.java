package org.opal.ast;

import org.opal.Visitor;

public class Modifiers extends AstNode {

  public Modifiers() {
    super();
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

}
