package org.opal;

import org.opal.ast.AstNode;
import org.opal.ast.TranslationUnit;
import org.opal.ast.declaration.*;
import org.opal.ast.type.*;
import org.opal.symbol.PrimitiveTypeSymbol;
import org.opal.symbol.Scope;
import org.opal.type.*;

import java.util.LinkedList;

// The purpose of this pass is to annotate declarator/type AST nodes with type
// expressions. This is not necessarily a type-checking pass though - that will
// come later.

public class Pass30 extends BaseVisitor {

  private Scope currentScope;

  private final LinkedList<Type> typeStack = new LinkedList<>();
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
    if (node.hasTypeSpecifier())
      node.getTypeSpecifier().accept(this);
  }

  public void visit (VariableTypeSpecifier node ) {
    node.getDeclarator().accept(this);
//    for (var item : typeStack) {
//      System.out.println(item);
//    }
//    typeStack.pop();
  }

  public void visit (Declarator node) {
    System.out.println("DECL");
    node.getDirectDeclarator().accept(this);
    var baseType = typeStack.pop();
    node.getArrayDeclarators().accept(this);
    node.getPointerDeclarators().accept(this);
    typeStack.push(baseType);
  }

  public void visit (ArrayDeclarators node) {
    for (var arrayDeclarator : node.children())
      arrayDeclarator.accept(this);
  }

  public void visit (ArrayDeclarator node) {
    System.out.println("ARRAY_DECL");
    var t = new ArrayType();
    // Hard code size for now. This will eventually just be a reference to an
    // AST node representing the root of an expression sub-tree.
    t.setSize(12);
    typeStack.push(t);
  }

  public void visit (NominalDeclarator node) {
    System.out.println("NOMINAL_DECL");
    var t = new NominalType();
    t.setString(node.getToken().getLexeme());
    typeStack.push(t);
  }

  public void visit (PointerDeclarators node) {
    for (var pointerDeclarator : node.children())
      pointerDeclarator.accept(this);
  }

  public void visit (PointerDeclarator node) {
    System.out.println("POINTER_DECL");
    var t = new PointerType();
    typeStack.push(t);
  }

  public void visit (PrimitiveDeclarator node) {
    System.out.println("PRIM_DECL");
    var symbol = currentScope.resolve(node.getToken().getLexeme(), true);
    // The symbol is guaranteed a primitive type symbol because the resolve
    // call is being made from a method that could only be arrived at if the
    // declarator is a primitive declarator.
    var type = ((PrimitiveTypeSymbol)symbol).getType();
    typeStack.push(type);
  }
  
}
