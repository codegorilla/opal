package org.opal.ast;

import org.opal.ResultVisitor;
import org.opal.Visitor;
import org.opal.ast.declaration.*;

public class TranslationUnit extends AstNode {

  private PackageDeclaration packageDeclaration;
  private ImportDeclarations importDeclarations;
  private UseDeclarations useDeclarations;
  private OtherDeclarations otherDeclarations;

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

  public ImportDeclarations getImportDeclarations () {
    return importDeclarations;
  }

  public void setImportDeclarations (ImportDeclarations importDeclarations) {
    this.importDeclarations = importDeclarations;
  }


  // This needs to be updated
  public AstNode declarations () {
    return getChild(0);
  }

}
