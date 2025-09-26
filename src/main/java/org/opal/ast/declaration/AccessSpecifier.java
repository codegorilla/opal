package org.opal.ast.declaration;

import org.opal.ResultVisitor;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class AccessSpecifier extends AstNode {

  AstNode parent = null;

  public AccessSpecifier() {
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

  public AstNode getParent () {
    return parent;
  }

  public void setParent (AstNode parent) {
    this.parent = parent;
  }
}
