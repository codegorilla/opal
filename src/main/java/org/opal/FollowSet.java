package org.opal;

import java.util.EnumSet;

public class FollowSet {

  public static final EnumSet<Token.Kind> ARRAY_DECLARATOR =
    EnumSet.of(Token.Kind.R_PARENTHESIS, Token.Kind.SEMICOLON);

  public static final EnumSet<Token.Kind> PACKAGE_DECLARATION =
    union(FollowingSet.PACKAGE_DECLARATION, Token.Kind.EOF);

  public static final EnumSet<Token.Kind> IMPORT_DECLARATION =
    union(FollowingSet.IMPORT_DECLARATION, FollowingSet.IMPORT_DECLARATIONS, Token.Kind.EOF);

  
  private static EnumSet<Token.Kind> union (EnumSet<Token.Kind> a, Token.Kind b) {
    var combined = EnumSet.copyOf(a);
    combined.add(b);
    return combined;
  }

  private static EnumSet<Token.Kind> union (EnumSet<Token.Kind> a, Token.Kind b, Token.Kind c) {
    var combined = EnumSet.copyOf(a);
    combined.add(b);
    combined.add(c);
    return combined;
  }

  private static EnumSet<Token.Kind> union (EnumSet<Token.Kind> a, EnumSet<Token.Kind> b) {
    var combined = EnumSet.copyOf(a);
    combined.addAll(b);
    return combined;
  }

  private static EnumSet<Token.Kind> union (EnumSet<Token.Kind> a, EnumSet<Token.Kind> b, Token.Kind c) {
    var combined = EnumSet.copyOf(a);
    combined.addAll(b);
    combined.add(c);
    return combined;
  }

  private static EnumSet<Token.Kind> union (EnumSet<Token.Kind> a, EnumSet<Token.Kind> b, EnumSet<Token.Kind> c) {
    var combined = EnumSet.copyOf(a);
    combined.addAll(b);
    combined.addAll(c);
    return combined;
  }

}
