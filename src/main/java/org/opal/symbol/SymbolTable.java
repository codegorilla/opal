package org.opal.symbol;

import java.util.HashMap;

public class SymbolTable {

  private final HashMap<String, Symbol> data;

  public SymbolTable () {
    data = new HashMap<>();
  }

  public void insert (Symbol symbol) {
    data.put(symbol.getName(), symbol);
  }

  public Symbol lookup (String name) {
      return data.getOrDefault(name, null);
  }
}
