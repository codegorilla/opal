package org.opal;

import org.opal.ast.AstNode;
import org.opal.ast.TranslationUnit;
import org.opal.ast.declaration.*;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupDir;

import java.net.URL;
import java.util.LinkedList;

// The purpose of this pass is to aggregate declarations and definitions for
// the module implementation unit.

public class Generator3 extends ResultBaseVisitor <ST> {

  private final URL templateDirectoryUrl;
  private final STGroupDir group;

  // Stack for facilitating out-of-order operations
  private final LinkedList<ST> genStack = new LinkedList<>();

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
    return node.accept(this);
  }

  // To do: We need to accumulate declarations from all translation units into
  // one list and their corresponding definitions into another list. Then write
  // to file all declarations, followed by all definitions. The following code
  // doesn't yet handle multiple files.

  public ST visit (TranslationUnit node) {
    var st = group.getInstanceOf("implementation/translationUnit");
    st.add("packageDeclaration", visit(node.packageDeclaration()));
    st.add("packageName", genStack.pop());
    // Add in declarations
    var generator3a = new Generator3a(node);
    st.add("declarations", generator3a.process());
    // Add in definitions
    var generator3b = new Generator3b(node);
    st.add("definitions", generator3b.process());
    return st;
  }

  // DECLARATIONS **************************************************

  // PACKAGE DECLARATIONS

  // Package declaration is special in that there is only one, and it must appear at the top of the translation unit.

  public ST visit (PackageDeclaration node) {
    var st = group.getInstanceOf("implementation/declaration/packageDeclaration");
    visit(node.packageName());
    st.add("packageName", genStack.getFirst());
    return st;
  }

  // For now just support single word package names

  // Normally, we would return string templates, but in this case, we use the
  // general stack to facilitate re-use of the package name in more than one
  // place.

  public ST visit (PackageName node) {
    var st = new ST(node.getToken().getLexeme());
    genStack.push(st);
    return null;
  }

}
