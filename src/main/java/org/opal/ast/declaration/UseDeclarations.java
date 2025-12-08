package org.opal.ast.declaration;

import java.util.LinkedList;

import org.opal.ResultVisitor;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class UseDeclarations extends AstNode {

  private final LinkedList<UseDeclaration> children = new LinkedList<>();

  public UseDeclarations () {}

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

  @Override
  public <T> T accept (ResultVisitor<T> v) {
    return v.visit(this);
  }

  public Iterable<UseDeclaration> children () {
    return children;
  }

  public void addUseDeclaration (UseDeclaration useDeclaration) {
    children.add(useDeclaration);
  }

}
