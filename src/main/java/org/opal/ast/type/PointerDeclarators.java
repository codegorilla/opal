package org.opal.ast.type;

import java.util.LinkedList;

import org.opal.ResultVisitor;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class PointerDeclarators extends AstNode {

  private final LinkedList<PointerDeclarator> children = new LinkedList<>();

  public PointerDeclarators () {}

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

  @Override
  public <T> T accept (ResultVisitor<T> v) {
    return v.visit(this);
  }

  public Iterable<PointerDeclarator> children () {
    return children;
  }

  public void addPointerDeclarator (PointerDeclarator pointerDeclarator) {
    children.add(pointerDeclarator);
  }

}
