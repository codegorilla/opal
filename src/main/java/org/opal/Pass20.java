package org.opal;

import org.opal.ast.AstNode;
import org.opal.ast.TranslationUnit;
import org.opal.ast.declaration.*;
import org.opal.ast.statement.CompoundStatement;
import org.opal.symbol.*;
import org.opal.type.Type;

import java.util.LinkedList;

// DEFINE

// The purpose of this pass is to add variable and routine symbols to the
// symbol table. We don't want to try to resolve symbols in this pass because
// we want to allow objects to be defined out of order.

// We also don't necessarily want to derive type expressions from declarators
// at this point (although it might be possible).

public class Pass20 extends BaseVisitor {

  private final LinkedList<RoutineParameter> paramStack = new LinkedList<>();

  // Built-in and global scopes were already created in previous pass
  private Scope currentScope;

  public Pass20 (AstNode input) {
    super(input);
  }

  public void process () {
    System.out.println("PASS 20");
    visit((TranslationUnit)root);
  }

  public void visit (TranslationUnit node ) {
    currentScope = node.getScope();
    node.getPackageDeclaration().accept(this);
    node.getOtherDeclarations().accept(this);
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

  // Variable symbol should have a link to the AST node and AST node should
  // have a link to the symbol. Why?

  public void visit (VariableName node) {
    var symbol = new VariableSymbol(node.getToken().getLexeme());
    currentScope.define(symbol);
  }

  public void visit (RoutineDeclaration node) {
    node.getName().accept(this);
    node.getParameters().accept(this);
    node.getBody().accept(this);
  }

  public void visit (RoutineName node) {
    var symbol = new RoutineSymbol(node.getToken().getLexeme());
    currentScope.define(symbol);
  }

  public void visit (RoutineParameters node) {
    for (var routineParameter : node.children())
      routineParameter.accept(this);
  }

  // We use a stack to facilitate capturing routine parameters in the top-most
  // block scope of the routine.

  public void visit (RoutineParameter node) {
    paramStack.push(node);
  }

  public void visit (RoutineParameterName node) {
    var symbol = new VariableSymbol(node.getToken().getLexeme());
    currentScope.define(symbol);
  }

  // Normally, in C++ the function body is just a compound statement. However,
  // the top-most compound statement is special in that it includes function
  // parameters even though they appear outside of the block delimiters. This
  // raises questions about how to best implement this "special case".

  // One idea is to have a special kind of compound statement (i.e. a "top"
  // compound statement). Another is to change behavior based on what the
  // parent node is. For now, we will do the latter.

  public void visit (RoutineBody node) {
    node.getCompoundStatement().accept(this);
  }

  public void visit (CompoundStatement node) {
    var scope = new Scope(Scope.Kind.BLOCK);
    scope.setEnclosingScope(currentScope);
    currentScope = scope;
    node.setScope(currentScope);
    // If the parent is instance of routine body then process the parameters
    // To do: Keep a node path stack so that determination can be made
    while (!paramStack.isEmpty()) {
      var param = paramStack.pop();
      param.getName().accept(this);
    }
    // To do: Now we need to go process any declaration statements and any
    // nested compound statements that might contain more declaration
    // statements.
    currentScope = scope.getEnclosingScope();
  }

}
