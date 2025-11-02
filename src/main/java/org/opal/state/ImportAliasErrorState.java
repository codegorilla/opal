package org.opal.state;

import org.opal.ast.declaration.ImportDeclaration;

// This state is reached if two different import declarations try to establish
// the same import alias with duplicate 'as' clauses.

public class ImportAliasErrorState implements ImportAliasState {

  public ImportAliasErrorState () {}

  public void transitionExplicit (ImportDeclaration node) {
    System.out.println("Remaining at error.");
  }

  public void transitionImplicit (ImportDeclaration node) {
    System.out.println("Remaining at error.");
  }
}
