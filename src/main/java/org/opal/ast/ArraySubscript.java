package org.opal.ast;

import org.opal.Visitor;

public class ArraySubscript extends AstNode {

  public ArraySubscript () {
    super();
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

}
