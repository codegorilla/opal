package org.opal.symbol;

public class Scope {

  private final Scope.Kind kind;
  private final SymbolTable symbolTable;
  private Scope enclosingScope;

  public Scope (Scope.Kind kind) {
    this.kind = kind;
    symbolTable = new SymbolTable();
    enclosingScope = null;
  }

  public void define (Symbol symbol) {
    symbolTable.insert(symbol);
  }

  public Symbol resolve (String name, boolean recurse) {
    // Recurse through scope stack, looking for symbol
    var symbol = symbolTable.lookup(name);
    if (symbol == null)
      if (recurse && enclosingScope != null)
        symbol = enclosingScope.resolve(name, true);
    return symbol;
  }

  public Scope getEnclosingScope () {
    return enclosingScope;
  }

  public Scope.Kind getKind () {
    return kind;
  }

  public void setEnclosingScope (Scope scope) {
    enclosingScope = scope;
  }

  // Where is package scope? Is it the same as global scope? We might be able
  // to combine them. But we might not be able to if we need to account for
  // true global objects when interfacing with C language.

  public enum Kind {
    BUILT_IN,
    CLASS,
    GLOBAL,
    BLOCK,
    PACKAGE
  }

}
