package org.opal;

// We will need different error classes because each kind of error
// has different kinds of things (e.g. tokens, nodes) to report.

public class Error {

  private final Error.Kind kind;
  private final String message;
  private final Token token;
  private final String sourceLine;

  public Error (Error.Kind kind, String message, Token token, String sourceLine) {
    this.kind = kind;
    this.message = message;
    this.token = token;
    this.sourceLine = sourceLine;
  }

  // We want to construct a summary line followed by some detail lines. The
  // summary line shows the file, line, column, and a brief message. The detail
  // lines show the affected source code line and a marker indicating the
  // specific point where the error occurred.

  public String complete () {
    var s = new StringBuilder();
    s.append(summary());
    s.append('\n');
    s.append(detail());
    return s.toString();
  }

  private String summary () {
    var s = new StringBuilder("semantic error (");
    s.append(token.getLine())
      .append(", ")
      .append(token.getColumn())
      .append("): ")
      .append(message);
    return s.toString();
  }

  private String detail () {
    var s = new StringBuffer();
    s.append("  | ");
    s.append(sourceLine);
    return s.toString();
  }

  public enum Kind {
    LEXICAL,
    SYNTAX,
    SEMANTIC
  }

}


