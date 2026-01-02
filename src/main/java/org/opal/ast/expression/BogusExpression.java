package org.opal.ast.expression;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.type.Declarator;

// Not sure if we really need to capture a token or not

// I don't think we will be using bogus expression nodes. (Might not use bogus
// nodes at all.)

@ Deprecated
public class BogusExpression extends Expression {

  public BogusExpression (Token token) {
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

}
