package org.opal;

import java.util.EnumSet;

// This differs from following set. This is used for single-token insertion
// in the match() method, whereas the former is used to construct a sync set
// for synchronization in the sync() method. The follower set may actually be
// equivalent to the sync set in some situations, but not always. However, it
// is always comprises a subset of the sync set. Unfortunately, we cannot
// automatically construct the follower set from the sync set because it isn't
// easy to automatically determine which elements of the sync set belong to the
// follower set.

// NEW: I think this might be best suited to hold singleton sets that follow
// tokens in a match method call.

public class FollowerSet {

  public static final EnumSet<Token.Kind> COLON =
    EnumSet.of(Token.Kind.EQUAL);

  public static final EnumSet<Token.Kind> EQUAL =
    EnumSet.of(Token.Kind.EQUAL);

  public static final EnumSet<Token.Kind> EXPRESSION =
    EnumSet.of(
      Token.Kind.FALSE,
      Token.Kind.TRUE,
      Token.Kind.CHARACTER_LITERAL,
      Token.Kind.STRING_LITERAL,
      Token.Kind.FLOAT32_LITERAL,
      Token.Kind.FLOAT64_LITERAL,
      Token.Kind.INT32_LITERAL,
      Token.Kind.INT64_LITERAL,
      Token.Kind.UINT32_LITERAL,
      Token.Kind.UINT64_LITERAL,
      Token.Kind.AMPERSAND,
      Token.Kind.ASTERISK,
      Token.Kind.L_PARENTHESIS,
      Token.Kind.MINUS,
      Token.Kind.PLUS,
      Token.Kind.TILDE
    );

  public static final EnumSet<Token.Kind> IDENTIFIER =
    EnumSet.of(Token.Kind.IDENTIFIER);

  public static final EnumSet<Token.Kind> L_PARENTHESIS =
    EnumSet.of(Token.Kind.L_PARENTHESIS);

  public static final EnumSet<Token.Kind> PERIOD =
    EnumSet.of(Token.Kind.PERIOD);

  public static final EnumSet<Token.Kind> SEMICOLON =
    EnumSet.of(Token.Kind.SEMICOLON);

  public static final EnumSet<Token.Kind> OTHER_DECLARATION_USE =
    EnumSet.of(Token.Kind.PRIVATE, Token.Kind.VAL, Token.Kind.VAR, Token.Kind.DEF, Token.Kind.CLASS, Token.Kind.USE);

  public static final EnumSet<Token.Kind> ARRAY_DECLARATOR = EnumSet.of (
    Token.Kind.L_BRACKET,
    Token.Kind.R_PARENTHESIS,
    Token.Kind.SEMICOLON
  );



}
