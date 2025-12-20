package org.opal;

import java.util.EnumSet;

@Deprecated
public class SyncSet {

  public static final EnumSet<Token.Kind> GLOBAL =
    EnumSet.of(Token.Kind.SEMICOLON, Token.Kind.R_BRACE, Token.Kind.EOF);

  // Only used  by check-in?
//  public static final EnumSet<Token.Kind> PACKAGE_DECLARATION =
//    union(FirstSet.PACKAGE_DECLARATION, GLOBAL);

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
