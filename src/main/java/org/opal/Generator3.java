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

  private int pass = 2;

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

  // Access specifier should exist, but if it is implicit, then it won't have a token unless we artificially add one
  // during semantic analysis.

  // If we want different behavior for variables vs. routines, then we need to find a way to differentiate access
  // specifiers. The problem is that the access specifier is encountered in the parser before we know which construct
  // it belongs to. So we must defer differentiation. Nevertheless, we can either mark it in the parser from within the
  // corresponding construct rule or we can use a separate semantic analysis pass. We could also use K>1 lookahead.

  /*
  public ST visit (AccessSpecifier node) {
    ST st = null;
    var token = node.getToken();
    if (token != null && token.getKind() == Token.Kind.PUBLIC) {
      st = group.getInstanceOf("declaration/accessSpecifier");
      st.add("value", "export");
    }
    // This is how we can tell what kind of access specifier we have.
//    if (node.getKind() == AccessSpecifier.VARIABLE)
//      System.out.println("THIS IS AN INSTANCE OF A VARIABLE DECLARATION!");
    return st;
  }
  */

  public ST visit (Modifiers node) {
    System.out.println("Modifiers");
    return null;
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

  // To do: Might need to put conditional on the return type

  private ST routineDeclarationPass1 (RoutineDeclaration node) {
      var st = group.getInstanceOf("declaration/functionDeclaration");
      st.add("functionName", visit(node.routineName()));
      st.add("functionParameters", visit(node.routineParameters()));
      st.add("functionReturnType", visit(node.routineReturnType()));
      return st;
  }

  private ST routineDeclarationPass2 (RoutineDeclaration node) {
    var st = group.getInstanceOf("declaration/functionDefinition");
    st.add("functionName", visit(node.routineName()));
    st.add("functionParameters", visit(node.routineParameters()));
    if (node.hasRoutineReturnType())
      st.add("functionReturnType", visit(node.routineReturnType()));
    st.add("functionBody", visit(node.routineBody()));
    return st;
  }

  public ST visit (RoutineName node) {
    var st = group.getInstanceOf("declaration/functionName");
    st.add("identifier", node.getToken().getLexeme());
    return st;
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
    var token = node.accessSpecifier().getToken();
    if (token == null || token.getKind() == Token.Kind.PRIVATE) {
      var st = group.getInstanceOf("declaration/variableDeclaration");
      stack.push(visit(node.variableName()));
      if (node.variableTypeSpecifier() != null)
        st.add("typeSpecifier", visit(node.variableTypeSpecifier()));
      st.add("declarator", stack.pop());
//    node.getModifiers().accept(this);
      if (node.variableInitializer() != null)
        st.add("initializer", visit(node.variableInitializer()));
      return st;
    }
    else
      return null;
  }

  public ST visit (LocalVariableDeclaration node) {
    var st = group.getInstanceOf("declaration/localVariableDeclaration");
    stack.push(visit(node.variableName()));
    if (node.variableTypeSpecifier() != null)
      st.add("typeSpecifier", visit(node.variableTypeSpecifier()));
    st.add("declarator", stack.pop());
    if (node.variableInitializer() != null)
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
    var st = group.getInstanceOf("statement/breakStatement");
    return st;
  }

  public ST visit (ContinueStatement node) {
    var st = group.getInstanceOf("statement/continueStatement");
    return st;
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
    var st = group.getInstanceOf("statement/forStatement");
    if (node.hasForInitializer())
      st.add("forInitializer", visit(node.forInitializer()));
    if (node.hasForCondition())
      st.add("forCondition", visit(node.forCondition()));
    if (node.hasForUpdate())
      st.add("forUpdate", visit(node.forUpdate()));
    st.add("forBody", visit(node.forBody()));
    return st;
  }

  public ST visit (ForInitializer node) {
    return visit(node.expression());
  }

  public ST visit (ForCondition node) {
    return visit(node.expression());
  }

  public ST visit (ForUpdate node) {
    return visit(node.expression());
  }

  public ST visit (ForeachStatement node) {
    var st = group.getInstanceOf("statement/foreachStatement");
    st.add("name", visit(node.name()));
    st.add("expression", visit(node.expression()));
    st.add("foreachBody", visit(node.foreachBody()));
    return st;
  }

  // If body is a passthrough

  public ST visit (IfStatement node) {
    var st = group.getInstanceOf("statement/ifStatement");
    st.add("ifCondition", visit(node.ifCondition()));
    st.add("ifBody", visit(node.ifBody()));
    if (node.getChildCount() == 3)
      st.add("elseClause", visit(node.elseClause()));
    return st;
  }

  public ST visit (IfCondition node) {
    var st = group.getInstanceOf("statement/ifCondition");
    st.add("expression", visit(node.expression()));
    return st;
  }

  public ST visit (ElseClause node) {
    var st = group.getInstanceOf("statement/elseClause");
    st.add("elseBody", visit(node.elseBody()));
    return st;
  }

  public ST visit (ReturnStatement node) {
    var st = group.getInstanceOf("statement/returnStatement");
    if (node.hasChildren())
      st.add("expression", visit(node.expression()));
    return st;
  }

  // Until body is a passthrough

  public ST visit (UntilStatement node) {
    var st = group.getInstanceOf("statement/untilStatement");
    st.add("untilCondition", visit(node.untilCondition()));
    st.add("untilBody", visit(node.untilBody()));
    return st;
  }

  public ST visit (UntilCondition node) {
    var st = group.getInstanceOf("statement/untilCondition");
    st.add("expression", visit(node.expression()));
    return st;
  }

  // While body is a passthrough

  public ST visit (WhileStatement node) {
    var st = group.getInstanceOf("statement/whileStatement");
    st.add("whileCondition", visit(node.whileCondition()));
    st.add("whileBody", visit(node.whileBody()));
    return st;
  }

  public ST visit (WhileCondition node) {
    var st = group.getInstanceOf("statement/whileCondition");
    st.add("expression", visit(node.expression()));
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

  public ST visit (ArraySubscript node) {
    var st = group.getInstanceOf("expression/arraySubscript");
    st.add("array", visit(node.array()));
    st.add("subscript", visit(node.subscript()));
    return st;
  }

  public ST visit (DereferencingMemberAccess node) {
    var st = group.getInstanceOf("expression/dereferencingMemberAccess");
    st.add("object", visit(node.object()));
    st.add("member", visit(node.member()));
    return st;
  }

  public ST visit (MemberAccess node) {
    var st = group.getInstanceOf("expression/memberAccess");
    st.add("object", visit(node.object()));
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
    for (var child : node.getChildren())
      st.add("argument", visit(child));
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

  // Do we need separate string templates for each literal type? It does not appear so.

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
