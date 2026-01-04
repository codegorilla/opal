package org.opal.ast.type;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;
import org.opal.type.Type;

public class Declarator extends AstNode {

  // CHILD NODE FIELDS

  private Declarator directDeclarator = null;
  private ArrayDeclarators arrayDeclarators = null;
  private PointerDeclarators pointerDeclarators = null;

  // ATTRIBUTE FIELDS

  // Type attribute
  Type type = null;

  // STANDARD METHODS

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

  // CHILD NODE METHODS

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

  // ATTRIBUTE METHODS

  public Type getType () {
    return type;
  }

  public void setType (Type type) {
    this.type = type;
  }

}
