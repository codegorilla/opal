// Scratch space

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

private static EnumSet<Token.Kind> union (EnumSet<Token.Kind> a, EnumSet<Token.Kind> b, EnumSet<Token.Kind> c) {
  var combined = EnumSet.copyOf(a);
  combined.addAll(b);
  combined.addAll(c);
  return combined;
}
