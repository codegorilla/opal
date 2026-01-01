package org.opal.symbol;

import org.opal.ResultVisitor;
import org.opal.SymbolVisitor;
import org.opal.Visitor;
import org.opal.type.PrimitiveType;

public class PrimitiveTypeSymbol extends Symbol {

  private final PrimitiveType type;

  public PrimitiveTypeSymbol (String name) {
    super(name);
    type = new PrimitiveType();
    type.setSymbol(this);
  }

  @Override
  public void accept (SymbolVisitor v) {
    v.visit(this);
  }

  public PrimitiveType getType () {
    return type;
  }



}
