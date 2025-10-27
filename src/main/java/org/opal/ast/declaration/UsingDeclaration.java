package org.opal.ast.declaration;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class UsingDeclaration extends AstNode {

  public UsingDeclaration (Token token) {
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

  public AstNode exportSpecifier () {
    return getChild(0);
  }

  public boolean hasExportSpecifier () {
    return getChild(0) != null;
  }

  public AstNode qualifiedName () {
    return getChild(1);
  }

}
