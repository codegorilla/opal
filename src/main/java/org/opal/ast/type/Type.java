package org.opal.ast.type;

import org.opal.Token;
import org.opal.ast.AstNode;
import org.opal.ResultVisitor;
import org.opal.Visitor;

public abstract class Type extends AstNode {

  private boolean root = false;

  public Type (Token token) {
    super(token);
  }

//  @Override
//  public void accept (Visitor v) {
//    v.visit(this);
//  }
//
//  @Override
//  public <T> T accept (ResultVisitor<T> v) {
//    return v.visit(this);
//  }

  public void setRoot (boolean root) {
    this.root = root;
  }

}
