package org.opal.ast.type;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class RoutinePointerType extends Declarator {

  RoutinePointerTypeParameters routinePointerTypeParameters = null;

  public RoutinePointerType (Token token) {
    super(token);
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

  @Override
  public <T> T accept (ResultVisitor<T> v) {
    return v.visit(this);
  }

  public void setRoutinePointerTypeParameters (RoutinePointerTypeParameters parameters) {
    routinePointerTypeParameters = parameters;
  }

}
