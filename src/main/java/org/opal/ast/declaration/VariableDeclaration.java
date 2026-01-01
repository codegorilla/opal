package org.opal.ast.declaration;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;
import org.opal.symbol.Scope;

public class VariableDeclaration extends AstNode {

  private ExportSpecifier exportSpecifier = null;
  private VariableModifiers modifiers = null;
  private VariableName name = null;
  private VariableTypeSpecifier typeSpecifier = null;
  private VariableInitializer initializer = null;

  // Attributes
  private Scope scope = null;

  public VariableDeclaration (Token token) {
    super(token);
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

  @Override
  public <T> T accept (ResultVisitor<T> v) {
    return v.visit(this);
  }

  public ExportSpecifier exportSpecifier () {
    return exportSpecifier;
  }

  public boolean hasExportSpecifier () {
    return exportSpecifier != null;
  }

  public boolean hasInitializer () {
    return initializer != null;
  }

  public boolean hasTypeSpecifier () {
    return typeSpecifier != null;
  }

  public VariableInitializer getInitializer () {
    return initializer;
  }

  public VariableModifiers getModifiers () {
    return modifiers;
  }

  public VariableName getName () {
    return name;
  }

  public VariableTypeSpecifier getTypeSpecifier () {
    return typeSpecifier;
  }

  public void setExportSpecifier (ExportSpecifier exportSpecifier) {
    this.exportSpecifier = exportSpecifier;
  }

  public void setInitializer (VariableInitializer initializer) {
    this.initializer = initializer;
  }

  public void setModifiers (VariableModifiers modifiers) {
    this.modifiers = modifiers;
  }

  public void setName (VariableName name) {
    this.name = name;
  }

  public void setTypeSpecifier (VariableTypeSpecifier typeSpecifier) {
    this.typeSpecifier = typeSpecifier;
  }

}
