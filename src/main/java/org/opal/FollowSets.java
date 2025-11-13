package org.opal;

import java.util.EnumSet;
import java.util.Set;

public class FollowSets {

  // To do: We don't necessarily want follow sets, but rather following sets
  // because what may follow a production terminal depends on context. How to
  // handle this?

  // According to online sources, EOF should be in the FOLLOW set of the start
  // symbol. It may be in the follow sets of other non-terminals.

  public static final Set<Token.Kind> DECLARATIONS = EnumSet.of (Token.Kind.EOF);

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

}
