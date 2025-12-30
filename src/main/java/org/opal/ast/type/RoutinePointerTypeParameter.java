package org.opal.ast.type;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

import java.util.LinkedList;

public class RoutinePointerTypeParameter extends Declarator {

  Declarator declarator;

  public RoutinePointerTypeParameter () {
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

  public void setDeclarator (Declarator declarator) {
    this.declarator = declarator;
  }

}
