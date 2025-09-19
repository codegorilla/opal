package org.opal.ast;

import org.opal.Token;
import org.opal.Visitor;

public class RoutineDeclaration extends AstNode {

  public RoutineDeclaration (Token token) {
    super(token);
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

  public AstNode getName () {
    return getChild(0);
  }

}
