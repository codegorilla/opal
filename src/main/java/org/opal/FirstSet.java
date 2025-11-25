package org.opal;

import org.opal.ast.expression.*;

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

  public static final EnumSet<Token.Kind> LITERAL = EnumSet.of (
    Token.Kind.FALSE,
    Token.Kind.TRUE,
    Token.Kind.CHARACTER_LITERAL,
    Token.Kind.FLOAT32_LITERAL,
    Token.Kind.FLOAT64_LITERAL,
    Token.Kind.INT32_LITERAL,
    Token.Kind.INT64_LITERAL,
    Token.Kind.NULL,
    Token.Kind.STRING_LITERAL,
    Token.Kind.UINT32_LITERAL,
    Token.Kind.UINT64_LITERAL
  );

}
