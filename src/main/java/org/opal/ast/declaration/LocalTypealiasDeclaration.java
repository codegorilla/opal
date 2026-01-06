package org.opal.ast.declaration;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;
import org.opal.ast.statement.Statement;

// This is actually a statement because it is a declaration statement

public class LocalTypealiasDeclaration extends Statement {

  public LocalTypealiasDeclaration (Token token) {
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

  public AstNode name () {
    return getChild(0);
  }

  public AstNode type () {
    return getChild(1);
  }

}
