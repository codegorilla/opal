package org.opal.state;

import org.opal.ast.declaration.ImportDeclaration;

// This finite state machine (FSM) is used to track which import declaration
// node (if any) corresponds to a particular import alias. It is probably
// overkill to use the State design pattern for this, but it works fine and was
// a nice exercise in software design patterns.

// In this FSM, the implicit, explicit, none, and error states are all
// accepting states. The implicit and explicit states result in a viable import
// declaration node that matches the import alias. The none and error states do
// not. Furthermore, the error state results in a compilation error.

public class ImportAliasContext {

  // Import alias states
  public final ImportAliasStartState START = new ImportAliasStartState(this);
  public final ImportAliasImplicitState IMPLICIT = new ImportAliasImplicitState(this);
  public final ImportAliasExplicitState EXPLICIT = new ImportAliasExplicitState(this);
  public final ImportAliasNoneState NONE = new ImportAliasNoneState(this);
  public final ImportAliasErrorState ERROR = new ImportAliasErrorState();

  // Tracks current import alias state
  private ImportAliasState state;

  // Tracks current import declaration node
  private ImportDeclaration node;

  // Tracks whether there has been an error or not
  private boolean errorBit;

  public ImportAliasContext () {
    setState(START);
    setNode(null);
    setErrorBit(false);
  }

  public static ImportAliasContext createExplicit (ImportDeclaration node) {
    var context = new ImportAliasContext();
    context.requestExplicit(node);
    return context;
  }

  public static ImportAliasContext createImplicit (ImportDeclaration node) {
    var context = new ImportAliasContext();
    context.requestImplicit(node);
    return context;
  }

  public boolean getErrorBit () {
    return errorBit;
  }

  public ImportDeclaration getNode () {
    return node;
  }

  public void setErrorBit (boolean errorBit) {
    this.errorBit = errorBit;
  }

  public void setNode (ImportDeclaration node) {
    this.node = node;
  }

  public void setState (ImportAliasState state) {
    this.state = state;
  }

  public void requestExplicit (ImportDeclaration node) {
    state.handleExplicit(node);
  }

  public void requestImplicit (ImportDeclaration node) {
    state.handleImplicit(node);
  }
}
