package org.opal.type;

import org.opal.symbol.Symbol;

public class PrimitiveType extends Type {

  private String text;

  private Symbol symbol;

  public static final PrimitiveType BOOL    = new PrimitiveType("bool");
  public static final PrimitiveType FLOAT   = new PrimitiveType("float");
  public static final PrimitiveType FLOAT32 = new PrimitiveType("float32");
  public static final PrimitiveType FLOAT64 = new PrimitiveType("float64");
  public static final PrimitiveType INT     = new PrimitiveType("int");
  public static final PrimitiveType INT8    = new PrimitiveType("int8");
  public static final PrimitiveType INT16   = new PrimitiveType("int16");
  public static final PrimitiveType INT32   = new PrimitiveType("int32");
  public static final PrimitiveType INT64   = new PrimitiveType("int64");
  public static final PrimitiveType UINT     = new PrimitiveType("uint");
  public static final PrimitiveType UINT8    = new PrimitiveType("uint8");
  public static final PrimitiveType UINT16   = new PrimitiveType("uint16");
  public static final PrimitiveType UINT32   = new PrimitiveType("uint32");
  public static final PrimitiveType UINT64   = new PrimitiveType("uint64");
  public static final PrimitiveType VOID  = new PrimitiveType("void");

  public PrimitiveType (String text) {
    super();
    this.text = text;
  }

  @Override
  public void accept (TypeVisitor v) {
    v.visit(this);
  }

  public String getText () {
    return text;
  }

  public void setText (String text) {
    this.text = text;
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
