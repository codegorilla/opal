package org.opal;

import org.opal.ast.AstNode;
import org.opal.ast.TranslationUnit;
import org.opal.ast.declaration.*;
import java.util.LinkedList;
import java.util.List;

// The purpose of this pass is to create import declarations from use
// declarations. This avoids an extra sub-pass during code generation.

public class Pass20 extends BaseResultVisitor<AstNode> {

  private final List<String> sourceLines;

  // Stack for keeping track of current node path
  private final LinkedList<AstNode> nodePath = new LinkedList<>();

  public Pass20 (AstNode input, List<String> sourceLines) {
    super(input);
    this.sourceLines = sourceLines;
  }

  @Override
  public AstNode process () {
    visit(root);
    return null;
  }

  public AstNode visit (AstNode node) {
    nodePath.push(node);
    var n = node.accept(this);
    nodePath.pop();
    return n;
  }

  @Override
  public AstNode visit (TranslationUnit node) {
    visit(node.declarations());
    return null;
  }

  @Override
  public AstNode visit (Declarations node) {
    visit(node.useDeclarations());
    return null;
  }

  // To do: Although the C++ standard allows multiple import declarations of
  // the same module, we might want to avoid such duplication. One way to do
  // this is to convert the qualified names to a string representation and then
  // compare the strings to those of any existing qualified names. We could
  // compare the hashes instead. Adding them to a set data structure may
  // accomplish the same thing.

  @Override
  public AstNode visit (UseDeclarations node) {
    var n = ((Declarations)nodePath.get(1)).importDeclarations();
    for (var useDeclaration : node.getChildren())
      n.addChild(visit(useDeclaration));
    return null;
  }

  @Override
  public AstNode visit (UseDeclaration node) {
    var n = new ImportDeclaration(null);
    n.addChild(visit(node.useQualifiedName()));
    return n;
  }

  @Override
  public AstNode visit (UseQualifiedName node) {
    var n = new ImportQualifiedName();
    for (var useName : node.getChildren())
      n.addChild(visit(useName));
    return n;
  }

  @Override
  public AstNode visit (UseName node) {
    return new ImportName(node.getToken());
  }

}
