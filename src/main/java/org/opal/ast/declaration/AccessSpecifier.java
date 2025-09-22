package org.opal.ast.declaration;

import org.opal.Visitor;
import org.opal.ast.AstNode;

public class AccessSpecifier extends AstNode {

  public AccessSpecifier() {
    super();
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

}
