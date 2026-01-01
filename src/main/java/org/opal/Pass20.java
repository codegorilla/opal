package org.opal;

import org.opal.ast.AstNode;
import org.opal.ast.TranslationUnit;
import org.opal.ast.declaration.*;
import org.opal.ast.type.*;
import org.opal.symbol.*;
import org.opal.type.ArrayType;
import org.opal.type.PointerType;
import org.opal.type.Type;

import java.util.LinkedList;

// The purpose of this pass is to add variable and routine symbols to the
// symbol table. We don't want to try to resolve symbols in this pass because
// we want to allow objects to be defined out of order.

public class Pass20 extends BaseVisitor {

  private final LinkedList<Type> typeStack = new LinkedList<>();

  // Built-in and global scopes were already created in previous pass
  private Scope currentScope;

  public Pass20 (AstNode input) {
    super(input);
  }

  public void process () {
    visit((TranslationUnit)root);
  }

  public void visit (TranslationUnit node ) {
    currentScope = node.getScope();
    node.otherDeclarations().accept(this);
  }

  public void visit (PackageDeclaration node) {
    // Create package-level scope
    var scope = new Scope(Scope.Kind.PACKAGE);
    node.setScope(scope);
    scope.setEnclosingScope(currentScope);
    currentScope = scope;
  }

  public void visit (OtherDeclarations node ) {
    for (var otherDeclaration : node.children())
      otherDeclaration.accept(this);
  }

  public void visit (VariableDeclaration node ) {
    node.getName().accept(this);
  }

  public void visit (VariableName node) {
    var symbol = new VariableSymbol(node.getToken().getLexeme());
    currentScope.define(symbol);
  }

  public void visit (RoutineDeclaration node) {
    node.getName().accept(this);
  }

  public void visit (RoutineName node) {
    var symbol = new VariableSymbol(node.getToken().getLexeme());
    currentScope.define(symbol);
  }


}
