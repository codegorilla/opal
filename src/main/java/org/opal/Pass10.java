package org.opal;

import org.opal.ast.AstNode;
import org.opal.ast.TranslationUnit;
import org.opal.ast.declaration.*;
import org.opal.ast.expression.BinaryExpression;
import org.opal.ast.expression.Expression;
import org.opal.ast.expression.FloatingPointLiteral;
import org.opal.ast.expression.IntegerLiteral;
import org.opal.ast.type.ArrayType;
import org.opal.ast.type.NominalType;
import org.opal.ast.type.PointerType;
import org.opal.ast.type.PrimitiveType;

import org.opal.state.ImportAliasContext;

// The purpose of this pass is to determine import alias names.

// By default the import alias name is the last component of the fully
// qualified name. However, if an explicit alias name is specified with an "as
// clause", then that is taken to be the import alias name instead. If two
// packages are imported whose fully qualified names share the same last
// component, then no import alias name will be created for either package.
// If two packages are imported and the same explicit alias name is specified
// for both, then this is an error. Likewise, if a package is imported and its
// explicit alias name conflicts with an implicit alias name from another
// package, then this is also an error. Finally, if a packages is imported
// whose explicit alias name is specified and is the same as the implicit alias
// name, then this is also an error.

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Pass10 extends BaseVisitor {

  private final List<String> sourceLines;

  // Stack for keeping track of current node path
  private final LinkedList<AstNode> nodePath = new LinkedList<>();

  // Stack for passing name information up and down during traversal
  private final LinkedList<String> nameStack = new LinkedList<>();

  // Do we need two-way mapping?

  // Map that relates import declaration nodes to import alias names
  //  private final HashMap<ImportDeclaration, String> aliasNames = new HashMap<>();

  // Map that relates alias name to import alias state machine
  private final HashMap<String, ImportAliasContext> aliasMachineTable = new HashMap<>();

  public Pass10 (AstNode input, List<String> sourceLines) {
    super(input);
    this.sourceLines = sourceLines;
  }

  public void process () {
    visit(root);
  }

  public void visit (AstNode node) {
    nodePath.push(node);
    node.accept(this);
    nodePath.pop();
  }


  public void visit (TranslationUnit node) {
    System.out.println("Translation unit");
    if (node.hasImportDeclarations())
      visit(node.importDeclarations());
  }

  // Declarations

  public void visit (Declarations node) {
    for (var declaration : node.declarations()) {
      visit(declaration);
    }
  }

  public void visit (PackageDeclaration node) {
    System.out.println("Package Declaration");
  }

  public void visit (PackageName node) {
    System.out.println("Package Name");
  }

  public void visit (ImportDeclarations node) {
    System.out.println("Import Declarations");
    for (var importDeclaration : node.importDeclarations()) {
      visit(importDeclaration);
    }
  }

  public void visit (ImportDeclaration node) {
    if (node.hasAliasName()) {
      // Explicit transition
      visit(node.aliasName());
      var aliasName = nameStack.pop();
      var machine = aliasMachineTable.get(aliasName);
      if (machine == null) {
        var newMachine = new ImportAliasContext();
        newMachine.transitionExplicit(node);
        aliasMachineTable.put(aliasName, newMachine);
      } else {
        machine.transitionExplicit(node);
      }
    } else {
      // Implicit transition
      visit(node.qualifiedName());
      var aliasName = nameStack.pop();
      var machine = aliasMachineTable.get(aliasName);
      if (machine == null) {
        var newMachine = new ImportAliasContext();
        newMachine.transitionImplicit(node);
        aliasMachineTable.put(aliasName, newMachine);
      } else {
        machine.transitionImplicit(node);
      }
    }

  }

  public void visit (ImportQualifiedName node) {
    visit(node.getLastChild());
  }

  public void visit (ImportName node) {
    nameStack.push(node.getToken().getLexeme());
  }

  public void visit (ImportAliasName node) {
    nameStack.push(node.getToken().getLexeme());
  }




//  public void visit (Modifiers node) {
//    System.out.println("Modifiers");
//  }

//  public void visit (VariableDeclaration node) {
//    System.out.println("Variable Declaration");
//    node.getAccessSpecifier().accept(this);
//    node.getModifiers().accept(this);
//    node.getName().accept(this);
//    node.getTypeSpecifier().accept(this);
//    node.variableInitializer().accept(this);
//  }

  public void visit (VariableName node) {
    System.out.println("Variable Name");
  }

  public void visit (VariableTypeSpecifier node) {
    System.out.println("Variable Type Specifier");
  }

  public void visit (VariableInitializer node) {
    System.out.println("Variable Initializer");
    node.getChild(0).accept(this);
  }

  // Expressions

  public void visit (Expression node) {
    System.out.println("Expression");
    node.getChild(0).accept(this);
  }

  public void visit (BinaryExpression node) {
    System.out.println("Binary Expression");
    node.leftExpression().accept(this);
    node.rightExpression().accept(this);
  }

  public void visit (FloatingPointLiteral node) {
    System.out.println("Floating Point literal");
  }

  public void visit (IntegerLiteral node) {
    System.out.println("Integer literal");
  }


  // Types

  public void visit (ArrayType node) {
    System.out.println("ArrayType");
  }

  public void visit (NominalType node) {
    System.out.println("NominalType");
  }

  public void visit (PointerType node) {
    System.out.println("PointerType");
  }

  public void visit (PrimitiveType node) {
    System.out.println("PrimitiveType");
  }

}
