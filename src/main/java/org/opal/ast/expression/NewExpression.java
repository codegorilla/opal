package org.opal.ast.expression;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class NewExpression extends AstNode {

  public NewExpression (Token token) {
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

  public boolean hasInitializer () {
    return getChild(2) != null;
  }

  public AstNode expression () {
    return getChild(0);
  }

  public AstNode type () {
    return getChild(1);
  }

  public AstNode initializer () {
    return getChild(2);
  }

}
