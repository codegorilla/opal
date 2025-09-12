package org.opal.symbol;

public abstract class Symbol {

  private final String name;

  public Symbol (String name) {
    this.name = name;
  }

  public String getName () {
    return name;
  }
}
