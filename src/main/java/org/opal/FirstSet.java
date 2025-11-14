package org.opal;

import java.util.EnumSet;
import java.util.Set;

public class FirstSet {

  public static final Set<Token.Kind> DECLARATIONS = EnumSet.of (Token.Kind.PACKAGE);

  public static final Set<Token.Kind> PACKAGE_DECLARATION = EnumSet.of (Token.Kind.PACKAGE);

  public static final Set<Token.Kind> IMPORT_DECLARATION = EnumSet.of (Token.Kind.IMPORT);

  public static final Set<Token.Kind> IMPORT_QUALIFIED_NAME  = EnumSet.of (Token.Kind.IDENTIFIER);

  public static final Set<Token.Kind> USE_DECLARATION = EnumSet.of (Token.Kind.USE);

  public static final Set<Token.Kind> CLASS_DECLARATION = EnumSet.of (Token.Kind.CLASS);

  public static final Set<Token.Kind> ROUTINE_DECLARATION = EnumSet.of (Token.Kind.DEF);

  public static final Set<Token.Kind> VARIABLE_DECLARATION = EnumSet.of (Token.Kind.VAL, Token.Kind.VAR);

}
