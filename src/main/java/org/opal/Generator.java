package org.opal;

import org.opal.ast.AstNode;
import org.opal.ast.TranslationUnit;
import org.opal.ast.declaration.*;

import org.opal.ast.expression.*;
import org.opal.ast.type.PrimitiveType;
import org.opal.ast.type.Type;
import org.stringtemplate.v4.*;

import java.net.URL;

public class Generator extends ResultBaseVisitor <ST> {

  private final URL templateDirectoryUrl;
  private final STGroupDir group;

  public Generator (AstNode input) {
    super(input);
    templateDirectoryUrl = this.getClass().getClassLoader().getResource("templates");
    group = new STGroupDir(templateDirectoryUrl);
  }

  public void process () {
    root.accept(this);
  }

  public ST visit (AstNode node) {
    return node.accept(this);
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

  // DECLARATIONS

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

  public ST visit (Declarations node) {
    var st = group.getInstanceOf("declaration/declarations");
    for (var child : node.getChildren())
      st.add("declaration", visit(child));
    return st;
  }

  // VARIABLE DECLARATIONS

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

  // Change variableAccessSpecifier to accessSpecifier for consistency?

  public ST visit (VariableDeclaration node) {
    var st = group.getInstanceOf("declaration/variableDeclaration");
    st.add("variableAccessSpecifier", visit(node.accessSpecifier()));
    st.add("declarator", visit(node.variableName()));
//    node.getModifiers().accept(this);
    st.add("typeSpecifier", visit(node.variableTypeSpecifier()));
    st.add("initializer", visit(node.variableInitializer()));
    return st;
  }

  public ST visit (VariableName node) {
    var st = group.getInstanceOf("declaration/variableName");
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

  /*
  public void visit (ArrayType node) {
    System.out.println("ArrayType");
  }

  public void visit (NominalType node) {
    System.out.println("NominalType");
  }

  public void visit (PointerType node) {
    System.out.println("PointerType");
  }
  */

  public ST visit (PrimitiveType node) {
    var st = group.getInstanceOf("type/primitiveType");
    st.add("name", node.getToken().getLexeme());
    return st;
  }

}
