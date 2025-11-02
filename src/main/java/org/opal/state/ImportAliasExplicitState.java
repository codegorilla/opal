package org.opal.state;

import org.opal.ast.declaration.ImportDeclaration;

// This state is reached when an import alias is explicitly established by an
// 'as' clause in an import declaration.

public class ImportAliasExplicitState implements ImportAliasState {

  private final ImportAliasContext context;

  public ImportAliasExplicitState (ImportAliasContext context) {
    this.context = context;
  }

  public void handleExplicit (ImportDeclaration node) {
    System.out.println("Transitioning from explicit to error.");
    context.setState(context.ERROR);
    context.setNode(null);
    context.setErrorBit(true);
  }

  public void handleImplicit (ImportDeclaration node) {
    System.out.println("Remaining at explicit.");
  }
}
