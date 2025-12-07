package org.opal.ast.declaration;

import java.util.LinkedList;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;


public class ImportDeclarations extends AstNode {

  private final LinkedList<ImportDeclaration> importDeclarations = new LinkedList<>();

  public ImportDeclarations () {}

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

  @Override
  public <T> T accept (ResultVisitor<T> v) {
    return v.visit(this);
  }

  public Iterable<ImportDeclaration> importDeclarations () {
    return importDeclarations;
  }

  public void addImportDeclaration (ImportDeclaration importDeclaration) {
    importDeclarations.add(importDeclaration);
  }

}
