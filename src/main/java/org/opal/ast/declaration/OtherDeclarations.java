package org.opal.ast.declaration;

import java.util.LinkedList;

import org.opal.ResultVisitor;
import org.opal.Visitor;
import org.opal.ast.AstNode;
import org.stringtemplate.v4.ST;

public class OtherDeclarations extends AstNode {

  // Leave public for now while experimenting
  public LinkedList<ST> templates;

  private final LinkedList<AstNode> children = new LinkedList<>();

  public OtherDeclarations () {
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

  public Iterable<AstNode> children () {
    return children;
  }

  public void addOtherDeclaration (AstNode otherDeclaration) {
    children.add(otherDeclaration);
  }

}
