package org.opal.ast;

import org.opal.ResultVisitor;
import org.opal.Visitor;

public class TranslationUnit extends AstNode {

  public TranslationUnit () {
    super();
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

  @Override
  public <T> T accept (ResultVisitor<T> v) {
    return v.visit(this);
  }

  // This needs to be updated
  public AstNode declarations () {
    return getChild(0);
  }

}
