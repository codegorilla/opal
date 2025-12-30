package org.opal.ast.type;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

import java.util.LinkedList;

public class RoutinePointerTypeParameters extends Declarator {

  private final LinkedList<RoutinePointerTypeParameter> children = new LinkedList<>();

  public RoutinePointerTypeParameters (Token token) {
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

  public Iterable<RoutinePointerTypeParameter> children () {
    return children;
  }

  public void addRoutinePointerTypeParameter (RoutinePointerTypeParameter parameter) {
    children.add(parameter);
  }

}
