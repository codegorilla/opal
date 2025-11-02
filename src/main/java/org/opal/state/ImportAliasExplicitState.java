package org.opal.state;

import org.opal.ast.declaration.ImportDeclaration;

// This state is reached when an import alias is explicitly established by an
// 'as' clause in an import declaration.

public class ImportAliasExplicitState implements ImportAliasState {

  private final ImportAliasContext context;

  public ImportAliasExplicitState (ImportAliasContext context) {
    this.context = context;
  }

  // Even though this is an error, we still set the node so that there is
  // enough context to fill out the error message.

  public void handleExplicit (ImportDeclaration node) {
    System.out.println("Transitioning from explicit to error.");
    context.setState(context.ERROR);
    context.setNode(node);
    context.setErrorBit(true);
  }

  public void handleImplicit (ImportDeclaration node) {
    System.out.println("Remaining at explicit.");
  }
}
