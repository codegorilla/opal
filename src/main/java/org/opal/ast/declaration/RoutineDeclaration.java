package org.opal.ast.declaration;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;

public class RoutineDeclaration extends AstNode {

  private ExportSpecifier exportSpecifier = null;
  private RoutineModifiers modifiers = null;
  private RoutineName name = null;

  public RoutineDeclaration (Token token) {
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

  public boolean hasExportSpecifier () {
    return exportSpecifier != null;
  }

  public boolean hasNoexceptSpecifier () {
    return getChild(4) != null;
  }

  public boolean hasReturnType () {
    return getChild(5) != null;
  }

  public AstNode modifiers () {
    return getChild(1);
  }

  public AstNode getName () {
    return name;
  }

  public RoutineModifiers getModifiers () {
    return modifiers;
  }

  public AstNode parameters () {
    return getChild(3);
  }

  public AstNode noexceptSpecifier () {
    return getChild(4);
  }

  public AstNode returnType () {
    return getChild(5);
  }

  public AstNode body () {
    return getChild(6);
  }

  public void setExportSpecifier (ExportSpecifier exportSpecifier) {
    this.exportSpecifier = exportSpecifier;
  }

  public void setModifiers (RoutineModifiers modifiers) {
    this.modifiers = modifiers;
  }

  public void setName (RoutineName name) {
    this.name = name;
  }

}
