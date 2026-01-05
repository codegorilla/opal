package org.opal;

import org.opal.ast.*;
import org.opal.ast.declaration.*;
import org.opal.ast.type.*;
import org.opal.symbol.*;
import org.opal.type.*;

import java.util.LinkedList;

// The purpose of this pass is to construct type expressions from declarators.
// However, this pass does not perform any type checking.

public class Pass30 extends BaseVisitor {

  private Scope currentScope;

  private final LinkedList<Type> typeQueue = new LinkedList<>();

  public Pass30 (AstNode input) {
    super(input);
  }

  public void process () {
    System.out.println("PASS 30");
    visit((TranslationUnit)root);
  }

  public void visit (TranslationUnit node ) {
    node.getPackageDeclaration().accept(this);
    node.getOtherDeclarations().accept(this);
  }

  public void visit (PackageDeclaration node) {
    currentScope = node.getScope();
  }

  public void visit (OtherDeclarations node ) {
    for (var otherDeclaration : node.children())
      otherDeclaration.accept(this);
  }

  public void visit (VariableDeclaration node ) {
    if (node.hasTypeSpecifier()) {
      node.getTypeSpecifier().accept(this);
      node.getName().accept(this);
    }
  }

  public void visit (VariableName node) {
    var symbol = currentScope.resolve(node.getToken().getLexeme(), true);
    ((VariableSymbol)symbol).setType(typeQueue.remove());
  }

  public void visit (VariableTypeSpecifier node ) {
    node.getDeclarator().accept(this);
  }

  public void visit (Declarator node) {
    node.getPointerDeclarators().accept(this);
    node.getArrayDeclarators().accept(this);
    node.getDirectDeclarator().accept(this);
  }

  public void visit (ArrayDeclarators node) {
    for (var arrayDeclarator : node.children())
      arrayDeclarator.accept(this);
  }

  public void visit (ArrayDeclarator node) {
    var type = new ArrayType();
    // Hard code size for now. This will eventually just be a reference to an
    // AST node representing the root of an expression sub-tree.
//    type.setSize(12);
    typeQueue.add(type);
  }

//  public void visit (NominalDeclarator node) {
//    System.out.println("NOMINAL_DECL");
//    var t = new NominalType();
//    t.setString(node.getToken().getLexeme());
//    typeStack.push(t);
//  }

  public void visit (PointerDeclarators node) {
    for (var pointerDeclarator : node.children())
      pointerDeclarator.accept(this);
  }

  public void visit (PointerDeclarator node) {
    var type = new PointerType();
    typeQueue.add(type);
  }

  public void visit (PrimitiveDeclarator node) {
    var symbol = currentScope.resolve(node.getToken().getLexeme(), true);
    // The symbol is guaranteed a type symbol because the resolve call is being
    // made from a method that could only be arrived at from a declarator.
    var current = ((TypeSymbol)symbol).getType();
    while (!typeQueue.isEmpty()) {
      var next = typeQueue.remove();
      if (next.getKind() == Type.Kind.ARRAY) {
        ((ArrayType)next).setElementType(current);
        current = next;
      } else if (next.getKind() == Type.Kind.POINTER) {
        ((PointerType)next).setPointeeType(current);
        current = next;
      }
    }
    typeQueue.add(current);
  }

}
