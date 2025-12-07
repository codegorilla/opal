package org.opal.ast.declaration;

import org.opal.ResultVisitor;
import org.opal.Visitor;
import org.opal.ast.AstNode;

import java.util.LinkedList;

public class VariableModifiers extends AstNode {

  private final LinkedList<Modifier> modifiers = new LinkedList<>();

  public VariableModifiers () {}

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

  @Override
  public <T> T accept (ResultVisitor<T> v) {
    return v.visit(this);
  }

  public Iterable<Modifier> modifiers () {
    return modifiers;
  }

}
