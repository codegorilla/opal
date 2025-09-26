package org.opal.ast;

import java.util.LinkedList;

import org.stringtemplate.v4.*;

import org.opal.Token;
import org.opal.Visitor;
import org.opal.ResultVisitor;

// We use a normalized heterogeneous AST design (Parr, 96). This
// allows for a relatively simple treatment of child nodes, while
// permitting the ability to annotate some nodes with specialized
// information.

public abstract class AstNode {

//  private ST st;
  private Token token;
  private final LinkedList<AstNode> children;

  public AstNode () {
    token = null;
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

  public int getChildCount () {
    return children.size();
  }

  /*
  public Iterator<AstNode> getChildren () {
    return children.iterator();
  }
  */

  public Iterable<AstNode> getChildren () {
    return children;
  }

//  public ST getST () {
//    return st;
//  }
//
//  public void setST (ST st) {
//    this.st = st;
//  }

  public Token getToken () {
    return token;
  }

  public void setToken (Token token) {
    this.token = token;
  }

  public abstract void accept (Visitor v);

  public abstract <T> T accept (ResultVisitor<T> v);
}
