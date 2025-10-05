package org.opal;

import org.opal.ast.TranslationUnit;
import org.opal.ast.declaration.*;
import org.opal.ast.expression.*;
import org.opal.ast.statement.*;
import org.opal.ast.type.*;

public interface ResultVisitor <T> {

  // DECLARATIONS

  // General declarations
  public T visit (TranslationUnit node);
  public T visit (Declarations node);
  public T visit (AccessSpecifier node);
  public T visit (Modifiers node);
  public T visit (Modifier node);

  // Package declarations
  public T visit (PackageDeclaration node);
  public T visit (PackageName node);
  
  // Import declarations
  public T visit (ImportDeclarations node);
  public T visit (ImportDeclaration node);
  public T visit (ImportName node);

  // Routine declarations
  public T visit (RoutineDeclaration node);
  public T visit (RoutineName node);
  public T visit (RoutineParameters node);
  public T visit (RoutineParameter node);
  public T visit (RoutineParameterName node);
  public T visit (RoutineParameterTypeSpecifier node);
  public T visit (RoutineReturnType node);
  public T visit (RoutineBody node);

  // Variable declarations
  public T visit (VariableDeclaration node);
  public T visit (LocalVariableDeclaration node);
  public T visit (VariableName node);
  public T visit (VariableTypeSpecifier node);
  public T visit (VariableInitializer node);

  // STATEMENTS

  public T visit (BreakStatement node);
  public T visit (CompoundStatement node);
  public T visit (ContinueStatement node);
  public T visit (DoStatement node);
  public T visit (ElseClause node);
  public T visit (EmptyStatement node);
  public T visit (ExpressionStatement node);
  public T visit (IfStatement node);
  public T visit (IfCondition node);
  public T visit (ReturnStatement node);
  public T visit (UntilStatement node);
  public T visit (UntilCondition node);
  public T visit (WhileStatement node);
  public T visit (WhileCondition node);

  // EXPRESSIONS

  public T visit (Expression node);
  public T visit (BinaryExpression node);
  public T visit (UnaryExpression node);
  public T visit (DereferencingMemberAccess node);
  public T visit (MemberAccess node);
  public T visit (RoutineCall node);
  public T visit (Arguments node);
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
  public T visit (This node);

  // TYPES

  public T visit (Type node);
  public T visit (TypeRoot node);
  public T visit (ArrayType node);
  public T visit (NominalType node);
  public T visit (PointerType node);
  public T visit (PrimitiveType node);

  public T visit (TemplateInstantiation node);
  public T visit (TemplateArguments node);
  public T visit (TemplateArgument node);

}
