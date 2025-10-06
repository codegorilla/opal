package org.opal.ast.declaration;

import org.opal.Token;
import org.opal.ast.AstNode;
import org.opal.ResultVisitor;
import org.opal.Visitor;

public class VariableTypeSpecifier extends AstNode {

  public VariableTypeSpecifier (Token token) {
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

  public AstNode type () {
    return getChild(0);
  }
}
