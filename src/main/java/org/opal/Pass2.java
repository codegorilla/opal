package org.opal;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opal.ast.AstNode;
import org.opal.ast.TranslationUnit;
import org.opal.ast.declaration.*;
import org.opal.ast.expression.BinaryExpression;
import org.opal.ast.expression.Expression;
import org.opal.ast.expression.FloatingPointLiteral;
import org.opal.ast.expression.IntegerLiteral;
import org.opal.ast.type.NominalType;
import org.opal.ast.type.PrimitiveType;

public class Pass2 extends BaseVisitor {

  private final int indentSpaces = 2;

  private static final Logger LOGGER = LogManager.getLogger();

  private final Counter depth = new Counter();

  public Pass2 (AstNode input) {
    super(input);

    var level = Level.TRACE;
//    Configurator.setRootLevel(level);
  }

  public void process () {
    visit(root);
  }

  public void visit (AstNode node) {
//    nodePath.push(node);
    printNode(node);
    var children = node.getChildren();
    depth.increment();
    for (var child : children) {
      if (child != null)
        visit(child);
    }
    depth.decrement();
//    node.accept(this);
//    nodePath.pop();
  }

  public void printNode (AstNode node) {
    var spaces = " ".repeat(indentSpaces * depth.get());
    System.out.println( spaces + "- " + node.getClass().getSimpleName() + ": " + node.getToken());
  }


  public void visit (TranslationUnit node) {
    visit(node.declarations());
  }

  // Declarations

  public void visit (Declarations node) {
    var children = node.getChildren();
    for (var child : children) {
      if (child != null)
        visit(child);
    }
  }

  public void visit (PackageDeclaration node) {
    visit(node.getChild(0));
  }

  public void visit (PackageName node) {
  }

  public void visit (ImportDeclaration node) {
    System.out.println("Import Declaration");
  }

  public void visit (ImportName node) {
    System.out.println("Import Name");
  }

  public void visit (UseDeclarations node) {
    visit(node.getChild(0));
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

  /*
  public void visit (ArrayType node) {
    System.out.println("ArrayType");
  }
  */

  public void visit (NominalType node) {
    System.out.println("NominalType");
  }

  /*
  public void visit (PointerType node) {
    System.out.println("PointerType");
  }
*/

  public void visit (PrimitiveType node) {
    System.out.println("PrimitiveType");
  }

}
