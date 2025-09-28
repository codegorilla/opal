package org.opal.ast.type;

import org.opal.Token;
import org.opal.ast.AstNode;
import org.opal.ResultVisitor;
import org.opal.Visitor;

public class TemplateInstantiation extends Type {

  public TemplateInstantiation (Token token) {
    super(token);
  }

  @Override
  public void accept (Visitor v) {
    v.visit(this);
  }

  @Override
  public <T> T accept (ResultVisitor<T> v) {
    return v.visit(this);
  }

  public AstNode nominalType () {
    return getChild(0);
  }

}
