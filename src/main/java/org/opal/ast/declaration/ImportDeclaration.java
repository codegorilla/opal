package org.opal.ast.declaration;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class ImportDeclaration extends AstNode {

  public ImportDeclaration (Token token) {
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

  public AstNode qualifiedName () {
    return getChild(0);
  }

  public boolean hasAsName () {
    return getChild(1) != null;
  }

  public AstNode asName () {
    return getChild(1);
  }

}
