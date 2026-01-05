package org.opal.type;

import org.opal.ast.expression.Expression;

public class ArrayType extends Type {

  private Type elementType;
  private Expression size;

  public ArrayType () {
    super(Type.Kind.ARRAY);
  }

  public Type getElementType () {
    return elementType;
  }

  public Expression getSize () {
    return size;
  }

  public void setElementType (Type type) {
    this.elementType = type;
  }

  public void setSize (Expression size) {
    this.size = size;
  }

  public String toString () {
    return "array of " + elementType;
  }

}
