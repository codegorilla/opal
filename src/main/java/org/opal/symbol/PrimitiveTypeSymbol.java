package org.opal.symbol;

import org.opal.type.PrimitiveType;

public class PrimitiveTypeSymbol extends Symbol {

  private final PrimitiveType type;

  public PrimitiveTypeSymbol (String name) {
    super(name);
    type = new PrimitiveType();
    type.setSymbol(this);
  }

  public PrimitiveType getType () {
    return type;
  }

}
