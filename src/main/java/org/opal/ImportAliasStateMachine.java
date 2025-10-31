package org.opal;

import org.opal.ast.declaration.ImportDeclaration;

class ImportAliasStateMachine {

  public static final int START    = 0;
  public static final int IMPLICIT = 1;
  public static final int EXPLICIT = 2;
  public static final int NONE = 3;
  public static final int ERROR    = 4;

  private int state = START;

  // Track current node
  private ImportDeclaration node = null;

  public ImportAliasStateMachine () {}

  public void transitionExplicit (ImportDeclaration node) {
    if (state == START) {
      System.out.println("transitioning from start to explicit");
      state = EXPLICIT;
      this.node = node;
    } else if (state == IMPLICIT) {
      System.out.println("transitioning from implicit to explicit");
      state = EXPLICIT;
      this.node = node;
    } else if (state == EXPLICIT) {
      System.out.println("transitioning from implicit to error");
      state = ERROR;
      this.node = null;
    } else if (state == NONE) {
      System.out.println("transitioning from none to explicit");
      state = EXPLICIT;
      this.node = node;
    }
  }

  public void transitionImplicit (ImportDeclaration node) {
    if (state == START) {
      System.out.println("transitioning from start to implicit");
      state = IMPLICIT;
      this.node = node;
    } else if (state == IMPLICIT) {
      System.out.println("transitioning from implicit to none");
      state = NONE;
      this.node = null;
    } else if (state == EXPLICIT) {
      System.out.println("remaining at explicit");
    } else if (state == NONE) {
      System.out.println("remaining at none");
    }
  }

  public int getState () {
    return state;
  }

}
