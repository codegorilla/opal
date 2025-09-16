package org.opal.ast;

import org.opal.Visitor;
import org.opal.Token;

public class TranslationUnit extends AstNode {

  public TranslationUnit (Token token) {
    super(token);
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

}
