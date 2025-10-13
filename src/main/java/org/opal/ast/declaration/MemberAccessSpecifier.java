package org.opal.ast.declaration;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class MemberAccessSpecifier extends AstNode {

  public static final int NONE = 0;
  public static final int VARIABLE = 1;
  public static final int ROUTINE = 2;

  int kind = MemberAccessSpecifier.NONE;

  public MemberAccessSpecifier (Token token) {
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

  public int getKind () {
    return kind;
  }

  public void setKind (int kind) {
    this.kind = kind;
  }
}
