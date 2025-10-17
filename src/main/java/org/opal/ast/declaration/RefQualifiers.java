package org.opal.ast.declaration;

import org.opal.ResultVisitor;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class RefQualifiers extends AstNode {

  public RefQualifiers () {
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

  public Iterable<AstNode> getQualifiers () {
    return getChildren();
  }

}
