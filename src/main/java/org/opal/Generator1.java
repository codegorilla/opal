package org.opal;

import org.opal.ast.AstNode;
import org.opal.ast.TranslationUnit;
import org.opal.ast.declaration.*;
import org.opal.ast.expression.*;
import org.opal.ast.type.*;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupDir;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;

// The purpose of this pass is to create a module interface unit.

public class Generator1 extends BaseVisitor {

  private final URL templateDirectoryUrl;
  private final STGroupDir group;

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
    visit(node.declarations());
    ST st = group.getInstanceOf((nullaryEntryPoint ? "translationUnit0" : "translationUnit1"));
    System.out.println("---");
    System.out.println(st.render());
  }

  // DECLARATIONS **************************************************

  public void visit (Declarations node) {
    for (var child : node.getChildren()) {
      visit(child);
    }
  }

  public void visit (RoutineDeclaration node) {
    visit(node.routineName());
    if (isMain.get(node.routineName()))
      visit(node.routineParameters());
  }

  public void visit (RoutineName node) {
    isMain.put(node, node.getToken().getLexeme().equals("main"));
  }

  public void visit (RoutineParameters node) {
    if (node.hasChildren())
      nullaryEntryPoint = false;
  }

}
