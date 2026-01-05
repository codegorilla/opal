package org.opal.symbol;

import org.opal.SymbolVisitor;
import org.opal.type.Type;

import java.util.LinkedList;

public class RoutineSymbol extends Symbol {

  private Type returnType = null;
  private final LinkedList<Type> parameterTypes = new LinkedList<>();

  public RoutineSymbol (String name) {
    super(name);
  }

  @Override
  public void accept (SymbolVisitor v) {
    v.visit(this);
  }

  public void addParameterType (Type parameterType) {
    parameterTypes.add(parameterType);
  }

  public Type getParameterType (int index) {
    return parameterTypes.get(index);
  }

  public Type getReturnType () {
    return returnType;
  }

  public void setReturnType (Type returnType) {
    this.returnType = returnType;
  }

}
