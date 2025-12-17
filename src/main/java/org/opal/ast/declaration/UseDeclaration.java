package org.opal.ast.declaration;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class UseDeclaration extends Declaration {

  // Not sure if this is used anymore?
  private Kind kind = null;

  private UseQualifiedName qualifiedName = null;

  public UseDeclaration (Token token) {
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

  public void setQualifiedName (UseQualifiedName qualifiedName) {
    this.qualifiedName = qualifiedName;
  }

  public UseQualifiedName qualifiedName () {
    return qualifiedName;
  }

  // The second child may constitute one, some, or all names.

  // Not sure if these are used anymore?
  public AstNode useOneName () {
    return getChild(1);
  }

  public AstNode useSomeNames () {
    return getChild(1);
  }

  public AstNode useAllNames () {
    return getChild(1);
  }

  public void setKind (UseDeclaration.Kind kind) {
    this.kind = kind;
  }

  public UseDeclaration.Kind getKind () {
    return kind;
  }

  public enum Kind {
    ONE_NAME,
    SOME_NAMES,
    ALL_NAMES
  }

}
