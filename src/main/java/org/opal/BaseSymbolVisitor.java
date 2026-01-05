package org.opal;

import org.opal.symbol.*;

public class BaseSymbolVisitor implements SymbolVisitor {

  public BaseSymbolVisitor () {}

  public void visit (RoutineSymbol symbol) {}

  public void visit (TypeSymbol symbol) {
    System.out.println("Visited primitive symbol");
  }

  public void visit (VariableSymbol symbol) {}

}
