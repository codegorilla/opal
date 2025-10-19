package org.opal.ast.declaration;

import org.opal.ResultVisitor;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class MemberVariableModifiers extends AstNode {

  public MemberVariableModifiers () {
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

  public Iterable<AstNode> getModifiers () {
    return getChildren();
  }

}
