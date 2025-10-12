package org.opal.ast.declaration;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class ClassDeclaration extends AstNode {

  public ClassDeclaration (Token token) {
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

  public AstNode modifiers () {
    return getChild(1);
  }

  public AstNode className () {
    return getChild(2);
  }

  public AstNode baseClause () {
    return getChild(3);
  }

  public AstNode classBody () {
    return getChild(4);
  }

}
