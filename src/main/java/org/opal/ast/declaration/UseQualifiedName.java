package org.opal.ast.declaration;

import java.util.LinkedList;

import org.opal.ResultVisitor;
import org.opal.Visitor;
import org.opal.ast.AstNode;


public class UseQualifiedName extends AstNode {

  private UseName useName = null;

  public UseQualifiedName () {}

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

  @Override
  public <T> T accept (ResultVisitor<T> v) {
    return v.visit(this);
  }
  
  public UseName useName () {
    return useName;
  }

  public void setUseName (UseName useName) {
    this.useName = useName;
  }

}
