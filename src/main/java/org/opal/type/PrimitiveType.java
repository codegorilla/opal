package org.opal.type;

import org.opal.symbol.Symbol;

public class PrimitiveType extends Type {

  private PrimitiveType.Kind kind;

  private Symbol symbol;

  public static final PrimitiveType BOOL    = new PrimitiveType();
  public static final PrimitiveType FLOAT   = new PrimitiveType();
  public static final PrimitiveType FLOAT32 = new PrimitiveType();
  public static final PrimitiveType FLOAT64 = new PrimitiveType();
  public static final PrimitiveType INT     = new PrimitiveType();
  public static final PrimitiveType INT8    = new PrimitiveType();
  public static final PrimitiveType INT16   = new PrimitiveType();
  public static final PrimitiveType INT32   = new PrimitiveType();
  public static final PrimitiveType INT64   = new PrimitiveType();
  public static final PrimitiveType UINT    = new PrimitiveType();
  public static final PrimitiveType UINT8   = new PrimitiveType();
  public static final PrimitiveType UINT16  = new PrimitiveType();
  public static final PrimitiveType UINT32  = new PrimitiveType();
  public static final PrimitiveType UINT64  = new PrimitiveType();
  public static final PrimitiveType VOID  = new PrimitiveType();

  public PrimitiveType () {
    super();
  }

  public PrimitiveType.Kind getKind () {
    return kind;
  }

  public void setKind (PrimitiveType.Kind kind) {
    this.kind = kind;
  }

  public Symbol getSymbol () {
    return symbol;
  }

  public void setSymbol (Symbol symbol) {
    this.symbol = symbol;
  }

  public enum Kind {
    BOOL,
    INT,
    FLOAT
  }
}
