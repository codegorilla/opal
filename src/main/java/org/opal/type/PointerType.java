package org.opal.type;

public class PointerType extends Type {

  private Type pointeeType = null;

  public PointerType () {
    super();
  }

  public Type getPointeeType () {
    return pointeeType;
  }

  public void setPointeeType (Type pointeeType) {
    this.pointeeType = pointeeType;
  }

}
