package org.opal.ast.declaration;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class UseName extends AstNode {

  // I don't have a better name for this and it can be one of several kinds of
  // nodes. Just use AstNode type for now and use generic name "child".

  private AstNode child = null;

  public UseName (Token token) {
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

  public AstNode getChild () {
    return child;
  }
  
  public void setChild (AstNode child) {
    this.child = child;
  }

}
