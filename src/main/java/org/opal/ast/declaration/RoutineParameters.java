package org.opal.ast.declaration;

import org.opal.ResultVisitor;
import org.opal.Visitor;
import org.opal.ast.AstNode;

import java.util.LinkedList;

public class RoutineParameters extends AstNode {

  private final LinkedList<RoutineParameter> children = new LinkedList<>();

  public RoutineParameters () {
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

  public Iterable<RoutineParameter> children () {
    return children;
  }

  public void addParameter (RoutineParameter parameter) {
    children.add(parameter);
  }

}
