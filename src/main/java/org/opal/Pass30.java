package org.opal;

import org.opal.ast.AstNode;
import org.opal.ast.TranslationUnit;
import org.opal.ast.declaration.*;
import org.opal.error.SemanticError;
import org.opal.state.ImportAliasContext;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

// The purpose of this pass is the following: For each use declaration that has
// multiple elements, transform it into multiple use declarations that each
// have a single element. For example "use foo.{ Bar, Baz };" becomes
// "use foo.Bar; use foo.Baz;".

public class Pass30 extends BaseVisitor {

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

  public Pass30 (AstNode input, List<String> sourceLines) {
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
    for (var declaration : node.getChildren())
      visit(declaration);
  }

  // 3. Finally, we remove the new use declarations from the stack and add them
  // to the original list of use declarations.

  // P.S. Trying some functional coding style here.

  public void visit (UseDeclarations node) {
    node.getChildren().forEach(this::visit);
    nodeStack.reversed().forEach(node::addChild);
  }

  // 1. For each use declaration with multiple elements, we push the qualified
  // name (which is shared by all elements) onto the stack.

  public void visit (UseDeclaration node) {
    if (node.getKind() == UseDeclaration.Kind.SOME_NAMES) {
      visit(node.useQualifiedName());
      visit(node.useSomeNames());
    }
  }

  public void visit (UseQualifiedName node) {
    nodeStack.push(node);
  }

  public void visit (UseSomeNames node) {
    for (var useName : node.getChildren())
      visit(useName);
    nodeStack.pop();
  }

  // 2. Now, for each of the multiple elements, we construct a new use
  // declaration with a single element combined with the shared qualified name;
  // and push it onto the stack.

  public void visit (UseName node) {
    var n = new UseDeclaration(null);
    n.setKind(UseDeclaration.Kind.ONE_NAME);
    var p = nodeStack.pop();
    n.addChild(p);
    n.addChild(node);
    nodeStack.push(n);
    nodeStack.push(p);
  }

}
