package org.opal.symbol;

import org.opal.SymbolVisitor;
import org.opal.type.PrimitiveType;
import org.opal.type.Type;

// Should the variable name symbol contain a reference to the variable name
// node? I think so. Likewise the node should probably link back to the symbol.

public class VariableSymbol extends Symbol {

  private Type type;

  public VariableSymbol (String name) {
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
