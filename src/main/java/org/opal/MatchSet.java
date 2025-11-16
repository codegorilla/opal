package org.opal;

import java.util.EnumSet;

public class MatchSet {

  public static final EnumSet<Token.Kind> COMMA_R_BRACE =
    EnumSet.of(Token.Kind.COMMA, Token.Kind.R_BRACE);

  public static final EnumSet<Token.Kind> PERIOD =
    EnumSet.of(Token.Kind.PERIOD);

  public static final EnumSet<Token.Kind> PERIOD_SEMICOLON =
    EnumSet.of(Token.Kind.PERIOD, Token.Kind.SEMICOLON);

}
