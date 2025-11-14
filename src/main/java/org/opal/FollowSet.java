package org.opal;

import java.util.EnumSet;
import java.util.Set;

public class FollowSet {

  // Short aliases - not sure if we want to use these
//  private static final Token.Kind IMPORT = Token.Kind.IMPORT;
//  private static final Token.Kind USE = Token.Kind.USE;
//  private static final Token.Kind PRIVATE = Token.Kind.PRIVATE;
//  private static final Token.Kind DEF = Token.Kind.DEF;
//  private static final Token.Kind CLASS = Token.Kind.CLASS;
//  private static final Token.Kind VAL = Token.Kind.VAL;
//  private static final Token.Kind VAR = Token.Kind.VAR;
//  private static final Token.Kind EOF = Token.Kind.EOF;

  // To do: We don't necessarily want follow sets, but rather following sets
  // because what may follow a production terminal depends on context. How to
  // handle this?

  // According to online sources, EOF should be in the FOLLOW set of the start
  // symbol. It may be in the follow sets of other non-terminals.

  public static final Set<Token.Kind> DECLARATIONS = EnumSet.of (
    Token.Kind.EOF
  );

  public static final Set<Token.Kind> PACKAGE_DECLARATION = EnumSet.of (
    Token.Kind.IMPORT,
    Token.Kind.USE,
    Token.Kind.PRIVATE,
    Token.Kind.CLASS,
    Token.Kind.DEF,
    Token.Kind.VAL,
    Token.Kind.VAR,
    Token.Kind.EOF
  );

  public static final Set<Token.Kind> IMPORT_DECLARATION = EnumSet.of (
    Token.Kind.IMPORT,
    Token.Kind.USE,
    Token.Kind.PRIVATE,
    Token.Kind.CLASS,
    Token.Kind.DEF,
    Token.Kind.VAL,
    Token.Kind.VAR,
    Token.Kind.EOF
  );

  public static final Set<Token.Kind> IMPORT_QUALIFIED_NAME = EnumSet.of (
    Token.Kind.SEMICOLON
  );

  public static final Set<Token.Kind> USE_DECLARATION = EnumSet.of (
    Token.Kind.USE,
    Token.Kind.PRIVATE,
    Token.Kind.CLASS,
    Token.Kind.DEF,
    Token.Kind.VAL,
    Token.Kind.VAR,
    Token.Kind.EOF
  );

  public static final Set<Token.Kind> CLASS_DECLARATION = EnumSet.of (
    Token.Kind.PRIVATE,
    Token.Kind.CLASS,
    Token.Kind.DEF,
    Token.Kind.VAL,
    Token.Kind.VAR,
    Token.Kind.EOF
  );

  public static final Set<Token.Kind> ROUTINE_DECLARATION = EnumSet.of (
    Token.Kind.PRIVATE,
    Token.Kind.CLASS,
    Token.Kind.DEF,
    Token.Kind.VAL,
    Token.Kind.VAR,
    Token.Kind.EOF
  );

  public static final Set<Token.Kind> VARIABLE_DECLARATION = EnumSet.of (
    Token.Kind.PRIVATE,
    Token.Kind.CLASS,
    Token.Kind.DEF,
    Token.Kind.VAL,
    Token.Kind.VAR,
    Token.Kind.EOF
  );

}
