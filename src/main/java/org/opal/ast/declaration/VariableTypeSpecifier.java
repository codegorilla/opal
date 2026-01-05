package org.opal.ast.declaration;

import org.opal.ast.AstNode;
import org.opal.ResultVisitor;
import org.opal.Visitor;
import org.opal.ast.type.Declarator;
import org.opal.type.Type;

public class VariableTypeSpecifier extends AstNode {

  private Declarator declarator = null;

  // I think I'd rather the type attribute be placed on the declarator node
  // Update: I am not so sure about this. Perhaps it should actually be in the
  // type specifier.

  // Type attribute
  Type type = null;

  public VariableTypeSpecifier () {}

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

  @Override
  public <T> T accept (ResultVisitor<T> v) {
    return v.visit(this);
  }

  public Declarator getDeclarator () {
    return declarator;
  }

  public Type getType () {
    return type;
  }

  public void setDeclarator (Declarator declarator) {
    this.declarator = declarator;
  }

  public void setType (Type type) {
    this.type = type;
  }

}
