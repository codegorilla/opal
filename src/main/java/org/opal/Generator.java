package org.opal;

import org.opal.ast.AstNode;
import org.opal.ast.TranslationUnit;
import org.opal.ast.declaration.*;
import org.opal.ast.expression.BinaryExpression;
import org.opal.ast.expression.Expression;
import org.opal.ast.expression.FloatingPointLiteral;
import org.opal.ast.expression.IntegerLiteral;
import org.opal.ast.type.*;

import org.stringtemplate.v4.*;

import java.net.URL;
import java.util.HashMap;

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

  public ST visit (VariableDeclaration node) {
    var st = group.getInstanceOf("declaration/variableDeclaration");
    System.out.println(node.getToken());
    st.add("variableAccessSpecifier", visit(node.accessSpecifier()));
    st.add("declarator", visit(node.variableName()));
    return st;

//    node.getModifiers().accept(this);
//    node.getTypeSpecifier().accept(this);
//    node.getInitializer().accept(this);
  }

  public ST visit (VariableName node) {
    var st = group.getInstanceOf("declaration/variableName");
    st.add("name", node.getToken().getLexeme());
    return st;
  }

  /*

  public void visit (VariableTypeSpecifier node) {
    System.out.println("Variable Type Specifier");
  }

  public void visit (VariableInitializer node) {
    System.out.println("Variable Initializer");
    node.getChild(0).accept(this);
  }

  // Expressions

  public void visit (Expression node) {
    System.out.println("Expression");
    node.getChild(0).accept(this);
  }

  public void visit (BinaryExpression node) {
    System.out.println("Binary Expression");
    node.getLeft().accept(this);
    node.getRight().accept(this);
  }

  public void visit (FloatingPointLiteral node) {
    System.out.println("Floating Point literal");
  }

  public void visit (IntegerLiteral node) {
    System.out.println("Integer literal");
  }


  // Types

  public void visit (TypeRoot node) {
    System.out.println("TypeRoot");
  }

  public void visit (ArrayType node) {
    System.out.println("ArrayType");
  }

  public void visit (NominalType node) {
    System.out.println("NominalType");
  }

  public void visit (PointerType node) {
    System.out.println("PointerType");
  }

  public void visit (PrimitiveType node) {
    System.out.println("PrimitiveType");
  }

  */
}
