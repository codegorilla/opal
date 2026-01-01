package org.opal.ast.declaration;

import org.opal.ResultVisitor;
import org.opal.Visitor;
import org.opal.ast.AstNode;

import java.util.LinkedList;

public class RoutineModifiers extends AstNode {

  private final LinkedList<Modifier> children = new LinkedList<>();

  public RoutineModifiers () {
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

  public Iterable<Modifier> children () {
    return children;
  }

  public void addModifier (Modifier modifier) {
    children.add(modifier);
  }

}
