package org.opal.ast;

import java.util.LinkedList;

import org.opal.Token;
import org.opal.Visitor;
import org.opal.ResultVisitor;

// We use a normalized heterogeneous AST design (Parr, 96). This allows for a
// relatively simple treatment of child nodes, while permitting the ability to
// annotate some nodes with specialized information.

// Some operations require traversing upwards through the AST. These are not
// common, but they are important. The two options for implementing this are
// parent pointers and maintaining a stack of nodes. This is the classic
// space/time trade-off in computer science. I am choosing to use a stack to
// keep track of the node path. This turned out to be easier and more elegant,
// albeit slower.

public abstract class AstNode {

  private final LinkedList<AstNode> children = new LinkedList<>();
  private Token token;
  private boolean error = false;

  public AstNode () {
    token = null;
  }

  public AstNode (Token token) {
    this.token = token;
    // Inherit error condition from token
    error = token.getError();
  }

  public void addChild (AstNode node) {
    children.add(node);
  }

  public void addChildren (Iterable<AstNode> nodes) {
    for (var node : nodes)
      children.add(node);
  }

  public boolean hasChildren () {
    return !children.isEmpty();
  }

  public boolean getError () {
    return error;
  }

  public void setError () {
    error = true;
  }

  public AstNode getChild (int index) {
    return children.get(index);
  }

  public int getChildCount () {
    return children.size();
  }

  public AstNode getFirstChild () {
    return children.getFirst();
  }
  
  public AstNode getLastChild () {
    return children.getLast();
  }

  public Iterable<AstNode> getChildren () {
    return children;
  }

  public Token getToken () {
    return token;
  }

  public void setToken (Token token) {
    this.token = token;
  }

  public abstract void accept (Visitor v);

  public abstract <T> T accept (ResultVisitor<T> v);
}
