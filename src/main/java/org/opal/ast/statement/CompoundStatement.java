package org.opal.ast.statement;

import org.opal.ResultVisitor;
import org.opal.Token;
import org.opal.Visitor;
import org.opal.ast.AstNode;
import org.opal.symbol.Scope;

import java.util.LinkedList;

public class CompoundStatement extends Statement {

  private final LinkedList<Statement> statements = new LinkedList<>();

  private Scope scope = null;

  public CompoundStatement () {
    super();
  }

  public CompoundStatement (Token token) {
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

  public void addStatement (Statement statement) {
    statements.add(statement);
  }

  public Scope getScope () {
    return scope;
  }

  public void setScope (Scope scope) {
    this.scope = scope;
  }

  public Iterable<Statement> getStatements () {
    return statements;
  }

}
