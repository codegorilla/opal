package org.opal;

import java.util.EnumSet;

public class FollowSet {

  public static final EnumSet<Token.Kind> TRANSLATION_UNIT = EnumSet.of(Token.Kind.EOF);

  public static final EnumSet<Token.Kind> PACKAGE_DECLARATION = EnumSet.of (
    Token.Kind.IMPORT,
    Token.Kind.USE,
    Token.Kind.PRIVATE,
    Token.Kind.DEF,
    Token.Kind.CLASS,
    Token.Kind.VAR,
    Token.Kind.VAL,
    Token.Kind.EOF
  );

  public static final EnumSet<Token.Kind> IMPORT_DECLARATIONS = EnumSet.of (
    Token.Kind.USE,
    Token.Kind.PRIVATE,
    Token.Kind.DEF,
    Token.Kind.CLASS,
    Token.Kind.VAR,
    Token.Kind.VAL,
    Token.Kind.EOF
  );

  // Not sure if this ever gets used
  public static final EnumSet<Token.Kind> IMPORT_DECLARATION = EnumSet.of (
    Token.Kind.IMPORT,
    Token.Kind.USE,
    Token.Kind.PRIVATE,
    Token.Kind.DEF,
    Token.Kind.CLASS,
    Token.Kind.VAR,
    Token.Kind.VAL,
    Token.Kind.EOF
  );

  public static final EnumSet<Token.Kind> USE_DECLARATIONS = EnumSet.of (
    Token.Kind.PRIVATE,
    Token.Kind.DEF,
    Token.Kind.CLASS,
    Token.Kind.VAR,
    Token.Kind.VAL,
    Token.Kind.EOF
  );

  public static final EnumSet<Token.Kind> USE_DECLARATION = EnumSet.of (
    Token.Kind.USE,
    Token.Kind.PRIVATE,
    Token.Kind.DEF,
    Token.Kind.CLASS,
    Token.Kind.VAR,
    Token.Kind.VAL,
    Token.Kind.EOF
  );


  public static final EnumSet<Token.Kind> OTHER_DECLARATIONS = EnumSet.of (
    Token.Kind.EOF
  );

  public static final EnumSet<Token.Kind> OTHER_DECLARATION = EnumSet.of (
    Token.Kind.PRIVATE,
    Token.Kind.DEF,
    Token.Kind.CLASS,
    Token.Kind.VAR,
    Token.Kind.VAL,
    Token.Kind.EOF
  );



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
