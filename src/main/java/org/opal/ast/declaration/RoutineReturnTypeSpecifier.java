package org.opal.ast.declaration;

import org.opal.ResultVisitor;
import org.opal.Visitor;
import org.opal.ast.AstNode;
import org.opal.ast.type.Declarator;

public class RoutineReturnTypeSpecifier extends AstNode {

  private Declarator declarator = null;

  public RoutineReturnTypeSpecifier () {
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

  public Declarator getDeclarator () {
    return declarator;
  }

  public void setDeclarator (Declarator declarator) {
    this.declarator = declarator;
  }

}
