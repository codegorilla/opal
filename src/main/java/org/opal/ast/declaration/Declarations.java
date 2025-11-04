package org.opal.ast.declaration;

import org.opal.ResultVisitor;
import org.opal.Visitor;
import org.opal.ast.AstNode;

import org.stringtemplate.v4.ST;

import java.util.LinkedList;

public class Declarations extends AstNode {

  // Leave public for now while experimenting
  public LinkedList<ST> templates;

  public Declarations() {
    super();
    templates = new LinkedList<>();
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

  @Override
  public <T> T accept (ResultVisitor<T> v) {
    return v.visit(this);
  }

  public AstNode packageDeclaration () {
    return getChild(0);
  }

  public boolean hasImportDeclarations () {
    return getChild(1) != null;
  }

  public AstNode importDeclarations () {
    return getChild(1);
  }

  public boolean hasUseDeclarations () {
    return getChild(2) != null;
  }

  public AstNode useDeclarations () {
    return getChild(2);
  }

  public AstNode otherDeclarations () {
    return getChild(3);
  }

  // Deprecate?
  public Iterable<AstNode> declarations () {
    return getChildren();
  }

}
