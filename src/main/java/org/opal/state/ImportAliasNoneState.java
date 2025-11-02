package org.opal.state;

import org.opal.ast.declaration.ImportDeclaration;

// This state is reached if two qualified import names share the same final
// component name, and no explicit alias name has been declared.

public class ImportAliasNoneState implements ImportAliasState {

  private final ImportAliasContext context;

  public ImportAliasNoneState (ImportAliasContext context) {
    this.context = context;
  }

  public void transitionExplicit (ImportDeclaration node) {
    System.out.println("Transitioning from none to explicit.");
    context.setState(context.EXPLICIT);
    context.setNode(node);
  }

  public void transitionImplicit (ImportDeclaration node) {
    System.out.println("Remaining at none.");
  }
}
