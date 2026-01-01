package org.opal;

import org.opal.ast.AstNode;
import org.opal.symbol.PrimitiveTypeSymbol;
import org.opal.symbol.VariableSymbol;

public class BaseSymbolVisitor implements SymbolVisitor {

  public BaseSymbolVisitor () {}

  public void visit (PrimitiveTypeSymbol symbol) {
    System.out.println("Visited primitive symbol");
  }

  public void visit (VariableSymbol symbol) {

  }

}
