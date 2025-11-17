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

public class FollowerSet {

  public static final EnumSet<Token.Kind> COMMA_R_BRACE =
    EnumSet.of(Token.Kind.COMMA, Token.Kind.R_BRACE);

  public static final EnumSet<Token.Kind> IDENTIFIER =
    EnumSet.of(Token.Kind.IDENTIFIER);

  public static final EnumSet<Token.Kind> PERIOD =
    EnumSet.of(Token.Kind.PERIOD);

  public static final EnumSet<Token.Kind> PERIOD_SEMICOLON =
    EnumSet.of(Token.Kind.PERIOD, Token.Kind.SEMICOLON);

  public static final EnumSet<Token.Kind> SEMICOLON =
    EnumSet.of(Token.Kind.SEMICOLON);

  public static final EnumSet<Token.Kind> OTHER_DECLARATION_USE =
    EnumSet.of(Token.Kind.PRIVATE, Token.Kind.VAL, Token.Kind.VAR, Token.Kind.DEF, Token.Kind.CLASS, Token.Kind.USE);
}
