package org.opal.state;

import org.opal.ast.declaration.ImportDeclaration;

// This is the starting state.

public class ImportAliasStartState implements ImportAliasState {

  private final ImportAliasContext context;

  public ImportAliasStartState (ImportAliasContext context) {
    this.context = context;
  }

  public void transitionExplicit (ImportDeclaration node) {
    System.out.println("Transitioning from start to explicit.");
    context.setState(context.EXPLICIT);
    context.setNode(node);
  }

  public void transitionImplicit (ImportDeclaration node) {
    System.out.println("Transitioning from start to implicit.");
    context.setState(context.IMPLICIT);
    context.setNode(node);
  }
}
