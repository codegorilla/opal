package org.opal.ast;

import org.opal.Token;
import org.opal.Visitor;

public class VariableDeclaration extends AstNode {

  public VariableDeclaration (Token token) {
    super(token);
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

  public AstNode getAccessSpecifier () {
    return getChild(0);
  }

  public AstNode getModifiers () {
    return getChild(1);
  }

  public AstNode getName () {
    return getChild(2);
  }

  public AstNode getTypeSpecifier () {
    return getChild(3);
  }

  public AstNode getInitializer () {
    return getChild(4);
  }

}
