package org.opal;

import java.util.EnumSet;

public class SyncSet {

  public static final EnumSet<Token.Kind> GLOBAL =
    EnumSet.of(Token.Kind.SEMICOLON, Token.Kind.R_BRACE, Token.Kind.EOF);

  public static final EnumSet<Token.Kind> OTHER_DECLARATION =
    union(FirstSet.OTHER_DECLARATION, GLOBAL);

  private static EnumSet<Token.Kind> union (
    EnumSet<Token.Kind> a,
    EnumSet<Token.Kind> b
  ) {
    var combined = EnumSet.copyOf(a);
    combined.addAll(b);
    return combined;
  }
}
