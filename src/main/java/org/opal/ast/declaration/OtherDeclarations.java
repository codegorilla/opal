package org.opal.ast.declaration;

import java.util.LinkedList;

import org.opal.ResultVisitor;
import org.opal.Visitor;
import org.opal.ast.AstNode;
import org.stringtemplate.v4.ST;

public class OtherDeclarations extends AstNode {

  // Leave public for now while experimenting
  public LinkedList<ST> templates;

  private final LinkedList<Declaration> otherDeclarations = new LinkedList<>();

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

  public void addOtherDeclaration (Declaration otherDeclaration) {
    otherDeclarations.add(otherDeclaration);
  }

  public Iterable<Declaration> getOtherDeclarations () {
    return otherDeclarations;
  }


}
