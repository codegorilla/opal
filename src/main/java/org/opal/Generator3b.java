package org.opal;

import org.opal.ast.AstNode;
import org.opal.ast.declaration.*;
import org.opal.ast.expression.*;
import org.opal.ast.statement.*;
import org.opal.ast.type.*;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupDir;

import java.net.URL;
import java.util.LinkedList;

// The purpose of this pass is to create definitions within a module
// implementation unit.

public class Generator3b extends BaseResultVisitor<ST> {

  private final URL templateDirectoryUrl;
  private final STGroupDir group;

  // Stack for facilitating out-of-order operations on types. For example, we
  // need to swap the base type for the variable name in order to form a
  // declarator. We also need to invert the order in which arrays and pointers
  // are processed.
  private final LinkedList<ST> stack = new LinkedList<>();

  // Stack for keeping track of current node path
  private final LinkedList<AstNode> nodePath = new LinkedList<>();

  // Stack for keeping track of current class name
  private final LinkedList<AstNode> classNameStack = new LinkedList<>();

  public Generator3b (AstNode input) {
    super(input);
    templateDirectoryUrl = this.getClass().getClassLoader().getResource("templates");
    group = new STGroupDir(templateDirectoryUrl);
  }

  public ST process () {
    return visit(root);
  }

  public ST visit (AstNode node) {
    nodePath.push(node);
    var st = node.accept(this);
    nodePath.pop();
    return st;
  }

  // DECLARATIONS *************************************************************

  // OTHER DECLARATIONS

  public ST visit (OtherDeclarations node) {
    var st = group.getInstanceOf("implementation/definition/otherDefinitions");
    for (var child : node.getChildren())
      st.add("definition", visit(child));
    return st;
  }

  // Abstract member routines are never defined, so there is no need to map
  // 'abstract' keyword in this pass.

  public ST visit (Modifier node) {
    return new ST(node.getToken().getLexeme());
  }

  // CLASS DECLARATIONS

  public ST visit (ClassDeclaration node) {
      classNameStack.push(node.name());
      var st = visit(node.body());
      classNameStack.pop();
      return st;
  }

  public ST visit (ClassBody node) {
    var st = group.getInstanceOf("implementation/definition/memberFunctionDefinitions");
    for (var child : node.getChildren())
      st.add("definition", visit(child));
    return st;
  }

  // Access specifiers and some modifiers (e.g. final, override, virtual) are
  // not used in module implementation file.

  // To do: Work in progress. Need to add class name to front of name.

  public ST visit (MemberRoutineDeclaration node) {
    var st = group.getInstanceOf("implementation/definition/memberFunctionDefinition");
    if (node.modifiers().hasChildren()) {
      st.add("modifiers1", visit(node.modifiers()));
      st.add("modifiers2", visit(node.modifiers()));
    }
    var className = classNameStack.get(0).getToken().getLexeme();
    st.add("className", className);
    st.add("name", visit(node.name()));
    st.add("parameters", visit(node.parameters()));
    if (node.cvQualifiers().hasChildren())
      st.add("cvQualifiers", visit(node.cvQualifiers()));
    if (node.refQualifiers().hasChildren())
      st.add("refQualifiers", visit(node.refQualifiers()));
    if (node.hasNoexceptSpecifier())
      st.add("noexceptSpecifier", visit(node.noexceptSpecifier()));
    if (node.hasReturnType())
      st.add("returnType", visit(node.returnType()));
    st.add("body", visit(node.body()));
    return st;
  }

  public ST visit (MemberRoutineModifiers node) {
    var st = group.getInstanceOf("common/declaration/memberFunctionModifiers");
    for (var modifier : node.getModifiers()) {
      var kind = modifier.getToken().getKind();
      if (kind == Token.Kind.CONSTEXPR) {
        st.add("modifier", visit(modifier));
      }
    }
    return st;
  }

  public ST visit (CVQualifiers node) {
    var st = group.getInstanceOf("common/declaration/cvQualifiers");
    for (var qualifier : node.getQualifiers())
      st.add("qualifier", visit(qualifier));
    return st;
  }

