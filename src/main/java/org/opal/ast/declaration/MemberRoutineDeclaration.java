package org.opal.ast.declaration;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class MemberRoutineDeclaration extends AstNode {

  public MemberRoutineDeclaration (Token token) {
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

  public AstNode accessSpecifier () {
    return getChild(0);
  }

  public boolean hasAccessSpecifier () {
    return getChild(0) != null;
  }

  public boolean hasRoutineReturnType () {
    return getChild(4) != null;
  }

  public AstNode modifiers () {
    return getChild(1);
  }

  public AstNode routineName () {
    return getChild(2);
  }

  public AstNode routineParameters () {
    return getChild(3);
  }

  public AstNode routineReturnType () {
    return getChild(4);
  }

  public AstNode routineBody () {
    return getChild(5);
  }

}
