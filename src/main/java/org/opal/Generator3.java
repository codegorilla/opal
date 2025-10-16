package org.opal;

import org.opal.ast.AstNode;
import org.opal.ast.TranslationUnit;
import org.opal.ast.declaration.*;
import org.opal.ast.expression.*;
import org.opal.ast.statement.*;
import org.opal.ast.type.*;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupDir;

import java.net.URL;
import java.util.LinkedList;

// The purpose of this pass is to create function prototypes within a module implementation unit.

public class Generator3 extends ResultBaseVisitor <ST> {

  private final URL templateDirectoryUrl;
  private final STGroupDir group;

  // Stack for facilitating out-of-order operations. For example, we need to swap the base type for the variable name in
  // order to form a declarator. We also need to invert the order in which arrays and pointers are processed.
  private final LinkedList<ST> stack = new LinkedList<>();

  // Stack for keeping track of current node path
  private final LinkedList<AstNode> ancestorStack = new LinkedList<>();

  private int pass = 1;

  public Generator3 (AstNode input) {
    super(input);
    templateDirectoryUrl = this.getClass().getClassLoader().getResource("templates/implementation");
    group = new STGroupDir(templateDirectoryUrl);
  }

  public void process () {
    visit(root);
  }

  public ST visit (AstNode node) {
    ancestorStack.push(node);
    var st = node.accept(this);
    ancestorStack.pop();
    return st;
  }

  public ST visit (TranslationUnit node) {
    var st = group.getInstanceOf("translationUnit");
    st.add("packageDeclaration", visit(node.packageDeclaration()));
    st.add("declarations", visit(node.declarations()));
    pass += 1;
    st.add("declarations", visit(node.declarations()));
    System.out.println("---");
    System.out.println(st.render());
    return null;
  }

  // DECLARATIONS *************************************************************

  // PACKAGE DECLARATIONS

  // Package declaration is special in that there is only one, and it must appear at the top of the translation unit.

  public ST visit (PackageDeclaration node) {
    var st = group.getInstanceOf("declaration/packageDeclaration");
    st.add("packageName", visit(node.packageName()));
    return st;
  }

  // For now just support single word package names

  public ST visit (PackageName node) {
    var st = group.getInstanceOf("declaration/packageName");
    st.add("name", node.getToken().getLexeme());
    return st;
  }

  // COMMON DECLARATIONS

  public ST visit (Declarations node) {
    var st = group.getInstanceOf("declaration/declarations");
    for (var child : node.getChildren())
      st.add("declaration", visit(child));
    return st;
  }

  // To do: Might need a modifier table to map Opal modifiers to C++ modifiers

  public ST visit (Modifiers node) {
    var st = group.getInstanceOf("declaration/modifiers");
    for (var modifier : node.getModifiers())
      st.add("modifier", visit(modifier));
    return st;
  }

  public ST visit (Modifier node) {
    return new ST(node.getToken().getLexeme());
  }

  // CLASS DECLARATIONS

  // Might need declarations and definitions passes just as with routines

  public ST visit (ClassDeclaration node) {
    return (pass == 1) ? classDeclaration(node) : null;
  }

  public ST classDeclaration (ClassDeclaration node) {
    if (node.hasExportSpecifier()) {
      var st = group.getInstanceOf("declaration/classDeclaration");
      st.add("className", visit(node.className()));
      if (node.hasClassExtendsClause()) {
        st.add("classExtendsClause", visit(node.classExtendsClause()));
      }
      st.add("classBody", visit(node.classBody()));
      return st;
    }
    else
      return null;
  }

  public ST visit (ClassName node) {
    var st = group.getInstanceOf("declaration/className");
    st.add("identifier", node.getToken().getLexeme());
    return st;
  }

  public ST visit (ClassExtendsClause node) {
    var st = group.getInstanceOf("declaration/classExtendsClause");
    st.add("baseClasses", visit(node.baseClasses()));
    return st;
  }

  public ST visit (BaseClasses node) {
    var st = group.getInstanceOf("declaration/baseClasses");
    for (var child : node.getChildren())
      st.add("baseClass", visit(child));
    return st;
  }

