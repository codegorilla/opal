package org.opal.type;

public class PointerType extends Type {

  private Type pointeeType;

  public PointerType () {
    super(Kind.POINTER);
  }

  public Type getPointeeType () {
    return pointeeType;
  }

  public void setPointeeType (Type type) {
    this.pointeeType = type;
  }

}
