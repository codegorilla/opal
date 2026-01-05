package org.opal.type;

public class Type {

  private final Type.Kind kind;

  public Type (Type.Kind kind) {
    this.kind = kind;
  }

  public Type.Kind getKind () {
    return  kind;
  }

  public enum Kind {
    ARRAY,
    NOMINAL,
    PRIMITIVE,
    POINTER,
  }

}
