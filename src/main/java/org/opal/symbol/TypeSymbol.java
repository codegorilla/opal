package org.opal.symbol;

import org.opal.SymbolVisitor;
import org.opal.type.Type;

public class TypeSymbol extends Symbol {

  private final Type type;

  public TypeSymbol (String name, Type type) {
    super(name);
    this.type = type;
    // Not sure if we actually need a link back or not
    //type.setSymbol(this);
  }

  @Override
  public void accept (SymbolVisitor v) {
    v.visit(this);
  }

  public Type getType () {
    return type;
  }

}
