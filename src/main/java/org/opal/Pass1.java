package org.opal;

import org.opal.ast.*;
import org.opal.ast.declaration.*;

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
    for (var importDeclaration : node.getChildrenX())
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
    for (var importName : node.getChildrenX())
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

  public void printNode (AstNode node) {
    var INDENT_SPACES = 2;
    var spaces = " ".repeat(INDENT_SPACES * depth.get());
    var className = node.getClass().getSimpleName();
    var token = node.getToken();
    System.out.println(spaces + "- " + className + (token != null ? ": " + token : ""));
  }

}
