package org.opal.state;

import org.opal.ast.declaration.ImportDeclaration;

public interface ImportAliasState {
  void handleExplicit (ImportDeclaration node);
  void handleImplicit (ImportDeclaration node);
}
