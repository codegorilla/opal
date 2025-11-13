package org.opal;

import java.util.EnumSet;
import java.util.Set;

public class FirstSets {

  public static final Set<Token.Kind> DECLARATIONS = EnumSet.of (Token.Kind.PACKAGE);
  
  public static final Set<Token.Kind> PACKAGE_DECLARATION = EnumSet.of (Token.Kind.PACKAGE);

}