  public ST visit (BaseClass node) {
    var st = group.getInstanceOf("declaration/baseClass");
    st.add("name", node.getToken().getLexeme());
    return st;
  }

  public ST visit (ClassBody node) {
    var st = group.getInstanceOf("declaration/classBody");
    for (var child : node.getChildren())
      st.add("memberDeclaration", visit(child));
    return st;
  }

  public ST visit (MemberAccessSpecifier node) {
    var st = group.getInstanceOf("declaration/memberAccessSpecifier");
    st.add("value", node.getToken().getLexeme());
    return st;
  }

  public ST visit (MemberVariableDeclaration node) {
    var st = group.getInstanceOf("declaration/memberVariableDeclaration");
    if (node.hasAccessSpecifier())
      st.add("accessSpecifier", visit(node.accessSpecifier()));
    else
      st.add("accessSpecifier", "public");
    stack.push(visit(node.variableName()));
    if (node.hasVariableTypeSpecifier())
      st.add("typeSpecifier", visit(node.variableTypeSpecifier()));
    st.add("declarator", stack.pop());
//    node.getModifiers().accept(this);
    if (node.hasVariableInitializer())
      st.add("initializer", visit(node.variableInitializer()));
    return st;
  }

  public ST visit (MemberRoutineDeclaration node) {
    var st = group.getInstanceOf("declaration/memberFunctionDeclaration");
    if (node.hasAccessSpecifier())
      st.add("accessSpecifier", visit(node.accessSpecifier()));
    else
      st.add("accessSpecifier", "public");
    st.add("functionName", visit(node.routineName()));
    st.add("functionParameters", visit(node.routineParameters()));
    st.add("functionReturnType", visit(node.routineReturnType()));
    return st;
  }

  // ROUTINE DECLARATIONS

  public ST visit (RoutineDeclaration node) {
    var st = switch (pass) {
      case 1 -> routineDeclarationPass1(node);
      case 2 -> routineDeclarationPass2(node);
      default -> null;
    };
    return st;
  }

  // Modifiers can go in several possible locations:
  //   beginning: constexpr, virtual
  //   middle: const, volatile (aka CV qualifiers)
  //   end: final, override

  // To do: Might need to put conditional on the return type

  private ST routineDeclarationPass1 (RoutineDeclaration node) {
    if (node.hasExportSpecifier()) {
      var st = group.getInstanceOf("declaration/functionDeclaration");
      if (node.modifiers().hasChildren())
        st.add("modifiers", visit(node.modifiers()));
      st.add("name", visit(node.name()));
      st.add("parameters", visit(node.parameters()));
      st.add("returnType", visit(node.returnType()));
      return st;
    } else {
      return null;
    }
  }

  private ST routineDeclarationPass2 (RoutineDeclaration node) {
    var st = group.getInstanceOf("declaration/functionDefinition");
    if (node.modifiers().hasChildren())
      st.add("modifiers", visit(node.modifiers()));
    st.add("name", visit(node.name()));
    st.add("parameters", visit(node.parameters()));
    if (node.hasRoutineReturnType())
      st.add("returnType", visit(node.returnType()));
    st.add("body", visit(node.body()));
    return st;
  }

  public ST visit (RoutineName node) {
    return new ST(node.getToken().getLexeme());
  }

  public ST visit (RoutineParameters node) {
    var st = group.getInstanceOf("declaration/functionParameters");
    for (var child : node.getChildren())
      st.add("functionParameter", visit(child));
    return st;
  }

  public ST visit (RoutineParameter node) {
    var st = group.getInstanceOf("declaration/functionParameter");
    visit(node.routineParameterName());
    st.add("typeSpecifier", visit(node.routineParameterTypeSpecifier()));
    st.add("declarator", stack.pop());
    return st;
  }

  public ST visit(RoutineParameterName node) {
    var st = group.getInstanceOf("declarator/simpleDeclarator");
    st.add("name", node.getToken().getLexeme());
    stack.push(st);
    return null;
  }

