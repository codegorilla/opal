package org.opal;

import org.opal.symbol.*;

public interface SymbolVisitor {

  public void visit (RoutineSymbol symbol);

  public void visit (TypeSymbol symbol);

  public void visit (VariableSymbol symbol);

}