  public ST visit (CVQualifier node) {
    return new ST(node.getToken().getLexeme());
  }

  public ST visit (RefQualifiers node) {
    var st = group.getInstanceOf("common/declaration/refQualifiers");
    for (var qualifier : node.getQualifiers())
      st.add("qualifier", visit(qualifier));
    return st;
  }

  public ST visit (RefQualifier node) {
    return new ST(node.getToken().getLexeme());
  }

  // TYPEALIAS DECLARATIONS

  public ST visit (LocalTypealiasDeclaration node) {
    var st = group.getInstanceOf("common/declaration/localUsingDeclaration");
    st.add("name", visit(node.name()));
    stack.push(emptyDeclarator());
    st.add("type", visit(node.type()));
    st.add("declarator", stack.pop());
    return st;
  }

  public ST visit (TypealiasName node) {
    return new ST(node.getToken().getLexeme());
  }

  // ROUTINE DECLARATIONS

  public ST visit (RoutineDeclaration node) {
    var st = group.getInstanceOf("implementation/definition/functionDefinition");
    if (node.modifiers().hasChildren())
      st.add("modifiers", visit(node.modifiers()));
    st.add("name", visit(node.name()));
    st.add("parameters", visit(node.parameters()));
    if (node.hasNoexceptSpecifier())
      st.add("noexceptSpecifier", visit(node.noexceptSpecifier()));
    if (node.hasReturnType())
      st.add("returnType", visit(node.returnType()));
    st.add("body", visit(node.body()));
    return st;
  }

  // Some modifiers, such as 'static' and 'virtual' do not appear on
  // the routine definition.

  public ST visit (RoutineModifiers node) {
    var st = group.getInstanceOf("common/declaration/functionModifiers");
    for (var modifier : node.getModifiers()) {
      var kind = modifier.getToken().getKind();
      if (kind == Token.Kind.CONSTEXPR) {
        st.add("modifier", visit(modifier));
      }
    }
    return st;
  }

  public ST visit (RoutineName node) {
    return new ST(node.getToken().getLexeme());
  }

  public ST visit (RoutineParameters node) {
    var st = group.getInstanceOf("common/declaration/functionParameters");
    for (var child : node.getChildren())
      st.add("functionParameter", visit(child));
    return st;
  }

  public ST visit (RoutineParameter node) {
    var st = group.getInstanceOf("common/declaration/functionParameter");
    visit(node.routineParameterName());
    st.add("typeSpecifier", visit(node.routineParameterTypeSpecifier()));
    st.add("declarator", stack.pop());
    return st;
  }

  public ST visit(RoutineParameterName node) {
    var st = group.getInstanceOf("common/declarator/simpleDeclarator");
    st.add("name", node.getToken().getLexeme());
    stack.push(st);
    return null;
  }

  public ST visit (RoutineParameterTypeSpecifier node) {
    var st = group.getInstanceOf("common/declaration/functionParameterTypeSpecifier");
    st.add("type", visit(node.type()));
    return st;
  }

  public ST visit (NoexceptSpecifier node) {
    return new ST(node.getToken().getLexeme());
  }

  public ST visit (RoutineReturnTypeSpecifier node) {
    var st = group.getInstanceOf("common/declaration/functionReturnType");
    stack.push(emptyDeclarator());
    st.add("type", visit(node.type()));
    st.add("declarator", stack.pop());
    return st;
  }

  public ST visit (RoutineBody node) {
    var st = group.getInstanceOf("common/declaration/functionBody");
    st.add("compoundStatement", visit(node.compoundStatement()));
    return st;
  }

  private ST emptyDeclarator () {
    return new ST("");
  }

  // VARIABLE DECLARATIONS

  // In C++, any pointer or raw array portions of the type specifier are transferred to the name to form a so-called
  // "declarator".

  // The variable name is placed on a stack, transformed into a declarator, and then retrieved from the stack. Since
  // the stack never has more than one element, it doesn't actually need to be a stack.

  // Gen3a handles global variable definitions as part of declarations.

