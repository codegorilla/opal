package org.opal.symbol;

import org.opal.type.PrimitiveType;

// Should the variable name symbol contain a reference to the variable name
// node? I think so. Likewise the node should probably link back to the symbol.

public class VariableSymbol extends Symbol {

  public VariableSymbol (String name) {
    super(name);
  }

}
