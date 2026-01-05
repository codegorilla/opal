package org.opal;

import org.opal.symbol.TypeSymbol;
import org.opal.symbol.VariableSymbol;

public interface SymbolVisitor {

  public void visit (TypeSymbol symbol);

  public void visit (VariableSymbol symbol);

}
