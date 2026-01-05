package org.opal.ast.declaration;

import org.opal.ResultVisitor;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class RoutineParameter extends AstNode {

  private RoutineParameterName name = null;
  private RoutineParameterTypeSpecifier typeSpecifier = null;

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

  public RoutineParameterName getName () {
    return name;
  }

  public RoutineParameterTypeSpecifier getTypeSpecifier () {
    return typeSpecifier;
  }

  public void setName (RoutineParameterName name) {
    this.name = name;
  }

  public void setTypeSpecifier (RoutineParameterTypeSpecifier typeSpecifier) {
    this.typeSpecifier = typeSpecifier;
  }

}
