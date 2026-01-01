package org.opal.symbol;

import org.opal.ResultVisitor;
import org.opal.SymbolVisitor;
import org.opal.Visitor;

public abstract class Symbol {

  private final String name;

  public Symbol (String name) {
    this.name = name;
  }

  public String getName () {
    return name;
  }

  public abstract void accept (SymbolVisitor v);

}
