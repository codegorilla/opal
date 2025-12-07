package org.opal.ast.declaration;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class PackageDeclaration extends AstNode {

  private PackageName packageName = null;

  public PackageDeclaration(Token token) {
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

  public AstNode getPackageName () {
    return packageName;
  }

  public void setPackageName (PackageName packageName) {
    this.packageName = packageName;
  }

}
