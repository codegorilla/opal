package org.opal.ast;

import org.opal.Token;
import org.opal.Visitor;

public class ImportDeclaration extends AstNode {

  public ImportDeclaration (Token token) {
    super(token);
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }
}
