package org.opal;

import java.util.HashMap;

public class KeywordTable {

  // Mappings from string to token-kind and vice versa
  private final HashMap<String, Token.Kind> forwardLookupTable = new HashMap<>();
  private final HashMap<Token.Kind, String> reverseLookupTable = new HashMap<>();

  public KeywordTable () {
    buildForwardLookupTable();
    buildReverseLookupTable();
  }

  // The tables are built lazily on demand

  public void buildForwardLookupTable() {
    forwardLookupTable.put("abstract", Token.Kind.ABSTRACT);
    forwardLookupTable.put("and", Token.Kind.AND);
    forwardLookupTable.put("as", Token.Kind.AS);
    forwardLookupTable.put("break", Token.Kind.BREAK);
    forwardLookupTable.put("case", Token.Kind.CASE);
    forwardLookupTable.put("cast", Token.Kind.CAST);
    forwardLookupTable.put("catch", Token.Kind.CATCH);
    forwardLookupTable.put("class", Token.Kind.CLASS);
    forwardLookupTable.put("const", Token.Kind.CONST);
    forwardLookupTable.put("consteval", Token.Kind.CONSTEVAL);
    forwardLookupTable.put("constexpr", Token.Kind.CONSTEXPR);
    forwardLookupTable.put("continue", Token.Kind.CONTINUE);
    forwardLookupTable.put("def", Token.Kind.DEF);
    forwardLookupTable.put("default", Token.Kind.DEFAULT);
    forwardLookupTable.put("delete", Token.Kind.DELETE);
    forwardLookupTable.put("divine", Token.Kind.DIVINE);
    forwardLookupTable.put("do", Token.Kind.DO);
    forwardLookupTable.put("else", Token.Kind.ELSE);
    forwardLookupTable.put("enum", Token.Kind.ENUM);
    forwardLookupTable.put("extends", Token.Kind.EXTENDS);
    forwardLookupTable.put("false", Token.Kind.FALSE);
    forwardLookupTable.put("final", Token.Kind.FINAL);
    forwardLookupTable.put("for", Token.Kind.FOR);
    forwardLookupTable.put("fn", Token.Kind.FN);
    forwardLookupTable.put("fun", Token.Kind.FUN);
    forwardLookupTable.put("goto", Token.Kind.GOTO);
    forwardLookupTable.put("if", Token.Kind.IF);
    forwardLookupTable.put("import", Token.Kind.IMPORT);
    forwardLookupTable.put("in", Token.Kind.IN);
    forwardLookupTable.put("include", Token.Kind.INCLUDE);
    forwardLookupTable.put("loop", Token.Kind.LOOP);
    forwardLookupTable.put("new", Token.Kind.NEW);
    forwardLookupTable.put("nil", Token.Kind.NIL);
    forwardLookupTable.put("noexcept", Token.Kind.NOEXCEPT);
    forwardLookupTable.put("null", Token.Kind.NULL);
    forwardLookupTable.put("or", Token.Kind.OR);
    forwardLookupTable.put("override", Token.Kind.OVERRIDE);
    forwardLookupTable.put("package", Token.Kind.PACKAGE);
    forwardLookupTable.put("private", Token.Kind.PRIVATE);
    forwardLookupTable.put("protected", Token.Kind.PROTECTED);
    forwardLookupTable.put("return", Token.Kind.RETURN);
    forwardLookupTable.put("static", Token.Kind.STATIC);
    forwardLookupTable.put("struct", Token.Kind.STRUCT);
    forwardLookupTable.put("switch", Token.Kind.SWITCH);
    forwardLookupTable.put("template", Token.Kind.TEMPLATE);
    forwardLookupTable.put("this", Token.Kind.THIS);
    forwardLookupTable.put("trait", Token.Kind.TRAIT);
    forwardLookupTable.put("transmute", Token.Kind.TRANSMUTE);
    forwardLookupTable.put("true", Token.Kind.TRUE);
    forwardLookupTable.put("try", Token.Kind.TRY);
    forwardLookupTable.put("typealias", Token.Kind.TYPEALIAS);
    forwardLookupTable.put("union", Token.Kind.UNION);
    forwardLookupTable.put("until", Token.Kind.UNTIL);
    forwardLookupTable.put("use", Token.Kind.USE);
    forwardLookupTable.put("val", Token.Kind.VAL);
    forwardLookupTable.put("var", Token.Kind.VAR);
    forwardLookupTable.put("virtual", Token.Kind.VIRTUAL);
    forwardLookupTable.put("volatile", Token.Kind.VOLATILE);
    forwardLookupTable.put("when", Token.Kind.WHEN);
    forwardLookupTable.put("while", Token.Kind.WHILE);
    forwardLookupTable.put("with", Token.Kind.WITH);
    forwardLookupTable.put("short", Token.Kind.SHORT);
    forwardLookupTable.put("int", Token.Kind.INT);
    forwardLookupTable.put("long", Token.Kind.LONG);
    forwardLookupTable.put("int8", Token.Kind.INT8);
    forwardLookupTable.put("int16", Token.Kind.INT16);
    forwardLookupTable.put("int32", Token.Kind.INT32);
    forwardLookupTable.put("int64", Token.Kind.INT64);
    forwardLookupTable.put("uint", Token.Kind.UINT);
    forwardLookupTable.put("uint8", Token.Kind.UINT8);
    forwardLookupTable.put("uint16", Token.Kind.UINT16);
    forwardLookupTable.put("uint32", Token.Kind.UINT32);
    forwardLookupTable.put("uint64", Token.Kind.UINT64);
    forwardLookupTable.put("float", Token.Kind.FLOAT);
    forwardLookupTable.put("double", Token.Kind.DOUBLE);
    forwardLookupTable.put("float32", Token.Kind.FLOAT32);
    forwardLookupTable.put("float64", Token.Kind.FLOAT64);
    forwardLookupTable.put("void", Token.Kind.VOID);
  }

  // Creation of a reverse lookup table from the forward lookup table is
  // trivial because the key-value mappings are unique.

  public void buildReverseLookupTable () {
    for (var entry : forwardLookupTable.entrySet())
      reverseLookupTable.put(entry.getValue(), entry.getKey());
  }

  public HashMap<String, Token.Kind> getForwardLookupTable () {
    return forwardLookupTable;
  }

  public HashMap<Token.Kind, String> getReverseLookupTable () {
    return reverseLookupTable;
  }
}