  public ST visit (LocalVariableDeclaration node) {
    var st = group.getInstanceOf("common/declaration/localVariableDeclaration");
    if (node.modifiers().hasChildren())
      st.add("modifiers", visit(node.modifiers()));
    stack.push(visit(node.name()));
    if (node.hasTypeSpecifier())
      st.add("typeSpecifier", visit(node.typeSpecifier()));
    else
      st.add("typeSpecifier", "auto");
    st.add("declarator", stack.pop());
    if (node.hasInitializer())
      st.add("initializer", visit(node.initializer()));
    return st;
  }

  public ST visit (VariableModifiers node) {
    var st = group.getInstanceOf("common/declaration/variableModifiers");
    for (var modifier : node.modifiers())
      st.add("modifier", visit(modifier));
    return st;
  }

  // The variable name becomes a "simple declarator", which is the core of the overall C++ declarator that gets built
  // up. A C++ declaration is of the form "typeSpecifier declarator", which is essentially the reverse of the cobolt
  // declaration of the form "var variableName: typeSpecifier" (setting aside the fact that the term "type specifier"
  // has a different interpretation between the two). Due to the need to swap the variable name with the base type from
  // the type specifier, and that the base type may be several levels down in the type expression tree, we will use an
  // explicit stack to facilitate the exchange.

  public ST visit (VariableName node) {
    var st = group.getInstanceOf("common/declarator/simpleDeclarator");
    st.add("name", node.getToken().getLexeme());
    return st;
  }

  public ST visit (VariableTypeSpecifier node) {
    var st = group.getInstanceOf("common/declaration/variableTypeSpecifier");
    st.add("type", visit(node.getDeclarator()));
    return st;
  }

  public ST visit (VariableInitializer node) {
    if (node.hasChildren()) {
      var st = group.getInstanceOf("common/declaration/variableInitializer");
      st.add("expression", visit(node.expression()));
      return st;
    }
    else
      return null;
  }

  // STATEMENTS ***************************************************************

  public ST visit (CompoundStatement node) {
    var st = group.getInstanceOf("implementation/statement/compoundStatement");
    for (var child : node.getChildren())
      st.add("statement", visit(child));
    return st;
  }

  public ST visit (BreakStatement node) {
    return group.getInstanceOf("implementation/statement/breakStatement");
  }

  public ST visit (ContinueStatement node) {
    return group.getInstanceOf("implementation/statement/continueStatement");
  }

  public ST visit(DoUntilStatement node) {
    var st = group.getInstanceOf("implementation/statement/doUntilStatement");
    st.add("untilCondition", visit(node.untilCondition()));
    st.add("untilBody", visit(node.untilBody()));
    return st;
  }

  public ST visit(DoWhileStatement node) {
    var st = group.getInstanceOf("implementation/statement/doWhileStatement");
    st.add("whileCondition", visit(node.whileCondition()));
    st.add("whileBody", visit(node.whileBody()));
    return st;
  }

  public ST visit (ExpressionStatement node) {
    var st = group.getInstanceOf("implementation/statement/expressionStatement");
    st.add("expression", visit(node.expression()));
    return st;
  }

  public ST visit (ForStatement node) {
    var st = group.getInstanceOf("implementation/statement/rangeBasedForStatement");
    st.add("name", visit(node.name()));
    st.add("expression", visit(node.expression()));
    st.add("forBody", visit(node.forBody()));
    return st;
  }

  // If condition and body are passthroughs

  public ST visit (IfStatement node) {
    var st = group.getInstanceOf("implementation/statement/ifStatement");
    st.add("ifCondition", visit(node.ifCondition()));
    st.add("ifBody", visit(node.ifBody()));
    if (node.getChildCount() == 3)
      st.add("elseClause", visit(node.elseClause()));
    return st;
  }

  public ST visit (ElseClause node) {
    var st = group.getInstanceOf("implementation/statement/elseClause");
    st.add("elseBody", visit(node.elseBody()));
    return st;
  }

  public ST visit (LoopStatement node) {
    var st = group.getInstanceOf("implementation/statement/forStatement");
    if (node.hasLoopControl())
      st.add("forControl", visit(node.loopControl()));
    else
      st.add("forControl", "(;;)");
    st.add("forBody", visit(node.loopBody()));
    return st;
  }

