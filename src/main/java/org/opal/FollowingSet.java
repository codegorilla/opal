package org.opal;

import java.util.EnumSet;

// To do: We don't necessarily want FOLLOW sets, but rather following sets
// because what may follow a production terminal depends on context.

// These are really FOLLOWING sets, i.e subsets of FOLLOW sets, rather than
// actual FOLLOW sets.

// According to online sources, EOF should be in the FOLLOW set of the start
// symbol. It may be in the follow sets of other non-terminals.

public class FollowingSet {


//  public static final EnumSet<Token.Kind> COMMA =
//    EnumSet.of(Token.Kind.COMMA);
//
//  public static final EnumSet<Token.Kind> EQUAL =
//    EnumSet.of(Token.Kind.EQUAL);

  public static final EnumSet<Token.Kind> GREATER =
    EnumSet.of(Token.Kind.GREATER);

//  public static final EnumSet<Token.Kind> IMPORT =
//    EnumSet.of(Token.Kind.IMPORT);

  public static final EnumSet<Token.Kind> L_BRACE =
    EnumSet.of(Token.Kind.L_BRACE);


  public static final EnumSet<Token.Kind> PACKAGE_DECLARATION = EnumSet.of(Token.Kind.PACKAGE);

  //    union(FirstSet.IMPORT_DECLARATIONS, FirstSet.USE_DECLARATIONS, FirstSet.OTHER_DECLARATIONS);

  public static final EnumSet<Token.Kind> IMPORT_DECLARATIONS =
    union(FirstSet.USE_DECLARATIONS, FirstSet.OTHER_DECLARATIONS);

  public static final EnumSet<Token.Kind> USE_DECLARATIONS =
    FirstSet.OTHER_DECLARATIONS;

  // No need for a following set for other declarations

  public static final EnumSet<Token.Kind> OTHER_DECLARATION =
    FirstSet.OTHER_DECLARATION;



  /*
  public static final EnumSet<Token.Kind> PERIOD =
    EnumSet.of(Token.Kind.PERIOD);

  public static final EnumSet<Token.Kind> SEMICOLON =
    EnumSet.of(Token.Kind.SEMICOLON);

  public static final EnumSet<Token.Kind> USE =
    EnumSet.of(Token.Kind.USE);
*/

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
