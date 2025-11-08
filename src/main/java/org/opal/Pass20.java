package org.opal;

import org.opal.ast.AstNode;
import org.opal.ast.TranslationUnit;
import org.opal.ast.declaration.*;
import org.opal.state.ImportAliasContext;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

// The purpose of this pass is to create import declarations from use
// declarations. This avoids an extra sub-pass during code generation.

public class Pass20 extends BaseVisitor {

  private final List<String> sourceLines;

  // Stack for keeping track of current node path
  private final LinkedList<AstNode> nodePath = new LinkedList<>();

  // Stack for passing nodes up and down during traversal
  private final LinkedList<AstNode> nodeStack = new LinkedList<>();

  // Stack for passing name information up and down during traversal
  private final LinkedList<String> nameStack = new LinkedList<>();

  // Map that relates alias name to import alias state machine
  // This needs to either synthesize AST nodes or be passed to code generator
  // to be used for generating namespace aliases. Or, we can go back through
  // and attach to each import declaration node the appropriate alias name.
  // This last option actually sounds like the best plan.
  private final HashMap<String, ImportAliasContext> aliasMachineTable = new HashMap<>();

  public Pass20 (AstNode input, List<String> sourceLines) {
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
    visit(node.declarations());
  }

  public void visit (Declarations node) {
    visit(node.useDeclarations());
    visit(node.importDeclarations());
  }

  public void visit (UseDeclarations node) {
    for (var useDeclaration : node.getChildren())
      visit(useDeclaration);
  }

  public void visit (UseDeclaration node) {
    visit(node.useQualifiedName());
  }

  public void visit (UseQualifiedName node) {
    var n = new ImportQualifiedName(null);
    for (var useName : node.getChildren()) {
      visit(useName);
      n.addChild(nodeStack.pop());
    }
  }

  public void visit (UseName node) {
    var n = new ImportName(node.getToken());
    nodeStack.push(n);
  }

  // To do: Although it seems that C++ can handle multiple import declarations
  // of the same module, we might want to avoid such duplication. One way to do
  // this is to convert the qualified names to a string representation and then
  // compare them to any existing qualified names. Such qualified names can be
  // kept in a set data structure to ensure uniqueness.

  public void visit (ImportDeclarations node) {
    while (!nodeStack.isEmpty()) {
      var n = new ImportDeclaration(null);
      n.addChild(nodeStack.pollLast());
      node.addChild(n);
    }
  }

}
