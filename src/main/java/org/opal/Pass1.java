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
    visit((TranslationUnit) root);
    //printNode((TranslationUnit)root);
  }


  public void visit (AstNode node) {
    node.accept(this);
  }

  public void visit (TranslationUnit node ) {
    printNode(node);
    visit(node.getPackageDeclaration());
    if (node.hasImportDeclarations())
      visit(node.getImportDeclarations());
  }

  public void visit (PackageDeclaration node) {
    depth.increment();
    printNode(node);
    visit(node.getPackageName());
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
    depth.decrement();
  }

  /*
  public void printNode (TranslationUnit node) {
    System.out.println("GOT TU!");
    var n = node.getChild(0);
    n.accept(this);
  }
   */

//  public void printNode (PackageDeclaration node) {
//    System.out.println("GOT PACKAGE DECL");
//  }
//
//  public void printNode (ImportDeclaration node) {
//    System.out.println("GOT HERE!");
//  }

  public void printNode (AstNode node) {
    var INDENT_SPACES = 2;
    var spaces = " ".repeat(INDENT_SPACES * depth.get());
    var className = node.getClass().getSimpleName();
    var token = node.getToken();
    System.out.println(spaces + "- " + className + (token != null ? ": " + token : ""));
  }

}