  public ST visit (LoopControl node) {
    var st = group.getInstanceOf("implementation/statement/forControl");
    if (node.hasLoopInitializer())
      st.add("forInitializer", visit(node.forInitializer()));
    if (node.hasLoopCondition())
      st.add("forCondition", visit(node.forCondition()));
    if (node.hasLoopUpdate())
      st.add("forUpdate", visit(node.forUpdate()));
    return st;
  }

  public ST visit (LoopInitializer node) {
    return visit(node.expression());
  }

  public ST visit (LoopCondition node) {
    return visit(node.expression());
  }

  public ST visit (LoopUpdate node) {
    return visit(node.expression());
  }

  public ST visit (ReturnStatement node) {
    var st = group.getInstanceOf("implementation/statement/returnStatement");
    if (node.hasChildren())
      st.add("expression", visit(node.expression()));
    return st;
  }

  // Until condition and body are passthroughs

  public ST visit (UntilStatement node) {
    var st = group.getInstanceOf("implementation/statement/untilStatement");
    st.add("untilCondition", visit(node.untilCondition()));
    st.add("untilBody", visit(node.untilBody()));
    return st;
  }

  // While condition and body are passthroughs

  public ST visit (WhileStatement node) {
    var st = group.getInstanceOf("implementation/statement/whileStatement");
    st.add("whileCondition", visit(node.whileCondition()));
    st.add("whileBody", visit(node.whileBody()));
    return st;
  }

  // EXPRESSIONS **************************************************************

  public ST visit (Expression node) {
    var st = group.getInstanceOf("common/expression/expression");
    st.add("value", visit(node.getChild(0)));
    return st;
  }

  // Perhaps if we know this is the root node, we don't need parenthesis.

  public ST visit (BinaryExpression node) {
    var st = group.getInstanceOf("common/expression/binaryExpression");
    st.add("operation", node.getToken().getLexeme());
    st.add("leftExpression",  visit(node.leftExpression()));
    st.add("rightExpression", visit(node.rightExpression()));
    return st;
  }

  public ST visit (UnaryExpression node) {
    var st = group.getInstanceOf("common/expression/unaryExpression");
    st.add("operation", node.getToken().getLexeme());
    st.add("expression",  visit(node.expression()));
    return st;
  }

  // To do: Set operation based on lexeme

  public ST visit (CastExpression node) {
    var st = group.getInstanceOf("common/expression/castExpression");
    var operation = switch (node.getToken().getKind()) {
      case Token.Kind.CAST -> "static_cast";
      case Token.Kind.DIVINE -> "dynamic_cast";
      case Token.Kind.TRANSMUTE -> "reinterpret_cast";
      default -> null;
    };
    st.add("operation", operation);
    st.add("type", visit(node.type()));
    st.add("expression", visit(node.expression()));
    return st;
  }

  public ST visit (DeleteExpression node) {
    var st = group.getInstanceOf("common/expression/deleteExpression");
    st.add("arrayFlag", node.getArrayFlag());
    st.add("expression", visit(node.expression()));
    return st;
  }


  public ST visit (NewExpression node) {
    var st = group.getInstanceOf("common/expression/newExpression");
    stack.push(emptyDeclarator());
    st.add("type", visit(node.type()));
    st.add("declarator", stack.pop());
    if (node.hasInitializer())
      st.add("initializer", visit(node.initializer()));
    return st;
  }

  public ST visit (NewInitializer node) {
    var st = group.getInstanceOf("common/expression/newInitializer");
    for (var argument : node.arguments())
      st.add("argument", visit(argument));
    return st;
  }

  public ST visit (ArraySubscript node) {
    var st = group.getInstanceOf("common/expression/arraySubscript");
    st.add("name", visit(node.name()));
    st.add("subscript", visit(node.subscript()));
    return st;
  }

  public ST visit (DereferencingMemberAccess node) {
    var st = group.getInstanceOf("common/expression/dereferencingMemberAccess");
    st.add("name", visit(node.name()));
    st.add("member", visit(node.member()));
    return st;
  }

