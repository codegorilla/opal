package org.opal.ast.type;

import org.opal.ResultVisitor;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class Declarator extends AstNode {

  private AstNode directDeclarator = null;
  private ArrayDeclarators arrayDeclarators = null;
  private PointerDeclarators pointerDeclarators = null;

  public Declarator () {}

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

  @Override
  public <T> T accept (ResultVisitor<T> v) {
    return v.visit(this);
  }

  public AstNode getDirectDeclarator () {
    return directDeclarator;
  }

  public ArrayDeclarators getArrayDeclarators () {
    return arrayDeclarators;
  }

  public PointerDeclarators getPointerDeclarators () {
    return pointerDeclarators;
  }

  public void setDirectDeclarator (AstNode directDeclarator) {
    this.directDeclarator = directDeclarator;
  }

  public void setArrayDeclarators (ArrayDeclarators arrayDeclarators) {
    this.arrayDeclarators = arrayDeclarators;
  }

  public void setPointerDeclarators (PointerDeclarators pointerDeclarators) {
    this.pointerDeclarators = pointerDeclarators;
  }

}
