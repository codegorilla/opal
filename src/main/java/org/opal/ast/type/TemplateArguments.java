package org.opal.ast.type;

import org.opal.Token;
import org.opal.ast.AstNode;
import org.opal.ResultVisitor;
import org.opal.Visitor;

public class TemplateArguments extends AstNode {

  public TemplateArguments (Token token) {
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

}
