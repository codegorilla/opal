package org.opal.ast.type;

import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class NominalType extends AstNode {
  public NominalType (Token token) {
    super(token);
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }
}
