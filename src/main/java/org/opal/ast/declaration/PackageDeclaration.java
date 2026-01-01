package org.opal.ast.declaration;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;
import org.opal.symbol.Scope;

public class PackageDeclaration extends AstNode {

  // Child nodes
  private PackageName packageName = null;

  // Attributes
  private Scope scope;

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

  public PackageName getPackageName () {
    return packageName;
  }

  public void setPackageName (PackageName packageName) {
    this.packageName = packageName;
  }

  public Scope getScope () {
    return scope;
  }

  public void setScope (Scope scope) {
    this.scope = scope;
  }

}
