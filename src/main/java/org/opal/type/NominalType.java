package org.opal.type;

public class NominalType extends Type {

  private final String text;

  public NominalType (String text) {
    super(Kind.NOMINAL);
    this.text = text;
  }

  public String getText () {
    return text;
  }

}
