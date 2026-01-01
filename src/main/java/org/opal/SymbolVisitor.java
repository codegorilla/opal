package org.opal;

import org.opal.symbol.PrimitiveTypeSymbol;
import org.opal.symbol.VariableSymbol;

public interface SymbolVisitor {

  public void visit (PrimitiveTypeSymbol symbol);

  public void visit (VariableSymbol symbol);

}
