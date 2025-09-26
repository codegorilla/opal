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
    st.add("packageDeclaration", visit(node.getPackageDeclaration()));
    st.add("declarations", visit(node.getDeclarations()));
    System.out.println(st.render());
    return null;
  }

  public ST visit (PackageDeclaration node) {
    var st = group.getInstanceOf("declaration/packageDeclaration");
    st.add("packageName", visit(node.getPackageName()));
    return st;
  }

  // For now just support single word package names

  public ST visit (PackageName node) {
    var st = group.getInstanceOf("declaration/packageName");
    st.add("name", node.getToken().getLexeme());
    return st;
  }

  // Declarations

  // Use list of templates, or a template containing templates?

  public ST visit (Declarations node) {
    var st = group.getInstanceOf("declaration/declarations");
    var children = node.getChildren();
    for (var child : children)
      st.add("declaration", visit(child));
    return st;
  }

  /*

  public void visit (ImportDeclaration node) {
    System.out.println("Import Declaration");
  }

  public void visit (ImportName node) {
    System.out.println("Import Name");
  }

  public void visit (AccessSpecifier node) {
    System.out.println("Access Specifier");
  }

  public void visit (Modifiers node) {
    System.out.println("Modifiers");
  }

  public void visit (VariableDeclaration node) {
    System.out.println("Variable Declaration");
    node.getAccessSpecifier().accept(this);
    node.getModifiers().accept(this);
    node.getName().accept(this);
    node.getTypeSpecifier().accept(this);
    node.getInitializer().accept(this);
  }

  public void visit (VariableName node) {
    System.out.println("Variable Name");
  }

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
