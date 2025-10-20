package org.opal;

import org.opal.ast.*;
import org.opal.ast.declaration.*;
import org.opal.ast.expression.*;
import org.opal.ast.statement.*;
import org.opal.ast.type.*;

public interface Visitor {

  // DECLARATIONS

  // General declarations
  public void visit (TranslationUnit node);
  public void visit (Declarations node);
  public void visit (ExportSpecifier node);
  public void visit (NoexceptSpecifier node);
  public void visit (Modifier node);

  // Package declarations
  public void visit (PackageDeclaration node);
  public void visit (PackageName node);

  // Import declarations
  public void visit (ImportDeclarations node);
  public void visit (ImportDeclaration node);
  public void visit (ImportName node);

  // Class declarations
  public void visit (ClassDeclaration node);
  public void visit (ClassModifiers node);
  public void visit (ClassName node);
  public void visit (ClassBody node);
  public void visit (ClassExtendsClause node);
  public void visit (BaseClasses node);
  public void visit (BaseClass node);

  // Member general declarations
  public void visit (MemberAccessSpecifier node);

  // Member routine declarations
  public void visit (MemberRoutineDeclaration node);
  public void visit (MemberRoutineModifiers node);
  public void visit (CVQualifiers node);
  public void visit (CVQualifier node);
  public void visit (RefQualifiers node);
  public void visit (RefQualifier node);

  // Member variable declarations
  public void visit (MemberVariableDeclaration node);
  public void visit (MemberVariableModifiers node);

  // Routine declarations
  public void visit (RoutineDeclaration node);
  public void visit (RoutineModifiers node);
  public void visit (RoutineName node);
  public void visit (RoutineParameters node);
  public void visit (RoutineParameter node);
  public void visit (RoutineParameterName node);
  public void visit (RoutineParameterTypeSpecifier node);
  public void visit (RoutineReturnType node);
  public void visit (RoutineBody node);

  // Variable declarations
  public void visit (VariableDeclaration node);
  public void visit (VariableModifiers node);
  public void visit (LocalVariableDeclaration node);
  public void visit (VariableName node);
  public void visit (VariableTypeSpecifier node);
  public void visit (VariableInitializer node);

  // STATEMENTS

  public void visit (BreakStatement node);
  public void visit (CompoundStatement node);
  public void visit (ContinueStatement node);
  public void visit (DoUntilStatement node);
  public void visit (DoWhileStatement node);
  public void visit (ElseClause node);
  public void visit (EmptyStatement node);
  public void visit (ExpressionStatement node);
  public void visit (LoopStatement node);
  public void visit (LoopControl node);
  public void visit (LoopInitializer node);
  public void visit (LoopCondition node);
  public void visit (LoopUpdate node);
  public void visit (ForStatement node);
  public void visit (IfStatement node);
  public void visit (ReturnStatement node);
  public void visit (UntilStatement node);
  public void visit (WhileStatement node);

  // EXPRESSIONS

  public void visit (Expression node);
  public void visit (BinaryExpression node);
  public void visit (UnaryExpression node);
  public void visit (CastExpression node);
  public void visit (DeleteExpression node);
  public void visit (NewExpression node);
  public void visit (DereferencingMemberAccess node);
  public void visit (MemberAccess node);
  public void visit (RoutineCall node);
  public void visit (RoutineArguments node);
  public void visit (RoutineArgument node);
  public void visit (ArraySubscript node);

  // Literals
  public void visit (BooleanLiteral node);
  public void visit (CharacterLiteral node);
  public void visit (FloatingPointLiteral node);
  public void visit (IntegerLiteral node);
  public void visit (NullLiteral node);
  public void visit (StringLiteral node);
  public void visit (UnsignedIntegerLiteral node);

  // Special
  public void visit (Name node);
  public void visit (This node);

  // TYPES

  public void visit (Type node);
  public void visit (TypeRoot node);
  public void visit (ArrayType node);
  public void visit (NominalType node);
  public void visit (PointerType node);
  public void visit (PrimitiveType node);
  public void visit (RoutinePointerType node);

  public void visit (TemplateInstantiation node);
  public void visit (TemplateArguments node);
  public void visit (TemplateArgument node);

}
