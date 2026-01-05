package org.opal.type;

public class PrimitiveType extends Type {

  private final String text;
  private final int rank;

  public static final Type BOOL    = new PrimitiveType("bool", 1);
  public static final Type FLOAT   = new PrimitiveType("float", 20);
  public static final Type FLOAT32 = new PrimitiveType("float32", 20);
  public static final Type FLOAT64 = new PrimitiveType("float64", 21);
  public static final Type INT     = new PrimitiveType("int", 12);
  public static final Type INT8    = new PrimitiveType("int8", 10);
  public static final Type INT16   = new PrimitiveType("int16", 11);
  public static final Type INT32   = new PrimitiveType("int32", 12);
  public static final Type INT64   = new PrimitiveType("int64", 13);
  public static final Type UINT    = new PrimitiveType("uint", 12);
  public static final Type UINT8   = new PrimitiveType("uint8", 10);
  public static final Type UINT16  = new PrimitiveType("uint16", 11);
  public static final Type UINT32  = new PrimitiveType("uint32", 12);
  public static final Type UINT64  = new PrimitiveType("uint64", 13);
  public static final Type VOID    = new PrimitiveType("void", 0);

  public PrimitiveType (String text, int rank) {
    super(Type.Kind.PRIMITIVE);
    this.text = text;
    this.rank = rank;
  }

  public int getRank () {
    return rank;
  }

  public String getText () {
    return text;
  }

  public String toString () {
    return getText();
  }

}
