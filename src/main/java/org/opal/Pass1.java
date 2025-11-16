package org.opal;

import org.opal.ast.*;

public class Pass1 extends BaseVisitor {

  private final Counter depth = new Counter();

  public Pass1 (AstNode input) {
    super(input);
  }

  public void process () {
    System.out.println("---");
    visit(root);
  }

  public void visit (AstNode node) {
    printNode(node);
    depth.increment();
    var children = node.getChildren();
    for (var child : children)
      if (child != null)
        visit(child);
    depth.decrement();
  }

  public void printNode (AstNode node) {
    var INDENT_SPACES = 2;
    var spaces = " ".repeat(INDENT_SPACES * depth.get());
    var className = node.getClass().getSimpleName();
    var token = node.getToken();
    System.out.println(spaces + "- " + className + (token != null ? ": " + token : ""));
  }

}
