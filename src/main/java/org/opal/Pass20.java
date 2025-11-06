package org.opal;

import org.opal.ast.AstNode;
import org.opal.ast.TranslationUnit;
import org.opal.ast.declaration.*;
import org.opal.error.SemanticError;
import org.opal.state.ImportAliasContext;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

// The purpose of this pass is to annotate "use" declarations as being for a
// single element, some elements, or all elements of a package. We could avoid
// the need to annotate the use declaration if our parser was LL(2), but I
// prefer to keep it LL(1) and disambiguate during semantic analysis. It is
// also possible to do the annotating while parsing, but I prefer to keep each
// phase focused on a single concern, so this is outside of the scope of the
// parser.

public class Pass20 extends BaseResultVisitor<UseDeclaration.Kind> {

  private final List<String> sourceLines;

  // Stack for keeping track of current node path
  private final LinkedList<AstNode> nodePath = new LinkedList<>();

  // Stack for passing name information up and down during traversal
  private final LinkedList<String> nameStack = new LinkedList<>();

  public Pass20 (AstNode input, List<String> sourceLines) {
    super(input);
    this.sourceLines = sourceLines;
  }

  public UseDeclaration.Kind process () {
    visit(root);
    return null;
  }

  public UseDeclaration.Kind visit (AstNode node) {
    nodePath.push(node);
    var kind = node.accept(this);
    nodePath.pop();
    return kind;
  }

  public UseDeclaration.Kind visit (TranslationUnit node) {
    System.out.println("Translation unit");
    visit(node.declarations());
    return null;
  }

  // Declarations

  public UseDeclaration.Kind visit (Declarations node) {
    for (var declaration : node.getChildren())
      visit(declaration);
    return null;
  }

  public UseDeclaration.Kind visit (UseDeclarations node) {
    for (var useDeclaration : node.getChildren())
      visit(useDeclaration);
    return null;
  }

  public UseDeclaration.Kind visit (UseDeclaration node) {
    node.setKind(visit(node.useQualifiedName()));
    return null;
  }

  public UseDeclaration.Kind visit (UseQualifiedName node) {
    return visit(node.getLastChild());
  }

  public UseDeclaration.Kind visit (UseName node) {
    return UseDeclaration.Kind.ONE_NAME;
  }

  public UseDeclaration.Kind visit (UseSomeNames node) {
    return UseDeclaration.Kind.SOME_NAMES;
  }

  public UseDeclaration.Kind visit (UseAllNames node) {
    return UseDeclaration.Kind.ALL_NAMES;
  }

}
