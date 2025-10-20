package org.opal;

import org.opal.ast.AstNode;
import org.opal.ast.TranslationUnit;
import org.opal.ast.declaration.*;
import org.opal.ast.expression.*;
import org.opal.ast.statement.*;
import org.opal.ast.type.*;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupDir;

import java.net.URL;
import java.util.LinkedList;

// The purpose of this pass is to aggregate declarations and definitions for
// the module implementation unit.

public class Generator3 extends ResultBaseVisitor <ST> {

  private final URL templateDirectoryUrl;
  private final STGroupDir group;

  // Stack for facilitating out-of-order operations. For example, we need to swap the base type for the variable name in
  // order to form a declarator. We also need to invert the order in which arrays and pointers are processed.
  private final LinkedList<ST> stack = new LinkedList<>();

  // Stack for keeping track of current node path
  private final LinkedList<AstNode> ancestorStack = new LinkedList<>();

  private int pass = 1;

  // Tracks modifier passes
  private int modifiersPass = 0;

  public Generator3 (AstNode input) {
    super(input);
    templateDirectoryUrl = this.getClass().getClassLoader().getResource("templates");
    group = new STGroupDir(templateDirectoryUrl);
  }

  public ST process () {
    var st = visit(root);
    System.out.println("---");
    System.out.println(st.render());
    // Just return null for now. Maybe return ST later.
    return null;
  }

  public ST visit (AstNode node) {
    ancestorStack.push(node);
    var st = node.accept(this);
    ancestorStack.pop();
    return st;
  }

  public ST visit (TranslationUnit node) {
    var st = group.getInstanceOf("implementation/translationUnit");
    st.add("packageDeclaration", visit(node.packageDeclaration()));
    // Add in declarations
    var generator3a = new Generator3a(node);
    st.add("declarations", generator3a.process());
    // Add in definitions
    var generator3b = new Generator3b(node);
    st.add("definitions", generator3b.process());
    return st;
  }

}
