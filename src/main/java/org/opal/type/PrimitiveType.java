package org.opal.type;

import org.opal.symbol.Symbol;

public class PrimitiveType extends Type {

  private String text;
  private int rank;

  private Symbol symbol;

  public static final PrimitiveType BOOL    = new PrimitiveType("bool", 1);
  public static final PrimitiveType FLOAT   = new PrimitiveType("float", 20);
  public static final PrimitiveType FLOAT32 = new PrimitiveType("float32", 20);
  public static final PrimitiveType FLOAT64 = new PrimitiveType("float64", 21);
  public static final PrimitiveType INT     = new PrimitiveType("int", 12);
  public static final PrimitiveType INT8    = new PrimitiveType("int8", 10);
  public static final PrimitiveType INT16   = new PrimitiveType("int16", 11);
  public static final PrimitiveType INT32   = new PrimitiveType("int32", 12);
  public static final PrimitiveType INT64   = new PrimitiveType("int64", 13);
  public static final PrimitiveType UINT     = new PrimitiveType("uint", 12);
  public static final PrimitiveType UINT8    = new PrimitiveType("uint8", 10);
  public static final PrimitiveType UINT16   = new PrimitiveType("uint16", 11);
  public static final PrimitiveType UINT32   = new PrimitiveType("uint32", 12);
  public static final PrimitiveType UINT64   = new PrimitiveType("uint64", 13);
  public static final PrimitiveType VOID  = new PrimitiveType("void", 0);

  public PrimitiveType (String text, int rank) {
    super();
    this.text = text;
    this.rank = rank;
  }

  @Override
  public void accept (TypeVisitor v) {
    v.visit(this);
  }



  public String getText () {
    return text;
  }

  public String getRank () {
    return text;
  }

  public boolean isFloatingPoint () {
    return (this == FLOAT || this == FLOAT32 || this == FLOAT64);
  }

  public boolean isInteger () {
    var a = (this == INT || this == INT8 || this == INT16 || this == INT32 || this == INT64);
    var b = (this == UINT || this == UINT8 || this == UINT16 || this == UINT32 || this == UINT64);
    return a || b;
  }

  public boolean isSigned () {
    return (this == INT || this == INT8 || this == INT16 || this == INT32 || this == INT64);
  }

  public void setText (String text) {
    this.text = text;
  }

  public void setRank (int rank) {
    this.rank = rank;
  }

  public Symbol getSymbol () {
    return symbol;
  }

  public void setSymbol (Symbol symbol) {
    this.symbol = symbol;
  }

  public String toString () {
    return text;
  }

}
