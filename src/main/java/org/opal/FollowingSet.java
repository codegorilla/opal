package org.opal;

import java.util.EnumSet;

// To do: We don't necessarily want FOLLOW sets, but rather following sets
// because what may follow a production terminal depends on context.

// These are really FOLLOWING sets, i.e subsets of FOLLOW sets, rather than
// actual FOLLOW sets.

// According to online sources, EOF should be in the FOLLOW set of the start
// symbol. It may be in the follow sets of other non-terminals.

public class FollowingSet {

  public static final EnumSet<Token.Kind> COMMA =
    EnumSet.of(Token.Kind.COMMA);

  public static final EnumSet<Token.Kind> EQUAL =
    EnumSet.of(Token.Kind.EQUAL);

  public static final EnumSet<Token.Kind> IMPORT =
    EnumSet.of(Token.Kind.IMPORT);

  public static final EnumSet<Token.Kind> L_BRACE =
    EnumSet.of(Token.Kind.L_BRACE);

  public static final EnumSet<Token.Kind> PERIOD =
    EnumSet.of(Token.Kind.PERIOD);

  public static final EnumSet<Token.Kind> SEMICOLON =
    EnumSet.of(Token.Kind.SEMICOLON);

  public static final EnumSet<Token.Kind> USE =
    EnumSet.of(Token.Kind.USE);

}
