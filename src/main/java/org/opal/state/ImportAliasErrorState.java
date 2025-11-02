package org.opal.state;

import org.opal.ast.declaration.ImportDeclaration;

// This state is reached if two different import declarations try to establish
// the same import alias with duplicate 'as' clauses.

public class ImportAliasErrorState implements ImportAliasState {

  public ImportAliasErrorState () {}

  public void handleExplicit (ImportDeclaration node) {
    System.out.println("Remaining at error.");
  }

  public void handleImplicit (ImportDeclaration node) {
    System.out.println("Remaining at error.");
  }
}
