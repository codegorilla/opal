package org.opal;

import org.opal.ast.*;
import org.opal.ast.declaration.*;
import org.opal.ast.expression.*;
import org.opal.ast.statement.*;
import org.opal.ast.type.*;

public class BaseVisitor implements Visitor {

  AstNode root;

  public BaseVisitor  (AstNode input) {
    root = input;
  }

  public void process () {}

  // DECLARATIONS

  // General declarations
  public void visit (TranslationUnit node) {}
  public void visit (Declarations node) {}
  public void visit (AccessSpecifier node) {}
  public void visit (Modifiers node) {}
  public void visit (Modifier node) {}

  // Package declarations
  public void visit (PackageDeclaration node) {}
  public void visit (PackageName node) {}

  // Import declarations
  public void visit (ImportDeclarations node) {}
  public void visit (ImportDeclaration node) {}
  public void visit (ImportName node) {}

  // Routine declarations
  public void visit (RoutineDeclaration node) {}
  public void visit (RoutineName node) {}
  public void visit (RoutineParameters node) {}
  public void visit (RoutineParameter node) {}
  public void visit (RoutineParameterName node) {}
  public void visit (RoutineParameterTypeSpecifier node) {}
  public void visit (RoutineReturnType node) {}
  public void visit (RoutineBody node) {}

  // Variable declarations
  public void visit (VariableDeclaration node) {}
  public void visit (LocalVariableDeclaration node) {}
  public void visit (VariableName node) {}
  public void visit (VariableTypeSpecifier node) {}
  public void visit (VariableInitializer node) {}

  // STATEMENTS

  public void visit (BreakStatement node) {}
  public void visit (CompoundStatement node) {}
  public void visit (ContinueStatement node) {}
  public void visit (DoStatement node) {}
  public void visit (ElseClause node) {}
  public void visit (EmptyStatement node) {}
  public void visit (ExpressionStatement node) {}
  public void visit (IfStatement node) {}
  public void visit (IfCondition node) {}
  public void visit (ReturnStatement node) {}
  public void visit (UntilStatement node) {}
  public void visit (UntilCondition node) {}
  public void visit (WhileStatement node) {}
  public void visit (WhileCondition node) {}

  // EXPRESSIONS

  public void visit (Expression node) {}
  public void visit (BinaryExpression node) {}
  public void visit (UnaryExpression node) {}
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

  // TYPES

  public void visit (Type node) {}
  public void visit (TypeRoot node) {}
  public void visit (ArrayType node) {}
  public void visit (NominalType node) {}
  public void visit (PointerType node) {}
  public void visit (PrimitiveType node) {}

  public void visit (TemplateInstantiation node) {}
  public void visit (TemplateArguments node) {}
  public void visit (TemplateArgument node) {}

}
