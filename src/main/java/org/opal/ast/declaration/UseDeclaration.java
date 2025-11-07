package org.opal.ast.declaration;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class UseDeclaration extends AstNode {

  private Kind kind = null;

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

  public AstNode useQualifiedName () {
    return getChild(0);
  }

  // The second child may constitute one, some, or all names.

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
