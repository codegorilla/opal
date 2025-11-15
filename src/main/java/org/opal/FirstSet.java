package org.opal;

import java.util.EnumSet;
import java.util.Set;

public class FirstSet {

  public static final EnumSet<Token.Kind> OTHER_DECLARATIONS =
    EnumSet.of(Token.Kind.PRIVATE, Token.Kind.VAL, Token.Kind.VAR, Token.Kind.DEF, Token.Kind.CLASS);


  public static final EnumSet<Token.Kind> DECLARATIONS = EnumSet.of (Token.Kind.PACKAGE);

  public static final EnumSet<Token.Kind> PACKAGE_DECLARATION = EnumSet.of (Token.Kind.PACKAGE);

  public static final EnumSet<Token.Kind> IMPORT_DECLARATION = EnumSet.of (Token.Kind.IMPORT);

  public static final EnumSet<Token.Kind> IMPORT_QUALIFIED_NAME  = EnumSet.of (Token.Kind.IDENTIFIER);

  public static final EnumSet<Token.Kind> USE_DECLARATION = EnumSet.of (Token.Kind.USE);

  public static final EnumSet<Token.Kind> CLASS_DECLARATION = EnumSet.of (Token.Kind.CLASS);

  public static final EnumSet<Token.Kind> ROUTINE_DECLARATION = EnumSet.of (Token.Kind.DEF);

  public static final EnumSet<Token.Kind> VARIABLE_DECLARATION = EnumSet.of (Token.Kind.VAL, Token.Kind.VAR);

}
