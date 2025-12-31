package org.opal.type;

import org.opal.symbol.Symbol;

public class PrimitiveType extends Type {

  private PrimitiveType.Kind kind;

  private Symbol symbol;

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
