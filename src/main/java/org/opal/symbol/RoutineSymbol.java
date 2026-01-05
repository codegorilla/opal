package org.opal.symbol;

import org.opal.SymbolVisitor;
import org.opal.type.Type;

public class RoutineSymbol extends Symbol {

  // Do routines really need a type?

  private Type type;

  public RoutineSymbol (String name) {
    super(name);
  }

  @Override
  public void accept (SymbolVisitor v) {
    v.visit(this);
  }

  public Type getType () {
    return type;
  }

  public void setType (Type type) {
    this.type = type;
  }

}
