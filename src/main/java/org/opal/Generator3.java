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

public class Generator3 extends BaseResultVisitor<ST> {

  private final URL templateDirectoryUrl;
  private final STGroupDir group;

  // Stack for facilitating out-of-order operations
  private final LinkedList<ST> genStack = new LinkedList<>();

  // Stack for keeping track of current node path
  private final LinkedList<AstNode> nodePath = new LinkedList<>();

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
    nodePath.push(node);
    var st = node.accept(this);
    nodePath.pop();
    return st;
  }

  // To do: We need to accumulate declarations from all translation units into
  // one list and their corresponding definitions into another list. Then write
  // to file all declarations, followed by all definitions. The following code
  // doesn't yet handle multiple files.

  public ST visit (TranslationUnit node) {
    var st = group.getInstanceOf("implementation/translationUnit");
    st.add("elements", visit(node.declarations()));
    return st;
  }

  // DECLARATIONS **************************************************

  public ST visit (Declarations node) {
    var st = group.getInstanceOf("implementation/elements");
    st.add("moduleDeclaration", visit(node.packageDeclaration()));
    var tempStack = genStack.reversed();
    while (!genStack.isEmpty())
      st.add("moduleName", tempStack.pop());
    st.add("otherDeclarations", visit(node.otherDeclarations()));
    st.add("otherDefinitions", visit(node.otherDeclarations()));
    return st;
  }

  // PACKAGE DECLARATIONS

  public ST visit (PackageDeclaration node) {
    var st = group.getInstanceOf("implementation/declaration/moduleDeclaration");
    st.add("name", visit(node.getPackageName()));
    return st;
  }

  // Normally, we would just return string templates, but in this case, we also
  // use the general stack to facilitate re-use of the package name in more
  // than one place.

  public ST visit (PackageName node) {
    var st = new ST(node.getToken().getLexeme());
    genStack.push(st);
    return st;
  }

  // Tracks whether we are in declarations pass or definitions pass
  private boolean evenPass = true;

  public ST visit (OtherDeclarations node) {
    ST st = evenPass ? otherDeclarationsGroup(node) : otherDefinitionsGroup(node);
    evenPass = !evenPass;
    return st;
  }

  public ST otherDeclarationsGroup (OtherDeclarations node) {
    var st = group.getInstanceOf("implementation/declaration/otherDeclarationsGroup");
    var generator3a = new Generator3a(node);
    // Process multiple times so forward declarations appear in proper order
    st.add("usingDeclarations", generator3a.process());
    st.add("typeDeclarations", generator3a.process());
    st.add("routineDeclarations", generator3a.process());
    st.add("variableDeclarations", generator3a.process());
    st.add("classDeclarations", generator3a.process());
    return st;
  }

  public ST otherDefinitionsGroup (OtherDeclarations node) {
    var st = group.getInstanceOf("implementation/definition/otherDefinitionsGroup");
    var generator3b = new Generator3b(node);
    st.add("definitions", generator3b.process());
    return st;
  }

}
