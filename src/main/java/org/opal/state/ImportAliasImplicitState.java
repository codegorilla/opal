package org.opal.state;

import org.opal.ast.declaration.ImportDeclaration;

// This state is reached when an import alias is implicitly established by a
// qualified import name in an import declaration.

public class ImportAliasImplicitState implements ImportAliasState {

  private final ImportAliasContext context;

  public ImportAliasImplicitState (ImportAliasContext context) {
    this.context = context;
  }

  public void handleExplicit (ImportDeclaration node) {
    System.out.println("Transitioning from implicit to explicit.");
    context.setState(context.EXPLICIT);
    context.setNode(node);
  }

  public void handleImplicit (ImportDeclaration node) {
    System.out.println("Transitioning from implicit to none.");
    context.setState(context.NONE);
    context.setNode(null);
  }
}
