package org.opal;

import org.opal.ast.AstNode;
import org.opal.ast.TranslationUnit;
import org.opal.ast.declaration.*;
import org.opal.ast.expression.*;
import org.opal.ast.type.*;
import org.opal.type.ArrayType;
import org.opal.type.PointerType;
import org.opal.type.Type;

import java.util.LinkedList;

// The purpose of this pass is to annotate declarator/type AST nodes with type
// expressions.

public class Pass2 extends BaseVisitor {

  private LinkedList<Type> typeStack = new LinkedList<>();

  public Pass2 (AstNode input) {
    super(input);
  }

  public void process () {
    visit((TranslationUnit)root);
  }

  public void visit (TranslationUnit node ) {
    if (node.hasOtherDeclarations())
      node.otherDeclarations().accept(this);
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
  }

  public void visit (Declarator node) {
    node.getDirectDeclarator().accept(this);
    node.getPointerDeclarators().accept(this);
    node.getArrayDeclarators().accept(this);
//    var t = typeStack.pop();
  }

  public void visit (ArrayDeclarators node) {
    for (var arrayDeclarator : node.children())
      arrayDeclarator.accept(this);
  }

  public void visit (ArrayDeclarator node) {
    var t = new ArrayType();
    t.setElementType(typeStack.pop());
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

  public void visit (PrimitiveType node) {
    var t = new org.opal.type.PrimitiveType();
    // Hard-code INT for now
    t.setKind(org.opal.type.PrimitiveType.Kind.INT);
    typeStack.push(t);
  }


}
