package org.opal.ast.declaration;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class TypealiasDeclaration extends Declaration {

  public TypealiasDeclaration (Token token) {
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

  public AstNode name () {
    return getChild(1);
  }

  public AstNode type () {
    return getChild(2);
  }

}
