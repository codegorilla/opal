package org.opal.ast;

import org.opal.Visitor;

public class Expression extends AstNode {

  public Expression () {
    super();
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

}
