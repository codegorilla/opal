package org.opal.ast.declaration;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class ImportDeclaration extends AstNode {

  // Alias as determined from import declaration analysis
  private String aliasAttribute = null;

  private ImportQualifiedName qualifiedName = null;
  private AstNode asName = null;

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

  public void setQualifiedName (ImportQualifiedName qualifiedName) {
    this.qualifiedName = qualifiedName;
  }

  public ImportQualifiedName qualifiedName () {
    return qualifiedName;
  }

  public void setAsName (AstNode asName) {
    this.asName = asName;
  }

  public AstNode asName () {
    return asName;
  }

  public boolean hasAsName () {
    return asName != null;
  }

  public String getAliasAttribute () {
    return aliasAttribute;
  }

  public void setAliasAttribute (String aliasAttribute) {
    this.aliasAttribute = aliasAttribute;
  }

}
