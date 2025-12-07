package org.opal.ast.declaration;

import java.util.LinkedList;

import org.opal.ResultVisitor;
import org.opal.Visitor;
import org.opal.ast.AstNode;


public class UseNameGroup extends AstNode {

  private final LinkedList<UseName> children = new LinkedList<>();

  public UseNameGroup () {}

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

  @Override
  public <T> T accept (ResultVisitor<T> v) {
    return v.visit(this);
  }

  public Iterable<UseName> children () {
    return children;
  }

  public void addUseName (UseName useName) {
    children.add(useName);
  }


}
