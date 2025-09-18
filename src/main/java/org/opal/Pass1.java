package org.opal;

import org.opal.ast.*;

import java.util.Iterator;

public class Pass1 extends BaseVisitor {

  public Pass1 (AstNode input) {
    super(input);
  }

  public void process () {
    root.accept(this);
  }

  public void visit (TranslationUnit node) {
    System.out.println("Translation unit");
    var child = node.getChild(0);
    child.accept(this);
  }

  // Declarations

  public void visit (Declarations node) {
    var children = node.getChildren();
    while (children.hasNext()) {
      var child = children.next();
      child.accept(this);
    }
  }

  public void visit (ImportDeclaration node) {
    System.out.println("Import Declaration");
  }

  public void visit (ImportName node) {
    System.out.println("Import Name");
  }

  public void visit (VariableDeclaration node) {
    System.out.println("Variable Declaration");
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

}
