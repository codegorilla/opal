package org.opal.ast.declaration;

import java.util.LinkedList;

import org.opal.ResultVisitor;
import org.opal.Visitor;
import org.opal.ast.AstNode;


public class ImportDeclarations extends AstNode {

  private final LinkedList<ImportDeclaration> children = new LinkedList<>();

  public ImportDeclarations () {}

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

  @Override
  public <T> T accept (ResultVisitor<T> v) {
    return v.visit(this);
  }

  public Iterable<ImportDeclaration> children () {
    return children;
  }

  public void addImportDeclaration (ImportDeclaration importDeclaration) {
    children.add(importDeclaration);
  }

}
