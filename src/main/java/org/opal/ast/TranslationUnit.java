package org.opal.ast;

import org.opal.ResultVisitor;
import org.opal.Visitor;
import org.opal.ast.declaration.*;

public class TranslationUnit extends AstNode {

  private PackageDeclaration packageDeclaration = null;
  private ImportDeclarations importDeclarations = null;
  private UseDeclarations useDeclarations = null;
  private OtherDeclarations otherDeclarations = null;

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

  public PackageDeclaration getPackageDeclaration () {
    return packageDeclaration;
  }

  public void setPackageDeclaration (PackageDeclaration packageDeclaration) {
    this.packageDeclaration = packageDeclaration;
  }

  public boolean hasImportDeclarations () {
    return importDeclarations != null;
  }

  public ImportDeclarations getImportDeclarations () {
    return importDeclarations;
  }

  public void setImportDeclarations (ImportDeclarations importDeclarations) {
    this.importDeclarations = importDeclarations;
  }
  
  public boolean hasUseDeclarations () {
    return useDeclarations != null;
  }

  public UseDeclarations getUseDeclarations () {
    return useDeclarations;
  }

  public void setUseDeclarations (UseDeclarations useDeclarations) {
    this.useDeclarations = useDeclarations;
  }


  // This needs to be updated
  public AstNode declarations () {
    return getChild(0);
  }

}
