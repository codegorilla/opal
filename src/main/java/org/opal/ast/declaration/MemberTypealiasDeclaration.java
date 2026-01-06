package org.opal.ast.declaration;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class MemberTypealiasDeclaration extends Declaration {

  public MemberTypealiasDeclaration (Token token) {
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

  public AstNode accessSpecifier () {
    return getChild(0);
  }

  public boolean hasAccessSpecifier () {
    return getChild(0) != null;
  }

  public AstNode name () {
    return getChild(1);
  }

  public AstNode type () {
    return getChild(2);
  }

}
