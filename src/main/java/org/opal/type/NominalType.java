package org.opal.type;

// This might become a singleton eventually and be looked up in a table.

public class NominalType extends Type {

  private String string;

  public NominalType () {
    super();
  }

  @Override
  public void accept (TypeVisitor v) {
    v.visit(this);
  }

  public String getString () {
    return string;
  }

  public void setString (String string) {
    this.string = string;
  }

}
