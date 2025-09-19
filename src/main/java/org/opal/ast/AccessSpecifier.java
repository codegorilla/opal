package org.opal.ast;

import org.opal.Visitor;

public class AccessSpecifier extends AstNode {

  public AccessSpecifier() {
    super();
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

}
