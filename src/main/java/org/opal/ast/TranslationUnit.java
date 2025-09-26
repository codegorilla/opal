package org.opal.ast;

import org.opal.ResultVisitor;
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

  @Override
  public <T> T accept (ResultVisitor<T> v) {
    return v.visit(this);
  }

  public AstNode packageDeclaration () {
    return getChild(0);
  }

  public AstNode importDeclarations () {
    return getChild(1);
  }

  public AstNode declarations () {
    return getChild(2);
  }

}
