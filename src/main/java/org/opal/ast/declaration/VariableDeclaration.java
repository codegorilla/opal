package org.opal.ast.declaration;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class VariableDeclaration extends AstNode {

  public VariableDeclaration (Token token) {
    super(token);
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

  @Override
  public <T> T accept (ResultVisitor<T> v) {
    return v.visit(this);
  }

  public AstNode exportSpecifier () {
    return getChild(0);
  }

  public boolean hasExportSpecifier () {
    return getChild(0) != null;
  }

  public boolean hasTypeSpecifier () {
    return getChild(3) != null;
  }

  public boolean hasInitializer () {
    return getChild(4) != null;
  }
  
  public AstNode modifiers () {
    return getChild(1);
  }

  public AstNode name () {
    return getChild(2);
  }

  public AstNode typeSpecifier () {
    return getChild(3);
  }

  public AstNode initializer () {
    return getChild(4);
  }

}
