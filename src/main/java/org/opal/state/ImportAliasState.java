package org.opal.state;

import org.opal.ast.declaration.ImportDeclaration;

public interface ImportAliasState {
  void transitionExplicit (ImportDeclaration node);
  void transitionImplicit (ImportDeclaration node);
}
