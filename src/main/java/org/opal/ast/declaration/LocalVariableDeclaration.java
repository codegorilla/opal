package org.opal.ast.declaration;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class LocalVariableDeclaration extends AstNode {

  public LocalVariableDeclaration (Token token) {
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

  public boolean hasTypeSpecifier () {
    return getChild(2) != null;
  }

  public boolean hasInitializer () {
    return getChild(3) != null;
  }

  public AstNode modifiers () {
    return getChild(0);
  }

  public AstNode name () {
    return getChild(1);
  }

  public AstNode typeSpecifier () {
    return getChild(2);
  }

  public AstNode initializer () {
    return getChild(3);
  }

}
