package org.opal;

import org.opal.ast.AstNode;
import org.opal.ast.TranslationUnit;
import org.opal.ast.declaration.*;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupDir;

import java.net.URL;
import java.util.HashMap;

// The purpose of this pass is to create the entry point. The entry point must
// be created with a main function that matches the main function defined in
// the main module.

// In C++, the entry point main function must be defined in the global module
// fragment, outside of any module. However, we can define other functions
// named main as long as they are placed inside namespaces, which distinguishes
// them from the entry point main function. We can leverage this fact to
// implement opal's design of having its "entry point" main function defined
// inside of its main module.

// To do: Only generate entry point code if the main package is being compiled.

public class Generator1 extends BaseVisitor {

  private final URL templateDirectoryUrl;
  private final STGroupDir group;

  // Tracks whether or not methods are named 'main'
  private HashMap<AstNode, Boolean> isMain = new HashMap<>();

  // Assume nullary (zero-argument) entry point unless proven otherwise
  private boolean nullaryEntryPoint = true;

  public Generator1 (AstNode input) {
    super(input);
    templateDirectoryUrl = this.getClass().getClassLoader().getResource("templates/entrypoint");
    group = new STGroupDir(templateDirectoryUrl);
  }

  public void process () {
    visit(root);
  }

  public void visit (AstNode node) {
    node.accept(this);
  }

  public void visit (TranslationUnit node) {
    visit(node.getOtherDeclarations());
    ST st = group.getInstanceOf("translationUnit" + (nullaryEntryPoint ? 0 : 1));
    System.out.println("---");
    System.out.println(st.render());
  }

  // DECLARATIONS **************************************************

  public void visit (Declarations node) {
    visit(node.otherDeclarations());
  }

  public void visit (OtherDeclarations node) {
    for (var child : node.getChildren()) {
      visit(child);
    }
  }

  public void visit (RoutineDeclaration node) {
    visit(node.getName());
    if (isMain.get(node.getName()))
      visit(node.parameters());
  }

  public void visit (RoutineName node) {
    isMain.put(node, node.getToken().getLexeme().equals("main"));
  }

  public void visit (RoutineParameters node) {
    if (node.hasChildren())
      nullaryEntryPoint = false;
  }

}
