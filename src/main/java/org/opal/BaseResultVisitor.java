package org.opal;

import org.opal.ast.*;
import org.opal.ast.ErrorNode;
import org.opal.ast.declaration.*;
import org.opal.ast.expression.*;
import org.opal.ast.statement.*;
import org.opal.ast.type.*;

public class BaseResultVisitor<T> implements ResultVisitor <T> {

  AstNode root;

  public BaseResultVisitor (AstNode input) {
    root = input;
  }

  public T process () { return null; }

  // TRANSLATION UNIT

  public T visit (TranslationUnit node) { return null; }

  // DECLARATIONS

  // General declarations
  public T visit (Declarations node) { return null; }
  public T visit (Declaration node) { return null; }

  // Package declarations
  public T visit (PackageDeclaration node) { return null; }
  public T visit (PackageName node) { return null; }

  // Import declarations
  public T visit (ImportDeclarations node) { return null; }
  public T visit (ImportDeclaration node) { return null; }
  public T visit (ImportQualifiedName node) { return null; }
  public T visit (ImportName node) { return null; }
  public T visit (ImportAsName node) { return null; }

  // Using declarations
  public T visit (UseDeclarations node) { return null; }
  public T visit (UseDeclaration node) { return null; }
  public T visit (UseQualifiedName node) { return null; }
  public T visit (UseName node) { return null; }
  public T visit (UseNameGroup node) { return null; }
  public T visit (UseNameWildcard node) { return null; }

  // Other declarations
  public T visit (OtherDeclarations node) { return null; }
  public T visit (ExportSpecifier node) { return null; }
  public T visit (NoexceptSpecifier node) { return null; }
  public T visit (Modifier node) { return null; }

  // Class declarations
  public T visit (ClassDeclaration node) { return null; }
  public T visit (ClassModifiers node) { return null; }
  public T visit (ClassName node) { return null; }
  public T visit (ClassBody node) { return null; }
  public T visit (BaseClasses node) { return null; }
  public T visit (BaseClass node) { return null; }

  // Member general declarations
  public T visit (MemberAccessSpecifier node) { return null; }

  // Member typealias declarations
  public T visit (MemberTypealiasDeclaration node) { return null; }

  // Member use declarations
  public T visit (MemberUseDeclaration node) { return null; }

  // Member routine declarations
  public T visit (MemberRoutineDeclaration node) { return null; }
  public T visit (MemberRoutineModifiers node) { return null; }
  public T visit (CVQualifiers node) { return null; }
  public T visit (CVQualifier node) { return null; }
  public T visit (RefQualifiers node) { return null; }
  public T visit (RefQualifier node) { return null; }

  // Member variable declarations
  public T visit (MemberVariableDeclaration node) { return null; }
  public T visit (MemberVariableModifiers node) { return null; }

  // Typealias declarations
  public T visit (TypealiasDeclaration node) { return null; }
  public T visit (LocalTypealiasDeclaration node) { return null; }
  public T visit (TypealiasName node) { return null; }

  // Routine declarations
  public T visit (RoutineDeclaration node) { return null; }
  public T visit (RoutineModifiers node) { return null; }
  public T visit (RoutineName node) { return null; }
  public T visit (RoutineParameters node) { return null; }
  public T visit (RoutineParameter node) { return null; }
  public T visit (RoutineParameterName node) { return null; }
  public T visit (RoutineParameterTypeSpecifier node) { return null; }
  public T visit (RoutineReturnTypeSpecifier node) { return null; }
  public T visit (RoutineBody node) { return null; }

  // Variable declarations
  public T visit (VariableDeclaration node) { return null; }
  public T visit (VariableModifiers node) { return null; }
  public T visit (LocalVariableDeclaration node) { return null; }
  public T visit (VariableName node) { return null; }
  public T visit (VariableTypeSpecifier node) { return null; }
  public T visit (VariableInitializer node) { return null; }

  // Special declarations
  public T visit (BogusDeclaration node) { return null; }

  // STATEMENTS

  public T visit (BreakStatement node) { return null; }
  public T visit (CompoundStatement node) { return null; }
  public T visit (ContinueStatement node) { return null; }
  public T visit (DoUntilStatement node) { return null; }
  public T visit (DoWhileStatement node) { return null; }
  public T visit (ElseClause node) { return null; }
  public T visit (EmptyStatement node) { return null; }
  public T visit (ExpressionStatement node) { return null; }
  public T visit (LoopStatement node) { return null; }
  public T visit (LoopControl node) { return null; }
  public T visit (LoopInitializer node) { return null; }
  public T visit (LoopCondition node) { return null; }
  public T visit (LoopUpdate node) { return null; }
  public T visit (ForStatement node) { return null; }
  public T visit (IfStatement node) { return null; }
  public T visit (ReturnStatement node) { return null; }
  public T visit (UntilStatement node) { return null; }
  public T visit (WhileStatement node) { return null; }

  // EXPRESSIONS

  public T visit (Expression node) { return null; }
  public T visit (BinaryExpression node) { return null; }
  public T visit (UnaryExpression node) { return null; }
  public T visit (CastExpression node) { return null; }
  public T visit (DeleteExpression node) { return null; }
  public T visit (NewExpression node) { return null; }
  public T visit (NewInitializer node) { return null; }
  public T visit (DereferencingMemberAccess node) { return null; }
  public T visit (MemberAccess node) { return null; }
  public T visit (RoutineCall node) { return null; }
  public T visit (RoutineArguments node) { return null; }
  public T visit (RoutineArgument node) { return null; }
  public T visit (ArraySubscript node) { return null; }

  // Literals
  public T visit (BooleanLiteral node) { return null; }
  public T visit (CharacterLiteral node) { return null; }
  public T visit (FloatingPointLiteral node) { return null; }
  public T visit (IntegerLiteral node) { return null; }
  public T visit (NullLiteral node) { return null; }
  public T visit (StringLiteral node) { return null; }
  public T visit (UnsignedIntegerLiteral node) { return null; }

  // Special
  public T visit (Name node) { return null; }
  public T visit (This node) { return null; }
  public T visit (ErrorNode node) { return null; }
  public T visit (BogusExpression node) { return null; }

  // NEW TYPES

  public T visit (Declarator node) { return null; }
  public T visit (ArrayDeclarators node) { return null; }
  public T visit (ArrayDeclarator node) { return null; }
  public T visit (PointerDeclarators node) { return null; }
  public T visit (PointerDeclarator node) { return null; }

  public T visit (BogusDeclarator node) { return null; }

  // TYPES

//  public T visit (Type node) { return null; }
  public T visit (NominalType node) { return null; }
  public T visit (PrimitiveType node) { return null; }
  public T visit (RoutinePointerDeclarator node) { return null; }

  public T visit (TemplateInstantiation node) { return null; }
  public T visit (TemplateArguments node) { return null; }
  public T visit (TemplateArgument node) { return null; }

}
