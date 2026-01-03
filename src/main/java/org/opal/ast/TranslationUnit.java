package org.opal.ast;

import org.opal.ResultVisitor;
import org.opal.Visitor;
import org.opal.ast.declaration.*;
import org.opal.symbol.Scope;

public class TranslationUnit extends AstNode {

  // CHILD NODE FIELDS

  private PackageDeclaration packageDeclaration = null;
  private ImportDeclarations importDeclarations = null;
  private UseDeclarations useDeclarations = null;
  private OtherDeclarations otherDeclarations = null;

  // ATTRIBUTE FIELDS

  // Built-in scope
  private Scope scope;

  // Global scope
  private Scope globalScope;

  // STANDARD METHODS

  public TranslationUnit () {
    super();
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

  @Override
  public <T> T accept (ResultVisitor<T> v) {
    return v.visit(this);
  }

  // CHILD NODE METHODS

  public ImportDeclarations getImportDeclarations () {
    return importDeclarations;
  }

  public OtherDeclarations getOtherDeclarations () {
    return otherDeclarations;
  }

  public PackageDeclaration getPackageDeclaration () {
    return packageDeclaration;
  }

  public UseDeclarations getUseDeclarations () {
    return useDeclarations;
  }

  public void setImportDeclarations (ImportDeclarations importDeclarations) {
    this.importDeclarations = importDeclarations;
  }

  public void setOtherDeclarations (OtherDeclarations otherDeclarations) {
    this.otherDeclarations = otherDeclarations;
  }

  public void setPackageDeclaration (PackageDeclaration packageDeclaration) {
    this.packageDeclaration = packageDeclaration;
  }

  public void setUseDeclarations (UseDeclarations useDeclarations) {
    this.useDeclarations = useDeclarations;
  }

  // ATTRIBUTE METHODS

  public Scope getScope () {
    return scope;
  }

  public void setScope (Scope scope) {
    this.scope = scope;
  }

}
