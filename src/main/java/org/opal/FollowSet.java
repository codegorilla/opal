package org.opal;

import java.util.EnumSet;

public class FollowSet {

  public static final EnumSet<Token.Kind> TRANSLATION_UNIT = EnumSet.of(Token.Kind.EOF);

  public static final EnumSet<Token.Kind> PACKAGE_DECLARATION =
    union(FirstSet.IMPORT_DECLARATIONS, FirstSet.USE_DECLARATIONS, FirstSet.OTHER_DECLARATIONS, FollowSet.TRANSLATION_UNIT);

  public static final EnumSet<Token.Kind> IMPORT_DECLARATIONS =
    union(FirstSet.USE_DECLARATIONS, FirstSet.OTHER_DECLARATIONS, FollowSet.TRANSLATION_UNIT);

  public static final EnumSet<Token.Kind> IMPORT_DECLARATION =
    union(FirstSet.IMPORT_DECLARATION, FollowSet.IMPORT_DECLARATIONS);

  public static final EnumSet<Token.Kind> IMPORT_QUALIFIED_NAME =
    EnumSet.of(Token.Kind.AS, Token.Kind.SEMICOLON);

  public static final EnumSet<Token.Kind> USE_DECLARATIONS =
    union(FirstSet.OTHER_DECLARATIONS, FollowSet.TRANSLATION_UNIT);

  public static final EnumSet<Token.Kind> USE_DECLARATION =
    union(FirstSet.USE_DECLARATION, FollowSet.USE_DECLARATIONS);

  public static final EnumSet<Token.Kind> OTHER_DECLARATIONS =
    FollowSet.TRANSLATION_UNIT;

  public static final EnumSet<Token.Kind> OTHER_DECLARATION =
    union(FirstSet.OTHER_DECLARATION, FollowSet.OTHER_DECLARATIONS);

  public static final EnumSet<Token.Kind> VARIABLE_DECLARATION =
    FollowSet.OTHER_DECLARATION;





  public static final EnumSet<Token.Kind> ARRAY_DECLARATOR =
    EnumSet.of(Token.Kind.R_PARENTHESIS, Token.Kind.SEMICOLON);


  private static EnumSet<Token.Kind> union (EnumSet<Token.Kind> a, Token.Kind b, Token.Kind c) {
    var combined = EnumSet.copyOf(a);
    combined.add(b);
    combined.add(c);
    return combined;
  }

  // Union of two items

  private static EnumSet<Token.Kind> union (
    EnumSet<Token.Kind> a,
    Token.Kind b
  ) {
    var combined = EnumSet.copyOf(a);
    combined.add(b);
    return combined;
  }

  private static EnumSet<Token.Kind> union (
    EnumSet<Token.Kind> a,
    EnumSet<Token.Kind> b
  ) {
    var combined = EnumSet.copyOf(a);
    combined.addAll(b);
    return combined;
  }

  // Union of three items

  private static EnumSet<Token.Kind> union (
    EnumSet<Token.Kind> a,
    EnumSet<Token.Kind> b,
    Token.Kind c
  ) {
    var combined = EnumSet.copyOf(a);
    combined.addAll(b);
    combined.add(c);
    return combined;
  }

  private static EnumSet<Token.Kind> union (
    EnumSet<Token.Kind> a,
    EnumSet<Token.Kind> b,
    EnumSet<Token.Kind> c
  ) {
    var combined = EnumSet.copyOf(a);
    combined.addAll(b);
    combined.addAll(c);
    return combined;
  }

  // Union of four items

  private static EnumSet<Token.Kind> union (
    EnumSet<Token.Kind> a,
    EnumSet<Token.Kind> b,
    EnumSet<Token.Kind> c,
    Token.Kind d
  ) {
    var combined = EnumSet.copyOf(a);
    combined.addAll(b);
    combined.addAll(c);
    combined.add(d);
    return combined;
  }

  private static EnumSet<Token.Kind> union (
    EnumSet<Token.Kind> a,
    EnumSet<Token.Kind> b,
    EnumSet<Token.Kind> c,
    EnumSet<Token.Kind> d
  ) {
    var combined = EnumSet.copyOf(a);
    combined.addAll(b);
    combined.addAll(c);
    combined.addAll(d);
    return combined;
  }

}
