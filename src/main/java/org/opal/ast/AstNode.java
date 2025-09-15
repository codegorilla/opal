package org.opal.ast;

import java.util.LinkedList;

import org.opal.Token;
import org.opal.Visitor;

abstract public class AstNode {

  private Token token;
  private LinkedList<AstNode> children;

  public AstNode () {
    children = new LinkedList<>();
  }

  public AstNode (Token token) {
    this.token = token;
    children = new LinkedList<>();
  }

  public void addChild (AstNode node) {
    children.add(node);
  }

  public AstNode getChild (int index) {
    return children.get(index);
  }

  public abstract void accept (Visitor v);

}