  public ST visit (RoutineParameterTypeSpecifier node) {
    var st = group.getInstanceOf("declaration/functionParameterTypeSpecifier");
    st.add("type", visit(node.type()));
    return st;
  }

  public ST visit (RoutineReturnType node) {
    var st = group.getInstanceOf("declaration/functionReturnType");
    stack.push(emptyDeclarator());
    st.add("type", visit(node.type()));
    st.add("declarator", stack.pop());
    return st;
  }

  public ST visit (RoutineBody node) {
    var st = group.getInstanceOf("declaration/functionBody");
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

  public ST visit (VariableDeclaration node) {
    return (pass == 2) ? variableDeclaration(node) : null;
  }

  // Some modifiers go in front, so go in other places. Need to account for all
  // possible cases.

  public ST variableDeclaration (VariableDeclaration node) {
    if (node.hasExportSpecifier()) {
      var st = group.getInstanceOf("declaration/variableDeclaration");
      if (node.modifiers().hasChildren())
        st.add("modifiers", visit(node.modifiers()));
      stack.push(visit(node.variableName()));
      if (node.hasVariableTypeSpecifier())
        st.add("typeSpecifier", visit(node.variableTypeSpecifier()));
      st.add("declarator", stack.pop());
      if (node.hasVariableInitializer())
        st.add("initializer", visit(node.variableInitializer()));
      return st;
    }
    else
      return null;
  }

  public ST visit (LocalVariableDeclaration node) {
    var st = group.getInstanceOf("declaration/localVariableDeclaration");
    stack.push(visit(node.variableName()));
    if (node.hasVariableTypeSpecifier())
      st.add("typeSpecifier", visit(node.variableTypeSpecifier()));
    st.add("declarator", stack.pop());
    if (node.hasVariableInitializer())
      st.add("initializer", visit(node.variableInitializer()));
    return st;
  }

  // The variable name becomes a "simple declarator", which is the core of the overall C++ declarator that gets built
  // up. A C++ declaration is of the form "typeSpecifier declarator", which is essentially the reverse of the cobolt
  // declaration of the form "var variableName: typeSpecifier" (setting aside the fact that the term "type specifier"
  // has a different interpretation between the two). Due to the need to swap the variable name with the base type from
  // the type specifier, and that the base type may be several levels down in the type expression tree, we will use an
  // explicit stack to facilitate the exchange.

  public ST visit (VariableName node) {
    var st = group.getInstanceOf("declarator/simpleDeclarator");
    st.add("name", node.getToken().getLexeme());
    return st;
  }

  public ST visit (VariableTypeSpecifier node) {
    var st = group.getInstanceOf("declaration/variableTypeSpecifier");
    st.add("type", visit(node.type()));
    return st;
  }

  public ST visit (VariableInitializer node) {
    if (node.hasChildren()) {
      var st = group.getInstanceOf("declaration/variableInitializer");
      st.add("expression", visit(node.expression()));
      return st;
    }
    else
      return null;
  }

  // STATEMENTS ***************************************************************

  public ST visit (CompoundStatement node) {
    var st = group.getInstanceOf("statement/compoundStatement");
    for (var child : node.getChildren())
      st.add("statement", visit(child));
    return st;
  }

  public ST visit (BreakStatement node) {
    return group.getInstanceOf("statement/breakStatement");
  }

  public ST visit (ContinueStatement node) {
    return group.getInstanceOf("statement/continueStatement");
  }

  public ST visit(DoUntilStatement node) {
    var st = group.getInstanceOf("statement/doUntilStatement");
    st.add("untilCondition", visit(node.untilCondition()));
    st.add("untilBody", visit(node.untilBody()));
    return st;
  }

  public ST visit(DoWhileStatement node) {
    var st = group.getInstanceOf("statement/doWhileStatement");
    st.add("whileCondition", visit(node.whileCondition()));
    st.add("whileBody", visit(node.whileBody()));
    return st;
  }

  public ST visit (ExpressionStatement node) {
    var st = group.getInstanceOf("statement/expressionStatement");
    st.add("expression", visit(node.expression()));
    return st;
  }

  public ST visit (ForStatement node) {
    var st = group.getInstanceOf("statement/rangeBasedForStatement");
    st.add("name", visit(node.name()));
    st.add("expression", visit(node.expression()));
    st.add("forBody", visit(node.forBody()));
    return st;
  }

  // If condition and body are passthroughs

  public ST visit (IfStatement node) {
    var st = group.getInstanceOf("statement/ifStatement");
    st.add("ifCondition", visit(node.ifCondition()));
    st.add("ifBody", visit(node.ifBody()));
    if (node.getChildCount() == 3)
      st.add("elseClause", visit(node.elseClause()));
    return st;
  }

  public ST visit (ElseClause node) {
    var st = group.getInstanceOf("statement/elseClause");
    st.add("elseBody", visit(node.elseBody()));
    return st;
  }

  public ST visit (LoopStatement node) {
    var st = group.getInstanceOf("statement/forStatement");
    if (node.hasLoopControl())
      st.add("forControl", visit(node.loopControl()));
    else
      st.add("forControl", "(;;)");
    st.add("forBody", visit(node.loopBody()));
    return st;
  }

  public ST visit (LoopControl node) {
    var st = group.getInstanceOf("statement/forControl");
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
    var st = group.getInstanceOf("statement/returnStatement");
    if (node.hasChildren())
      st.add("expression", visit(node.expression()));
    return st;
  }

  // Until condition and body are passthroughs

  public ST visit (UntilStatement node) {
    var st = group.getInstanceOf("statement/untilStatement");
    st.add("untilCondition", visit(node.untilCondition()));
    st.add("untilBody", visit(node.untilBody()));
    return st;
  }

  // While condition and body are passthroughs

  public ST visit (WhileStatement node) {
    var st = group.getInstanceOf("statement/whileStatement");
    st.add("whileCondition", visit(node.whileCondition()));
    st.add("whileBody", visit(node.whileBody()));
    return st;
  }

  // EXPRESSIONS **************************************************************

  public ST visit (Expression node) {
    var st = group.getInstanceOf("expression/expression");
    st.add("value", visit(node.getChild(0)));
    return st;
  }

  // Perhaps if we know this is the root node, we don't need parenthesis.

  public ST visit (BinaryExpression node) {
    var st = group.getInstanceOf("expression/binaryExpression");
    st.add("operation", node.getToken().getLexeme());
    st.add("leftExpression",  visit(node.leftExpression()));
    st.add("rightExpression", visit(node.rightExpression()));
    return st;
  }

  public ST visit (UnaryExpression node) {
    var st = group.getInstanceOf("expression/unaryExpression");
    st.add("operation", node.getToken().getLexeme());
    st.add("expression",  visit(node.expression()));
    return st;
  }

  // To do: Set operation based on lexeme

  public ST visit (CastExpression node) {
    var st = group.getInstanceOf("expression/castExpression");
    var operation = switch(node.getToken().getLexeme()) {
      case "cast" -> "static_cast";
      case "divine" -> "dynamic_cast";
      case "transmute" -> "reinterpret_cast";
      default -> null;
    };
    st.add("operation", operation);
    st.add("type", visit(node.type()));
    st.add("expression", visit(node.expression()));
    return st;
  }

  public ST visit (ArraySubscript node) {
    var st = group.getInstanceOf("expression/arraySubscript");
    st.add("name", visit(node.name()));
    st.add("subscript", visit(node.subscript()));
    return st;
  }

  public ST visit (DereferencingMemberAccess node) {
    var st = group.getInstanceOf("expression/dereferencingMemberAccess");
    st.add("name", visit(node.name()));
    st.add("member", visit(node.member()));
    return st;
  }

  public ST visit (MemberAccess node) {
    var st = group.getInstanceOf("expression/memberAccess");
    st.add("name", visit(node.name()));
    st.add("member", visit(node.member()));
    return st;
  }

  public ST visit (RoutineCall node) {
    var st = group.getInstanceOf("expression/functionCall");
    st.add("name", visit(node.name()));
    st.add("arguments", visit(node.routineArguments()));
    return st;
  }

  public ST visit (RoutineArguments node) {
    var st = group.getInstanceOf("expression/functionArguments");
    for (var routineArgument : node.getChildren())
      st.add("argument", visit(routineArgument));
    return st;
  }

  public ST visit (RoutineArgument node) {
    var st = group.getInstanceOf("expression/functionArgument");
    st.add("expression", visit(node.expression()));
    return st;
  }

  public ST visit (Name node) {
    var st = group.getInstanceOf("expression/name");
    st.add("value", node.getToken().getLexeme());
    return st;
  }

  public ST visit (NullLiteral node) {
    var st = group.getInstanceOf("expression/expression");
    st.add("value", "nullptr");
    return st;
  }

  public ST visit (BooleanLiteral node) {
    var st = group.getInstanceOf("expression/expression");
    st.add("value", node.getToken().getLexeme());
    return st;
  }

  public ST visit (CharacterLiteral node) {
    var st = group.getInstanceOf("expression/expression");
    st.add("value", node.getToken().getLexeme());
    return st;
  }

  public ST visit (FloatingPointLiteral node) {
    var st = group.getInstanceOf("expression/expression");
    st.add("value", node.getToken().getLexeme());
    return st;
  }

  public ST visit (IntegerLiteral node) {
    var st = group.getInstanceOf("expression/expression");
    st.add("value", node.getToken().getLexeme());
    return st;
  }

  public ST visit (StringLiteral node) {
    var st = group.getInstanceOf("expression/expression");
    st.add("value", node.getToken().getLexeme());
    return st;
  }

  // TYPES ********************************************************************

  public ST visit (ArrayType node) {
    var st = group.getInstanceOf("declarator/arrayDeclarator");
    st.add("directDeclarator", stack.pop());
    if (node.getChildCount() == 2)
      st.add("expression", visit(node.expression()));
    stack.push(st);
    return visit(node.baseType());
  }

  public ST visit (NominalType node) {
    var st = group.getInstanceOf("type/nominalType");
    st.add("name", node.getToken().getLexeme());
    return st;
  }

  public ST visit (TemplateInstantiation node) {
    var st = group.getInstanceOf("type/templateInstantiation");
    st.add("name", visit(node.getChild(0)));
    st.add("arguments", visit(node.getChild(1)));
    return st;
  }

  public ST visit (TemplateArguments node) {
    var st = group.getInstanceOf("type/templateArguments");
    for (var child : node.getChildren())
      st.add("argument", visit(child));
    return st;
  }

  public ST visit (TemplateArgument node) {
    // Put empty declarator on the stack
    stack.push(new ST(""));
    var st = group.getInstanceOf("type/templateArgument");
    st.add("type", visit(node.getChild(0)));
    st.add("declarator", stack.pop());
    return st;
  }

  // We need to be able to tell if we are inside template arguments. This could be done by using parent links or by
  // pushing nodes onto a stack and traversing the stack until a template argument is found or the root node is reached.
  // Update: Doesn't seem to be needed anymore. We push an empty declarator, so it can be treated the same regardless
  // of whether we are in template or not.

  // To do: Another thing we need to do is determine what kind of template argument it is. If it is a type argument,
  // then we need to parse it as a type; whereas if it is a non-type argument (e.g. variable), then we need to parse it
  // as an expression. This determination can be made via symbol table lookup, but for now just assume it is a type
  // argument.

  public ST visit (PointerType node) {
    var st = group.getInstanceOf("declarator/pointerDeclarator");
    st.add("directDeclarator", stack.pop());
    stack.push(st);
    return visit(node.baseType());
  }

  // To do: See if we can eliminate gratuitous parenthesis. If the PointerType node is at the top of the expression tree
  // then we don't need parentheses. There may be other situations too, such as consecutive pointers and consecutive
  // arrays.

  public ST visit (PrimitiveType node) {
    var st = group.getInstanceOf("type/primitiveType");
    st.add("name", node.getToken().getLexeme());
    return st;
  }

}
