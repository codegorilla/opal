package org.opal;

import org.opal.ast.AstNode;
import org.opal.ast.TranslationUnit;
import org.opal.ast.declaration.*;
import org.opal.ast.type.*;
import org.opal.type.ArrayType;
import org.opal.type.PointerType;
import org.opal.type.Type;

import java.util.LinkedList;

// The purpose of this pass is to annotate declarator/type AST nodes with type
// expressions. This is not necessarily a type-checking pass though - that will
// come later.

public class Pass30 extends BaseVisitor {

  private final LinkedList<Type> typeStack = new LinkedList<>();

  public Pass30 (AstNode input) {
    super(input);
  }

  public void process () {
    visit((TranslationUnit)root);
  }

  public void visit (TranslationUnit node ) {
    node.getOtherDeclarations().accept(this);
  }

  public void visit (OtherDeclarations node ) {
    for (var otherDeclaration : node.children())
      otherDeclaration.accept(this);
  }

  public void visit (VariableDeclaration node ) {
    if (node.hasTypeSpecifier())
      node.getTypeSpecifier().accept(this);
  }

  public void visit (VariableTypeSpecifier node ) {
    node.getDeclarator().accept(this);
    node.setType(typeStack.pop());
  }

  public void visit (Declarator node) {
    node.getDirectDeclarator().accept(this);
    node.getPointerDeclarators().accept(this);
    node.getArrayDeclarators().accept(this);
  }

  public void visit (ArrayDeclarators node) {
    for (var arrayDeclarator : node.children())
      arrayDeclarator.accept(this);
  }

  public void visit (ArrayDeclarator node) {
    var t = new ArrayType();
    t.setElementType(typeStack.pop());
    // Hard code size for now. This will eventually just be a reference to an
    // AST node representing the root of an expression sub-tree.
    t.setSize(12);
    typeStack.push(t);
  }

  public void visit (NominalDeclarator node) {
    var t = new org.opal.type.NominalType();
    t.setString(node.getToken().getLexeme());
    typeStack.push(t);
  }

  public void visit (PointerDeclarators node) {
    for (var pointerDeclarator : node.children())
      pointerDeclarator.accept(this);
  }

  public void visit (PointerDeclarator node) {
    var t = new PointerType();
    t.setPointeeType(typeStack.pop());
    typeStack.push(t);
  }

  public void visit (PrimitiveDeclarator node) {
    var t = new org.opal.type.PrimitiveType("INT", 12);
    // Hard-code INT for now
    t.setText("INT");
    typeStack.push(t);
  }


}
