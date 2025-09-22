package org.opal.ast.expression;

import org.opal.Visitor;
import org.opal.ast.AstNode;

public class Arguments extends AstNode {

  public Arguments () {
    super();
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

}
