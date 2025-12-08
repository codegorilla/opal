package org.opal;

import org.opal.ast.*;
import org.opal.ast.declaration.*;
import org.opal.ast.type.*;

public class Pass1 extends BaseVisitor {

  private final Counter depth = new Counter();

  public Pass1 (AstNode input) {
    super(input);
  }

  public void process () {
    System.out.println("---");
    visit((TranslationUnit)root);
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

  public void visit (PackageDeclaration node) {
    depth.increment();
    printNode(node);
    visit(node.packageName());
    depth.decrement();
  }

  public void visit (PackageName node) {
    depth.increment();
    printNode(node);
    depth.decrement();
  }

  public void visit (ImportDeclarations node) {
    depth.increment();
    printNode(node);
    for (var importDeclaration : node.children())
      visit(importDeclaration);
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

  public void visit (OtherDeclarations node) {
    depth.increment();
    printNode(node);
    for (var otherDeclaration : node.children())
      otherDeclaration.accept(this);
    depth.decrement();
  }

  public void visit (VariableDeclaration node) {
    depth.increment();
    printNode(node);
    visit(node.getName());
    if (node.hasTypeSpecifier())
      visit(node.getTypeSpecifier());
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
    visit(node.getDeclarator());
    depth.decrement();
  }

  // TYPES

  public void visit (Declarator node) {
    depth.increment();
    printNode(node);
    visit(node.getPointerDeclarators());
    node.getDirectDeclarator().accept(this);
    depth.decrement();
  }

  public void visit (PointerDeclarators node) {
    depth.increment();
    printNode(node);
    for (var pointerDeclarator : node.children())
      visit(pointerDeclarator);
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

  public void printNode (AstNode node) {
    var INDENT_SPACES = 2;
    var spaces = " ".repeat(INDENT_SPACES * depth.get());
    var className = node.getClass().getSimpleName();
    var token = node.getToken();
    System.out.println(spaces + "- " + className + (token != null ? ": " + token : ""));
  }

}
