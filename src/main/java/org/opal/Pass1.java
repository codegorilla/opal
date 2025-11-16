package org.opal;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.opal.ast.*;
import org.opal.ast.declaration.*;
import org.opal.ast.expression.BinaryExpression;
import org.opal.ast.expression.Expression;
import org.opal.ast.expression.FloatingPointLiteral;
import org.opal.ast.expression.IntegerLiteral;
import org.opal.ast.type.*;

public class Pass1 extends BaseVisitor {

  private static final Logger LOGGER = LogManager.getLogger();

  private final Counter depth = new Counter();

  public Pass1 (AstNode input) {
    super(input);

    var level = Level.TRACE;
//    Configurator.setRootLevel(level);
  }

  public void process () {
    root.accept(this);
  }

  public void visit (TranslationUnit node) {
    LOGGER.trace("TranslationUnit");
    System.out.print(" ".repeat(2*depth.get()));
    System.out.println(node.getClass().getSimpleName() + " " + node.getToken());
    var child = node.declarations();
    child.accept(this);
  }

  // Declarations

  public void visit (Declarations node) {
    LOGGER.trace("Declarations");
    depth.increment();
    System.out.print(" ".repeat(2*depth.get()));
    System.out.println(node.getClass().getSimpleName() + " " + node.getToken());
    var children = node.getChildren();
    for (var child : children) {
      child.accept(this);
    }
    depth.decrement();
  }

  public void visit (PackageDeclaration node) {
    LOGGER.trace("PackageDeclaration");
    depth.increment();
    System.out.print(" ".repeat(2*depth.get()));
    System.out.println(node.getClass().getSimpleName() + " " + node.getToken());
    node.getChild(0).accept(this);
    depth.decrement();
  }

  public void visit (PackageName node) {
    LOGGER.trace("PackageName");
    depth.increment();
    System.out.print(" ".repeat(2*depth.get()));
    System.out.println(node.getClass().getSimpleName() + " " + node.getToken());
    depth.decrement();
  }

  public void visit (ImportDeclaration node) {
    System.out.println("Import Declaration");
  }

  public void visit (ImportName node) {
    System.out.println("Import Name");
  }

  public void visit (OtherDeclarations node) {
    System.out.println("Other Declarations");
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
