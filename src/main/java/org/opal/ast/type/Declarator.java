package org.opal.ast.type;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;
import org.opal.type.Type;

public class Declarator extends AstNode {

  private Declarator directDeclarator = null;
  private ArrayDeclarators arrayDeclarators = null;
  private PointerDeclarators pointerDeclarators = null;

  public Declarator () {}

  // When should it take a token? I don't think it should ever take a token.

  public Declarator (Token token) {
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

  public ArrayDeclarators getArrayDeclarators () {
    return arrayDeclarators;
  }

  public Declarator getDirectDeclarator () {
    return directDeclarator;
  }

  public PointerDeclarators getPointerDeclarators () {
    return pointerDeclarators;
  }

  public void setArrayDeclarators (ArrayDeclarators arrayDeclarators) {
    this.arrayDeclarators = arrayDeclarators;
  }

  public void setDirectDeclarator (Declarator directDeclarator) {
    this.directDeclarator = directDeclarator;
  }

  public void setPointerDeclarators (PointerDeclarators pointerDeclarators) {
    this.pointerDeclarators = pointerDeclarators;
  }

}
