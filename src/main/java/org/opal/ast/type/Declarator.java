package org.opal.ast.type;

import org.opal.ResultVisitor;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class Declarator extends AstNode {

  public Declarator () {
    super();
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

  @Override
  public <T> T accept (ResultVisitor<T> v) {
    return v.visit(this);
  }

}
