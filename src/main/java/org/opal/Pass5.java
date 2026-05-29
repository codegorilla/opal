package org.opal;

import org.opal.ast.AstNode;
import org.opal.ast.TranslationUnit;
import org.opal.error.SemanticError;

import java.util.List;

// The purpose of this pass is to verify that every translation unit begins with
// a package declaration.
//
// The parser deliberately treats the package declaration as optional (it is
// only consumed when PACKAGE is the very first token) to keep its error
// recovery simple. A package declaration that appears out of order is therefore
// reported by the parser as a syntax error during recovery. By the time we
// reach this pass, the only remaining case to detect is a package declaration
// that is absent altogether.

public class Pass5 extends BaseVisitor {

  private final List<String> sourceLines;

  public Pass5 (AstNode input, List<String> sourceLines) {
    super(input);
    this.sourceLines = sourceLines;
  }

  public void process () {
    System.out.println("PASS 5");
    visit((TranslationUnit) root);
  }

  public void visit (TranslationUnit node) {
    if (node.getPackageDeclaration() != null)
      return;

    // The package declaration is missing. Anchor the error on the first
    // declaration in the unit, since that is the spot where a package
    // declaration was expected. If the unit has no declarations at all, there
    // is no source location to point at.
    var offending = firstDeclaration(node);
    if (offending != null)
      System.out.println(new SemanticError(sourceLines,
        "expected package declaration", offending.getToken()));
    else
      System.out.println("semantic error: missing package declaration");
  }

  // Returns the first declaration in the unit, scanning import, use, and other
  // declarations in that (source) order, or null if the unit is empty.

  private AstNode firstDeclaration (TranslationUnit node) {
    AstNode first = null;
    if (node.getImportDeclarations() != null)
      first = first(node.getImportDeclarations().getImportDeclarations());
    if (first == null && node.getUseDeclarations() != null)
      first = first(node.getUseDeclarations().getUseDeclarations());
    if (first == null && node.getOtherDeclarations() != null)
      first = first(node.getOtherDeclarations().getOtherDeclarations());
    return first;
  }

  private static AstNode first (Iterable<? extends AstNode> nodes) {
    var iterator = nodes.iterator();
    return iterator.hasNext() ? iterator.next() : null;
  }

}
