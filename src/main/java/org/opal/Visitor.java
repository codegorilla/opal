package org.opal;

import org.opal.ast.*;

public interface Visitor {
  // Declarations
  public void visit (TranslationUnit node);
  public void visit (Declarations node);
  public void visit (ImportDeclaration node);
  public void visit (ImportName node);
  public void visit (VariableDeclaration node);
  public void visit (VariableName node);
  public void visit (VariableTypeSpecifier node);
  public void visit (VariableInitializer node);

  // Types
  public void visit (TypeRoot node);
  public void visit (ArrayType node);
  public void visit (NominalType node);
  public void visit (PointerType node);
  public void visit (PrimitiveType node);
}
