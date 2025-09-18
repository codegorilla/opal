package org.opal.ast;

import org.opal.Token;
import org.opal.Visitor;

public class Arguments extends AstNode {

  public Arguments () {
    super();
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

}
