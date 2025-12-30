package org.opal;

import org.opal.ast.*;
import org.opal.ast.declaration.*;
import org.opal.ast.expression.*;
import org.opal.ast.type.*;

// We cannot use a generic "visit" method that takes an AST node because it
// won't be able to tell what kind of node it is and will treat them as plain
// AST nodes. That was fine when using regular nodes (with generic lists of
// children) but it doesn't work when using irregular nodes where the specific
// kind of node is important for method resolution.

// If a specific type isn't being used properly, make sure it is declared in
// the various visitor interfaces and base classes!

public class Pass1 extends BaseVisitor {

  private final Counter depth = new Counter();

  public Pass1 (AstNode input) {
    super(input);
  }

  public void process () {
    System.out.println("---");
    visit((TranslationUnit)root);
  }

  public void printNode (AstNode node) {
    var INDENT_SPACES = 2;
    var spaces = " ".repeat(INDENT_SPACES * depth.get());
    var className = node.getClass().getSimpleName();
    var token = node.getToken();
    var error = (token != null && token.getError());
    var e = (error ? "(error) " : "");
    System.out.println(spaces + "- " + e + className + (token != null ? ": " + token : ""));
  }

  public void visit (TranslationUnit node ) {
    printNode(node);
    visit(node.packageDeclaration());
    if (node.hasImportDeclarations())
      visit(node.importDeclarations());
    if (node.hasUseDeclarations())
      visit(node.useDeclarations());
    if (node.hasOtherDeclarations())
      visit(node.otherDeclarations());
  }

  // DECLARATIONS

  public void visit (PackageDeclaration node) {
    depth.increment();
    printNode(node);
    node.getPackageName().accept(this);
    depth.decrement();
  }

  public void visit (PackageName node) {
    depth.increment();
    printNode(node);
    depth.decrement();
  }

  public void visit (OtherDeclarations node) {
    depth.increment();
    printNode(node);
    for (var otherDeclaration : node.children())
      otherDeclaration.accept(this);
    depth.decrement();
  }

  public void visit (BogusDeclaration node) {
    depth.increment();
    printNode(node);
    depth.decrement();
  }

  public void visit (ImportDeclarations node) {
    depth.increment();
    printNode(node);
    for (var importDeclaration : node.children())
      importDeclaration.accept(this);
    depth.decrement();
  }

  public void visit (ImportDeclaration node) {
    depth.increment();
    printNode(node);
    visit(node.qualifiedName());
    if (node.hasAsName())
      visit(node.asName());
    depth.decrement();
  }

  public void visit (ImportQualifiedName node) {
    depth.increment();
    printNode(node);
    for (var importName : node.children())
      visit(importName);
    depth.decrement();
  }

  public void visit (ImportName node) {
    depth.increment();
    printNode(node);
    depth.decrement();
  }

  public void visit (ImportAsName node) {
    depth.increment();
    printNode(node);
    depth.decrement();
  }

  public void visit (UseDeclarations node) {
    depth.increment();
    printNode(node);
    for (var useDeclaration : node.children())
      visit(useDeclaration);
    depth.decrement();
  }

  public void visit (UseDeclaration node) {
    depth.increment();
    printNode(node);
    visit(node.qualifiedName());
    depth.decrement();
  }

  public void visit (UseQualifiedName node) {
    depth.increment();
    printNode(node);
    visit(node.useName());
    depth.decrement();
  }

  public void visit (UseName node) {
    depth.increment();
    printNode(node);
    if (node.hasChild())
      node.child().accept(this);
    depth.decrement();
  }

  public void visit (UseNameGroup node) {
    depth.increment();
    printNode(node);
    for (var child : node.children())
      visit(child);
    depth.decrement();
  }

  public void visit (UseNameWildcard node) {
    depth.increment();
    printNode(node);
    depth.decrement();
  }


  public void visit (VariableDeclaration node) {
    depth.increment();
    printNode(node);
    node.getModifiers().accept(this);
    node.getName().accept(this);
    if (node.hasTypeSpecifier())
      visit(node.getTypeSpecifier());
    if (node.hasInitializer())
      visit(node.getInitializer());
    depth.decrement();
  }

  public void visit (VariableModifiers node) {
    depth.increment();
    printNode(node);
    for (var variableModifier : node.children())
      variableModifier.accept(this);
    depth.decrement();
  }

  public void visit (Modifier node) {
    depth.increment();
    printNode(node);
    depth.decrement();
  }

  public void visit (VariableName node) {
    depth.increment();
    printNode(node);
    depth.decrement();
  }

  public void visit (VariableTypeSpecifier node) {
    depth.increment();
    printNode(node);
    node.getDeclarator().accept(this);
    depth.decrement();
  }

  public void visit (VariableInitializer node) {
    depth.increment();
    printNode(node);
    node.getExpression().accept(this);
    depth.decrement();
  }

  // STATEMENTS - TBD

  // EXPRESSIONS

  public void visit (Expression node) {
    depth.increment();
    printNode(node);
    if (node.hasSubExpression())
      node.getSubExpression().accept(this);
    depth.decrement();
  }

  public void visit (UnaryExpression node) {
    depth.increment();
    printNode(node);
    if (node.hasSubExpression())
      node.getSubExpression().accept(this);
    depth.decrement();
  }

  public void visit (FloatingPointLiteral node) {
    depth.increment();
    printNode(node);
    depth.decrement();
  }

  public void visit (IntegerLiteral node) {
    depth.increment();
    printNode(node);
    depth.decrement();
  }

  public void visit (BogusExpression node) {
    depth.increment();
    printNode(node);
    depth.decrement();
  }

  // TYPES

  public void visit (Declarator node) {
    depth.increment();
    printNode(node);
    if (node.hasPointerDeclarators()) {
      visit(node.getPointerDeclarators());
    }
    node.getDirectDeclarator().accept(this);
    if (node.hasArrayDeclarators())
      visit(node.getArrayDeclarators());
    depth.decrement();
  }

  public void visit (ArrayDeclarators node) {
    depth.increment();
    printNode(node);
    for (var arrayDeclarator : node.children())
      arrayDeclarator.accept(this);
    depth.decrement();
  }

  public void visit (ArrayDeclarator node) {
    depth.increment();
    printNode(node);
    if (node.hasExpression()) {
      node.getExpression().accept(this);
    }
    depth.decrement();
  }

  public void visit (PointerDeclarators node) {
    depth.increment();
    printNode(node);
    for (var pointerDeclarator : node.children())
      pointerDeclarator.accept(this);
    depth.decrement();
  }

  public void visit (PointerDeclarator node) {
    depth.increment();
    printNode(node);
    depth.decrement();
  }

  public void visit (PrimitiveType node) {
    depth.increment();
    printNode(node);
    depth.decrement();
  }

  public void visit (BogusDeclarator node) {
    depth.increment();
    printNode(node);
    depth.decrement();
  }

}
