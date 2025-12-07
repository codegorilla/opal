package org.opal.ast.declaration;

import java.util.LinkedList;

import org.opal.ResultVisitor;
import org.opal.Visitor;
import org.opal.ast.AstNode;


public class ImportQualifiedName extends AstNode {

  private final LinkedList<ImportName> children = new LinkedList<>();

  public ImportQualifiedName () {}

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

  @Override
  public <T> T accept (ResultVisitor<T> v) {
    return v.visit(this);
  }

  public Iterable<ImportName> getChildrenX () {
    return children;
  }

  public void addImportName (ImportName importName) {
    children.add(importName);
  }

}
