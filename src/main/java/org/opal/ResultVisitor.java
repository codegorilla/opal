package org.opal;

import org.opal.ast.*;
import org.opal.ast.ErrorNode;
import org.opal.ast.declaration.*;
import org.opal.ast.expression.*;
import org.opal.ast.statement.*;
import org.opal.ast.type.*;

public interface ResultVisitor <T> {

  // TRANSLATION UNIT

  public T visit (TranslationUnit node);

  // DECLARATIONS

  // General declarations
  public T visit (Declarations node);

  // Package declarations
  public T visit (PackageDeclaration node);
  public T visit (PackageName node);

  // Import declarations
  public T visit (ImportDeclarations node);
  public T visit (ImportDeclaration node);
  public T visit (ImportQualifiedName node);
  public T visit (ImportName node);
  public T visit (ImportAsName node);

  // Use declarations
  public T visit (UseDeclarations node);
  public T visit (UseDeclaration node);
  public T visit (UseQualifiedName node);
  public T visit (UseName node);
  public T visit (UseOneName node);
  public T visit (UseNameGroup node);
  public T visit (UseSomeName node);
  public T visit (UseNameWildcard node);

  // Other declarations
  public T visit (OtherDeclarations node);
  public T visit (ExportSpecifier node);
  public T visit (NoexceptSpecifier node);
  public T visit (Modifier node);

  // Class declarations
  public T visit (ClassDeclaration node);
  public T visit (ClassModifiers node);
  public T visit (ClassName node);
  public T visit (ClassBody node);
  public T visit (ClassExtendsClause node);
  public T visit (BaseClasses node);
  public T visit (BaseClass node);

  // Member general declarations
  public T visit (MemberAccessSpecifier node);

  // Member typealias declarations
  public T visit (MemberTypealiasDeclaration node);

  // Member use declarations
  public T visit (MemberUseDeclaration node);

  // Member routine declarations
  public T visit (MemberRoutineDeclaration node);
  public T visit (MemberRoutineModifiers node);
  public T visit (CVQualifiers node);
  public T visit (CVQualifier node);
  public T visit (RefQualifiers node);
  public T visit (RefQualifier node);

  // Member variable declarations
  public T visit (MemberVariableDeclaration node);
  public T visit (MemberVariableModifiers node);

  // Typealias declarations
  public T visit (TypealiasDeclaration node);
  public T visit (LocalTypealiasDeclaration node);
  public T visit (TypealiasName node);

  // Routine declarations
  public T visit (RoutineDeclaration node);
  public T visit (RoutineModifiers node);
  public T visit (RoutineName node);
  public T visit (RoutineParameters node);
  public T visit (RoutineParameter node);
  public T visit (RoutineParameterName node);
  public T visit (RoutineParameterTypeSpecifier node);
  public T visit (RoutineReturnType node);
  public T visit (RoutineBody node);

  // Variable declarations
  public T visit (VariableDeclaration node);
  public T visit (VariableModifiers node);
  public T visit (LocalVariableDeclaration node);
  public T visit (VariableName node);
  public T visit (VariableTypeSpecifier node);
  public T visit (VariableInitializer node);

  // STATEMENTS

  public T visit (BreakStatement node);
  public T visit (CompoundStatement node);
  public T visit (ContinueStatement node);
  public T visit (DoUntilStatement node);
  public T visit (DoWhileStatement node);
  public T visit (ElseClause node);
  public T visit (EmptyStatement node);
  public T visit (ExpressionStatement node);
  public T visit (LoopStatement node);
  public T visit (LoopControl node);
  public T visit (LoopInitializer node);
  public T visit (LoopCondition node);
  public T visit (LoopUpdate node);
  public T visit (ForStatement node);
  public T visit (IfStatement node);
  public T visit (ReturnStatement node);
  public T visit (UntilStatement node);
  public T visit (WhileStatement node);

  // EXPRESSIONS

  public T visit (Expression node);
  public T visit (BinaryExpression node);
  public T visit (UnaryExpression node);
  public T visit (CastExpression node);
  public T visit (DeleteExpression node);
  public T visit (NewExpression node);
  public T visit (NewInitializer node);
  public T visit (DereferencingMemberAccess node);
  public T visit (MemberAccess node);
  public T visit (RoutineCall node);
  public T visit (RoutineArguments node);
  public T visit (RoutineArgument node);
  public T visit (ArraySubscript node);

  // Literals
  public T visit (BooleanLiteral node);
  public T visit (CharacterLiteral node);
  public T visit (FloatingPointLiteral node);
  public T visit (IntegerLiteral node);
  public T visit (NullLiteral node);
  public T visit (StringLiteral node);
  public T visit (UnsignedIntegerLiteral node);

  // Special
  public T visit (Name node);
  public T visit (This node);
  public T visit (ErrorNode node);

  // TYPES

  public T visit (Type node);
  public T visit (ArrayType node);
  public T visit (NominalType node);
  public T visit (PointerType node);
  public T visit (PrimitiveType node);
  public T visit (RoutinePointerType node);

  public T visit (TemplateInstantiation node);
  public T visit (TemplateArguments node);
  public T visit (TemplateArgument node);

}
