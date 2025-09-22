package org.opal.ast.expression;

import org.opal.Visitor;
import org.opal.ast.AstNode;

public class ArraySubscript extends AstNode {

  public ArraySubscript () {
    super();
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

}
