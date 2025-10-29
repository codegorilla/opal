package org.opal;

import org.opal.ast.AstNode;
import org.opal.ast.TranslationUnit;
import org.opal.ast.declaration.*;
import org.opal.ast.expression.BinaryExpression;
import org.opal.ast.expression.Expression;
import org.opal.ast.expression.FloatingPointLiteral;
import org.opal.ast.expression.IntegerLiteral;
import org.opal.ast.type.ArrayType;
import org.opal.ast.type.NominalType;
import org.opal.ast.type.PointerType;
import org.opal.ast.type.PrimitiveType;

// The purpose of this pass is to determine import alias names.

import java.util.LinkedList;

public class Pass10 extends BaseVisitor {

  // Stack for keeping track of current node path
  private final LinkedList<AstNode> nodePath = new LinkedList<>();

  public Pass10 (AstNode input) {
    super(input);
  }

  public void process () {
    visit(root);
  }

  public void visit (AstNode node) {
    nodePath.push(node);
    node.accept(this);
    nodePath.pop();
  }


  public void visit (TranslationUnit node) {
    System.out.println("Translation unit");
    visit(node.importDeclarations());
    var child = node.declarations();
    visit(child);
  }

  // Declarations

  public void visit (Declarations node) {
    for (var declaration : node.declarations()) {
      System.out.println("Going to visit!");
      System.out.println(declaration);
      visit(declaration);
    }
  }

  public void visit (PackageDeclaration node) {
    System.out.println("Package Declaration");
  }

  public void visit (PackageName node) {
    System.out.println("Package Name");
  }

  public void visit (ImportDeclarations node) {
    System.out.println("Import Declarations");
    for (var importDeclaration : node.importDeclarations()) {
      visit(importDeclaration);
    }
  }

  public void visit (ImportDeclaration node) {
    System.out.println("Import Declaration");
  }

  public void visit (ImportName node) {
    System.out.println("Import Name");
  }

  public void visit (MemberAccessSpecifier node) {
    System.out.println("Access Specifier");
  }

//  public void visit (Modifiers node) {
//    System.out.println("Modifiers");
//  }

//  public void visit (VariableDeclaration node) {
//    System.out.println("Variable Declaration");
//    node.getAccessSpecifier().accept(this);
//    node.getModifiers().accept(this);
//    node.getName().accept(this);
//    node.getTypeSpecifier().accept(this);
//    node.variableInitializer().accept(this);
//  }

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
    node.leftExpression().accept(this);
    node.rightExpression().accept(this);
  }

  public void visit (FloatingPointLiteral node) {
    System.out.println("Floating Point literal");
  }

  public void visit (IntegerLiteral node) {
    System.out.println("Integer literal");
  }


  // Types

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
