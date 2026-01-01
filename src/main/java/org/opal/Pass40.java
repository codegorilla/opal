package org.opal;

import org.opal.ast.AstNode;
import org.opal.ast.TranslationUnit;
import org.opal.ast.declaration.OtherDeclarations;
import org.opal.ast.declaration.VariableDeclaration;
import org.opal.ast.declaration.VariableInitializer;
import org.opal.ast.declaration.VariableTypeSpecifier;
import org.opal.ast.expression.*;
import org.opal.ast.type.*;
import org.opal.symbol.Scope;
import org.opal.type.ArrayType;
import org.opal.type.PointerType;
import org.opal.type.Type;

import java.util.LinkedList;

// The purpose of this pass is compute expression types and maybe
// perform type checking, coercion, etc.

public class Pass40 extends BaseVisitor {

  private final LinkedList<Type> typeStack = new LinkedList<>();

  private Scope currentScope = null;

  public Pass40 (AstNode input) {
    super(input);
  }

  public void process () {
    visit((TranslationUnit)root);
  }

  public void visit (TranslationUnit node ) {
    // This is package scope
    currentScope = node.getScope();
    node.otherDeclarations().accept(this);
  }

  public void visit (OtherDeclarations node ) {
    for (var otherDeclaration : node.children())
      otherDeclaration.accept(this);
  }

  public void visit (VariableDeclaration node ) {
    if (node.hasInitializer())
      node.getInitializer().accept(this);
  }

  public void visit (VariableInitializer node) {
    node.getExpression().accept(this);
  }

  // Does every expression have a sub-expression?

  public void visit (Expression node) {
    System.out.println("GOT EXPR");
    node.getSubExpression().accept(this);
  }

  public void visit (BinaryExpression node) {
    System.out.println("BIN EXPR");
    node.getLeft().accept(this);
    node.getRight().accept(this);
  }

  public void visit (IntegerLiteral node) {
    var kind = node.getToken().getKind();
    if (kind == Token.Kind.INT32_LITERAL) {
      var symbol = currentScope.resolve("int32", true);
      // Now need to cast symbol to primitive symbol in order to retrieve type
      // See if we can use a visitor for that.
    }

  }


  /*
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
    var t = new org.opal.type.PrimitiveType();
    // Hard-code INT for now
    t.setKind(org.opal.type.PrimitiveType.Kind.INT);
    typeStack.push(t);
  }
*/

}
