package org.opal.type;

import org.opal.Visitor;

public class ArrayType extends Type {

  private Type elementType = null;

  // Size is probably just going to be an AST node pointing to an
  // expression
  private int size = -1;

  public ArrayType () {
    super();
  }

  @Override
  public void accept (TypeVisitor v) {
    v.visit(this);
  }

  public Type getElementType () {
    return elementType;
  }

  public int getSize () {
    return size;
  }

  public void setElementType (Type elementType) {
    this.elementType = elementType;
  }

  public void setSize (int size) {
    this.size = size;
  }

}
