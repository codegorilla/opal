package org.opal.ast.type;

import java.util.LinkedList;

import org.opal.ResultVisitor;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class ArrayDeclarators extends AstNode {

  private final LinkedList<Declarator> children = new LinkedList<>();

  public ArrayDeclarators () {}

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

  @Override
  public <T> T accept (ResultVisitor<T> v) {
    return v.visit(this);
  }

  public Iterable<Declarator> children () {
    return children;
  }

  public void addArrayDeclarator (Declarator arrayDeclarator) {
    children.add(arrayDeclarator);
  }

}
