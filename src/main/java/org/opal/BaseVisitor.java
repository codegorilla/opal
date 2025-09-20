package org.opal;

import org.opal.ast.*;

public class BaseVisitor implements Visitor {

  AstNode root;

  public BaseVisitor  (AstNode input) {
    root = input;
  }

  public void process () {}

  public void visit (TranslationUnit node) {}

  // Declarations
  public void visit (Declarations node) {}
  public void visit (ImportDeclaration node) {}
  public void visit (ImportName node) {}

  public void visit (AccessSpecifier node) {}
  public void visit (Modifiers node) {}
  public void visit (Modifier node) {}

  // Routine declarations
  public void visit (RoutineDeclaration node) {}
  public void visit (RoutineName node) {}
  public void visit (RoutineParameters node) {}
  public void visit (RoutineParameter node) {}
  public void visit (RoutineParameterName node) {}
  public void visit (RoutineReturnType node) {}
  public void visit (RoutineBody node) {}

  // Variable declarations
  public void visit (VariableDeclaration node) {}
  public void visit (VariableName node) {}
  public void visit (VariableTypeSpecifier node) {}
  public void visit (VariableInitializer node) {}

  // Expressions
  public void visit (Expression node) {}
  public void visit (BinaryExpression node) {}
  public void visit (DereferencingMemberAccess node) {}
  public void visit (MemberAccess node) {}
  public void visit (RoutineCall node) {}
  public void visit (Arguments node) {}
  public void visit (ArraySubscript node) {}

  // Literals
  public void visit (BooleanLiteral node) {}
  public void visit (CharacterLiteral node) {}
  public void visit (FloatingPointLiteral node) {}
  public void visit (IntegerLiteral node) {}
  public void visit (NullLiteral node) {}
  public void visit (StringLiteral node) {}
  public void visit (UnsignedIntegerLiteral node) {}

  // Special
  public void visit (This node) {}

  // Types
  public void visit (TypeRoot node) {}
  public void visit (ArrayType node) {}
  public void visit (NominalType node) {}
  public void visit (PointerType node) {}
  public void visit (PrimitiveType node) {}

}
