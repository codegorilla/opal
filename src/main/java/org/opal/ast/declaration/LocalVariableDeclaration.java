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

  public AstNode modifiers () {
    return getChild(0);
  }

  public AstNode variableName () {
    return getChild(1);
  }

  public AstNode variableTypeSpecifier () {
    return getChild(2);
  }

  public AstNode variableInitializer () {
    return getChild(3);
  }

}
