package org.opal.type;

public class PrimitiveType extends Type {

  private PrimitiveType.Kind kind;

  public PrimitiveType () {
    super();
  }

  public PrimitiveType.Kind getKind () {
    return kind;
  }

  public void setKind (PrimitiveType.Kind kind) {
    this.kind = kind;
  }

  public enum Kind {
    BOOL,
    INT,
    FLOAT
  }
}
