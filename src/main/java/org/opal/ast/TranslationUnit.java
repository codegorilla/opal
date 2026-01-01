package org.opal.ast;

import org.opal.ResultVisitor;
import org.opal.Visitor;
import org.opal.ast.declaration.*;
import org.opal.symbol.Scope;

public class TranslationUnit extends AstNode {

  // Child nodes
  private PackageDeclaration packageDeclaration = null;
  private ImportDeclarations importDeclarations = null;
  private UseDeclarations useDeclarations = null;
  private OtherDeclarations otherDeclarations = null;

  // Attributes

  // Built-in scope
  private Scope scope;

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

  public PackageDeclaration packageDeclaration () {
    return packageDeclaration;
  }

  public void setPackageDeclaration (PackageDeclaration packageDeclaration) {
    this.packageDeclaration = packageDeclaration;
  }

  public boolean hasImportDeclarations () {
    return importDeclarations != null;
  }

  public ImportDeclarations importDeclarations () {
    return importDeclarations;
  }

  public void setImportDeclarations (ImportDeclarations importDeclarations) {
    this.importDeclarations = importDeclarations;
  }

  // I think many of these has... declarations are no longer needed since
  // switching to "items... item*" instead of "items?... item+"

//  public boolean hasOtherDeclarations () {
//    return otherDeclarations != null;
//  }

  public OtherDeclarations otherDeclarations () {
    return otherDeclarations;
  }

  public void setOtherDeclarations (OtherDeclarations otherDeclarations) {
    this.otherDeclarations = otherDeclarations;
  }

  public boolean hasUseDeclarations () {
    return useDeclarations != null;
  }

  public UseDeclarations useDeclarations () {
    return useDeclarations;
  }

  public void setUseDeclarations (UseDeclarations useDeclarations) {
    this.useDeclarations = useDeclarations;
  }


  // This needs to be updated
  public AstNode declarations () {
    return getChild(0);
  }

  public Scope getScope () {
    return scope;
  }

  public void setScope (Scope scope) {
    this.scope = scope;
  }

}
