package org.opal;

import org.opal.ast.AstNode;
import org.opal.ast.TranslationUnit;
import org.opal.ast.declaration.*;
import org.opal.ast.expression.*;
import org.opal.ast.type.*;

import org.stringtemplate.v4.*;

import java.net.URL;
import java.util.LinkedList;

public class Generator extends ResultBaseVisitor <ST> {

  private final URL templateDirectoryUrl;
  private final STGroupDir group;

  // Stack for facilitating out-of-order operations. For example, we need to swap the base type for the variable name in
  // order to form a declarator. We also need to invert the order in which arrays and pointers are processed.
  private final LinkedList<ST> stack = new LinkedList<>();

  // Stack for keeping track of current node path
  private final LinkedList<AstNode> ancestorStack = new LinkedList<>();

  public Generator (AstNode input) {
    super(input);
    templateDirectoryUrl = this.getClass().getClassLoader().getResource("templates");
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
    st.add("importDeclarations", visit(node.importDeclarations()));
    st.add("declarations", visit(node.declarations()));
    System.out.println("---");
    System.out.println(st.render());
    return null;
  }

  // DECLARATIONS **************************************************

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

  // IMPORT DECLARATIONS

  public ST visit (ImportDeclarations node) {
    var st = group.getInstanceOf("declaration/importDeclarations");
    for (var child : node.getChildren())
      st.add("importDeclaration", visit(child));
    return st;
  }

  public ST visit (ImportDeclaration node) {
    var st = group.getInstanceOf("declaration/importDeclaration");
    st.add("importName", visit(node.importName()));
    return st;
  }

  public ST visit (ImportName node) {
    var st = group.getInstanceOf("declaration/importName");
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

  public ST visit (Modifiers node) {
    System.out.println("Modifiers");
    return null;
  }

  // ROUTINE DECLARATIONS

  public ST visit (RoutineDeclaration node) {
    var st = group.getInstanceOf("declaration/functionDeclaration");
    st.add("functionName", visit(node.routineName()));
    st.add("functionParameters", visit(node.routineParameters()));
    st.add("functionReturnType", visit(node.routineReturnType()));
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
    visit(node.routineParameterTypeSpecifier());
    st.add("typeSpecifier", stack.pop());
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
    visit(node.type());
    st.add("type", stack.pop());
    stack.push(st);
    return null;
  }

  public ST visit (RoutineReturnType node) {
    var st = group.getInstanceOf("declaration/functionReturnType");
    visit(node.type());
    st.add("type", stack.pop());
    return st;
  }

  // VARIABLE DECLARATIONS

  // Change variableAccessSpecifier to accessSpecifier for consistency?

  // In C++, any pointer or raw array portions of the type specifier are transferred to the name to form a so-called
  // "declarator".

  // The variable name is placed on a stack, transformed into a declarator, and then retrieved from the stack. Since
  // the stack never has more than one element, it doesn't actually need to be a stack.

  public ST visit (VariableDeclaration node) {
    var st = group.getInstanceOf("declaration/variableDeclaration");
    st.add("variableAccessSpecifier", visit(node.accessSpecifier()));
    stack.push(visit(node.variableName()));
    st.add("typeSpecifier", visit(node.variableTypeSpecifier()));
    st.add("declarator", stack.pop());
//    node.getModifiers().accept(this);
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
    var st = group.getInstanceOf("declaration/variableInitializer");
    st.add("expression", visit(node.expression()));
    return st;
  }

  // EXPRESSIONS

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

  // TYPES

  public ST visit (ArrayType node) {
    var st = group.getInstanceOf("declarator/arrayDeclarator");
    st.add("directDeclarator", stack.pop());
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
    // There should be a loop here, but assume only one child for now
    st.add("argument", visit(node.getChild(0)));
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
