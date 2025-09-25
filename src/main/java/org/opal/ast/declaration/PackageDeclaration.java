package org.opal.ast.declaration;

import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class PackageDeclaration extends AstNode {

  public PackageDeclaration(Token token) {
    super(token);
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

  public AstNode getPackageName () {
    return getChild(0);
  }

}