  public ST visit (MemberAccess node) {
    var st = group.getInstanceOf("common/expression/memberAccess");
    st.add("name", visit(node.name()));
    st.add("member", visit(node.member()));
    return st;
  }

  public ST visit (RoutineCall node) {
    var st = group.getInstanceOf("common/expression/functionCall");
    st.add("name", visit(node.name()));
    st.add("arguments", visit(node.routineArguments()));
    return st;
  }

  public ST visit (RoutineArguments node) {
    var st = group.getInstanceOf("common/expression/functionArguments");
    for (var routineArgument : node.getChildren())
      st.add("argument", visit(routineArgument));
    return st;
  }

  public ST visit (RoutineArgument node) {
    var st = group.getInstanceOf("common/expression/functionArgument");
    st.add("expression", visit(node.expression()));
    return st;
  }

  public ST visit (Name node) {
    var st = group.getInstanceOf("common/expression/name");
    st.add("value", node.getToken().getLexeme());
    return st;
  }

  public ST visit (NullLiteral node) {
    var st = group.getInstanceOf("common/expression/expression");
    st.add("value", "nullptr");
    return st;
  }

  public ST visit (BooleanLiteral node) {
    var st = group.getInstanceOf("common/expression/expression");
    st.add("value", node.getToken().getLexeme());
    return st;
  }

  public ST visit (CharacterLiteral node) {
    var st = group.getInstanceOf("common/expression/expression");
    st.add("value", node.getToken().getLexeme());
    return st;
  }

  public ST visit (FloatingPointLiteral node) {
    var st = group.getInstanceOf("common/expression/expression");
    st.add("value", node.getToken().getLexeme());
    return st;
  }

  public ST visit (IntegerLiteral node) {
    var st = group.getInstanceOf("common/expression/expression");
    st.add("value", node.getToken().getLexeme());
    return st;
  }

  public ST visit (StringLiteral node) {
    var st = group.getInstanceOf("common/expression/expression");
    st.add("value", node.getToken().getLexeme());
    return st;
  }

  // TYPES ********************************************************************

  /*
  public ST visit (ArrayType node) {
    var st = group.getInstanceOf("common/declarator/arrayDeclarator");
    st.add("cop", nodePath.get(1) instanceof PointerType);
    st.add("directDeclarator", stack.pop());
    if (node.getChildCount() == 2)
      st.add("expression", visit(node.expression()));
    stack.push(st);
    return visit(node.baseType());
  }
  */

  public ST visit (NominalType node) {
    var st = group.getInstanceOf("common/type/nominalType");
    st.add("name", node.getToken().getLexeme());
    return st;
  }

  public ST visit (TemplateInstantiation node) {
    var st = group.getInstanceOf("common/type/templateInstantiation");
    st.add("name", visit(node.getChild(0)));
    st.add("arguments", visit(node.getChild(1)));
    return st;
  }

  public ST visit (TemplateArguments node) {
    var st = group.getInstanceOf("common/type/templateArguments");
    for (var child : node.getChildren())
      st.add("argument", visit(child));
    return st;
  }

  public ST visit (TemplateArgument node) {
    // Put empty declarator on the stack
    stack.push(new ST(""));
    var st = group.getInstanceOf("common/type/templateArgument");
    st.add("type", visit(node.getChild(0)));
    st.add("declarator", stack.pop());
    return st;
  }

  // To do: Another thing we need to do is determine what kind of template
  // argument it is. If it is a type argument, then we need to parse it as a
  // type; whereas if it is a non-type argument (e.g. variable), then we need
  // to parse it as an expression. This determination can be made via symbol
  // table lookup, but for now just assume it is a type argument.

  /*
  public ST visit (PointerType node) {
    var st = group.getInstanceOf("common/declarator/pointerDeclarator");
    st.add("directDeclarator", stack.pop());
    stack.push(st);
    return visit(node.baseType());
  }
   */

  public ST visit (PrimitiveType node) {
    var st = group.getInstanceOf("common/type/primitiveType");
    st.add("name", node.getToken().getLexeme());
    return st;
  }

}
