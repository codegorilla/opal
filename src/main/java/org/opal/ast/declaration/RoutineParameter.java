package org.opal.ast.declaration;

import org.opal.ResultVisitor;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class RoutineParameter extends AstNode {

  public RoutineParameter () {
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

  public AstNode routineParameterName () {
    return getChild(0);
  }

  public AstNode routineParameterTypeSpecifier () {
    return getChild(1);
  }

}
