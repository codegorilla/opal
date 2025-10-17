package org.opal;

import org.opal.ast.AstNode;
import org.opal.ast.TranslationUnit;
import org.opal.ast.declaration.*;
import org.opal.ast.expression.*;
import org.opal.ast.type.*;

import org.stringtemplate.v4.*;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;

// The purpose of this pass is to create a module interface unit.

// To do: Public/private dichotomy needs to be handled correctly.

public class Generator2 extends ResultBaseVisitor <ST> {

  private final URL templateDirectoryUrl;
  private final STGroupDir group;

  // Stack for facilitating out-of-order operations. For example, we need to swap the base type for the variable name in
  // order to form a declarator. We also need to invert the order in which arrays and pointers are processed.
  private final LinkedList<ST> stack = new LinkedList<>();

  // Stack for keeping track of current node path
  private final LinkedList<AstNode> ancestorStack = new LinkedList<>();

  // Tracks passes on individual nodes
  private HashMap<AstNode, Integer> passCount = new HashMap<>();

  public Generator2 (AstNode input) {
    super(input);
    templateDirectoryUrl = this.getClass().getClassLoader().getResource("templates/interface");
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

  // To do: Might need a modifier table to map Opal modifiers to C++ modifiers

  // How do we know what kind of modifiers we have? Do we check the parent node
  // kind using 'instanceof'? Or should the modifiers be coded in the parser?

  // Need to set a pass counter

  private int modifiersPass = 0;

  public ST visit (Modifiers node) {
      var st = group.getInstanceOf("declaration/modifiers");
      for (var modifier : node.getModifiers())
        st.add("modifier", visit(modifier));
      return st;
  }
  
  public ST visit (Modifier node) {
    if (ancestorStack.get(1) instanceof ClassDeclaration)
      return classModifier(node);
    else if (ancestorStack.get(1) instanceof RoutineDeclaration)
      return routineModifier(node);
    else if (ancestorStack.get(1) instanceof VariableDeclaration)
      return variableModifier(node);
    return null;
  }

  public ST classModifier (Modifier node) {
    return new ST(node.getToken().getLexeme());
  }

  public ST routineModifier (Modifier node) {
    return new ST(node.getToken().getLexeme());
  }

  public ST variableModifier (Modifier node) {
    return new ST(node.getToken().getLexeme());
  }

  // CLASS DECLARATIONS

  public ST visit (ClassDeclaration node) {
    if (!node.hasExportSpecifier()) {
      var st = group.getInstanceOf("declaration/classDeclaration");
      if (node.modifiers().hasChildren()) {
        modifiersPass = 1;
        st.add("modifiers", visit(node.modifiers()));
      }
      if (node.modifiers().hasChildren()) {
        modifiersPass = 2;
        st.add("modifiers", visit(node.modifiers()));
      }
      st.add("name", visit(node.name()));
      if (node.hasExtendsClause())
        st.add("extendsClause", visit(node.extendsClause()));
      st.add("body", visit(node.body()));
      return st;
    } else {
      return null;
    }
  }

  public ST visit (ClassName node) {
    return new ST(node.getToken().getLexeme());
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
    for (var child : node.getChildren()) {
      st.add("memberDeclaration", visit(child));
    }
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
    stack.push(visit(node.name()));
    if (node.hasTypeSpecifier())
      st.add("typeSpecifier", visit(node.typeSpecifier()));
    st.add("declarator", stack.pop());
    if (node.hasInitializer())
      st.add("initializer", visit(node.initializer()));
    return st;
  }

  public ST visit (MemberRoutineDeclaration node) {
    var st = group.getInstanceOf("declaration/memberFunctionDeclaration");
    if (node.hasAccessSpecifier())
      st.add("accessSpecifier", visit(node.accessSpecifier()));
    else
      st.add("accessSpecifier", "public");
    st.add("name", visit(node.name()));
    st.add("parameters", visit(node.parameters()));
    if (node.cvQualifiers().hasChildren())
      st.add("cvQualifiers", visit(node.cvQualifiers()));
    st.add("returnType", visit(node.returnType()));
    return st;
  }

  public ST visit (CVQualifiers node) {
    var st = group.getInstanceOf("declaration/cvQualifiers");
    for (var qualifier : node.getQualifiers())
      st.add("qualifier", visit(qualifier));
    return st;
  }

  public ST visit (CVQualifier node) {
    return new ST(node.getToken().getLexeme());
  }

  // ROUTINE DECLARATIONS

  public ST visit (RoutineDeclaration node) {
    if (!node.hasExportSpecifier()) {
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

  private ST emptyDeclarator () {
    return new ST("");
  }

  // VARIABLE DECLARATIONS

  public ST visit (VariableDeclaration node) {
    if (!node.hasExportSpecifier()) {
      var st = group.getInstanceOf("declaration/variableDeclaration");
      if (node.modifiers().hasChildren())
        st.add("modifiers", visit(node.modifiers()));
      stack.push(visit(node.name()));
      if (node.hasTypeSpecifier())
        st.add("typeSpecifier", visit(node.typeSpecifier()));
      st.add("declarator", stack.pop());
      if (node.hasInitializer())
        st.add("initializer", visit(node.initializer()));
      return st;
    }
    else
      return null;
  }

  // In C++, any pointer or raw array portions of the type specifier are
  // transferred to the name to form a so-called "declarator".

  // The variable name is placed on a stack, transformed into a declarator, and
  // then retrieved from the stack.

  // The variable name becomes a "simple declarator", which is the core of the overall C++ declarator that gets built
  // up. A C++ declaration is of the form "typeSpecifier declarator", which is essentially the reverse of the cobalt
  // declaration of the form "var variableName: typeSpecifier" (setting aside the fact that the term "type specifier"
  // has a different interpretation between the two). Due to the need to swap the variable name with the leaf base type
  // from the type specifier, and that the leaf base type may be several levels down in the type expression tree, we
  // will use an explicit stack to facilitate the exchange.

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

  // EXPRESSIONS **************************************************

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

  // TYPES **************************************************

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
