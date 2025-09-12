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

  // Where is package scope? Is it the same as global scope?

  public enum Kind {
    BUILT_IN,
    CLASS,
    GLOBAL,
    LOCAL,
    PACKAGE
  }

}

/*

class Scope1 (private var kind: Scope.Kind) {

  val symbolTable = SymbolTable()
  var enclosingScope: Scope = null

  // Should caller provide name and kind, or already constructed symbol?

  def define (symbol: Symbol) =
      symbolTable.insert(symbol)

  def resolve (name: String, recurse: Boolean = true): Symbol =
  // Recurse through scope stack, looking for symbol
  var symbol = symbolTable.lookup(name)
    if symbol == null then
      if recurse && enclosingScope != null then
      symbol = enclosingScope.resolve(name)
    return symbol

  def getEnclosingScope (): Scope =
  enclosingScope

  def setEnclosingScope (scope: Scope) =
  enclosingScope = scope

  def getKind (): Scope.Kind =
      return kind
}

*/
