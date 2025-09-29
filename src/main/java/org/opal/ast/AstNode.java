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
// space/time trade-off in computer science. I am choosing to use parent
// pointers because this allows most of the work to be done during a single
// parsing stage instead of having to maintain a stack in each of multiple
// semantic analysis stages.

public abstract class AstNode {

  private final LinkedList<AstNode> children = new LinkedList<>();
  private AstNode parent = null;
  private Token token;

  public AstNode () {
    token = null;
  }

  public AstNode (Token token) {
    this.token = token;
  }

  public void addChild (AstNode node) {
    children.add(node);
  }

  public AstNode getChild (int index) {
    return children.get(index);
  }

  public int getChildCount () {
    return children.size();
  }
  
  public Iterable<AstNode> getChildren () {
    return children;
  }

  public AstNode getParent () {
    return parent;
  }

  public Token getToken () {
    return token;
  }

  public void setParent (AstNode parent) {
    this.parent = parent;
  }

  public void setToken (Token token) {
    this.token = token;
  }

  public abstract void accept (Visitor v);

  public abstract <T> T accept (ResultVisitor<T> v);
}
