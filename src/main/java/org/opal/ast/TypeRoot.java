package org.opal.ast;

import org.opal.Visitor;

public class TypeRoot extends AstNode {
  public TypeRoot () {
    super();
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }
}
