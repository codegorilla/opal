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
// for both, then this is an error.

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Pass10 extends BaseVisitor {

  private final List<String> sourceLines;

  // Stack for keeping track of current node path
  private final LinkedList<AstNode> nodePath = new LinkedList<>();

  // Stack for passing name information up and down during traversal
  private final LinkedList<String> nameStack = new LinkedList<>();

  // Map that relates alias name to import alias state machine
  // This needs to either synthesize AST nodes or be passed to code generator
  // to be used for generating namespace aliases. Or, we can go back through
  // and attach to each import declaration node the appropriate alias name.
  // This last option actually sounds like the best plan.
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
    var keys = aliasMachineTable.keySet();
    for (var key : keys) {
      var x = aliasMachineTable.get(key);
      // Need to check if error bit is set. If so, create error.
      System.out.println(x.getErrorBit());
      // If no error, then check if node is null.
      System.out.println(x.getNode());
      // If node exists, then we need to mark that node with its alias.
      // The code generator will then use those marks to generate a
      // namespace alias as required.
    }
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
    if (node.hasAsName()) {
      // Request explicit alias
      visit(node.asName());
      var aliasName = nameStack.pop();
      var context = aliasMachineTable.get(aliasName);
      if (context == null) {
        context = new ImportAliasContext();
        context.requestExplicit(node);
        aliasMachineTable.put(aliasName, context);
      } else {
        context.requestExplicit(node);
      }
    } else {
      // Request implicit alias
      visit(node.qualifiedName());
      var aliasName = nameStack.pop();
      var context = aliasMachineTable.get(aliasName);
      if (context == null) {
        context = new ImportAliasContext();
        context.requestImplicit(node);
        aliasMachineTable.put(aliasName, context);
      } else {
        context.requestImplicit(node);
      }
    }
  }

  public void visit (ImportQualifiedName node) {
    visit(node.getLastChild());
  }

  public void visit (ImportName node) {
    nameStack.push(node.getToken().getLexeme());
  }

  public void visit (ImportAsName node) {
    nameStack.push(node.getToken().getLexeme());
  }

}
