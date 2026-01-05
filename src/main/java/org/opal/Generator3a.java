package org.opal;

import org.opal.ast.AstNode;
import org.opal.ast.declaration.*;
import org.opal.ast.expression.*;
import org.opal.ast.type.*;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupDir;

import java.net.URL;
import java.util.LinkedList;

// The purpose of this pass is to create declarations within a module
// interface unit.

public class Generator3a extends BaseResultVisitor<ST> {

  private final URL templateDirectoryUrl;
  private final STGroupDir group;

  // Stack for facilitating out-of-order operations on types. For example, we
  // need to swap the base type for the variable name in order to form a
  // declarator. We also need to invert the order in which arrays and pointers
  // are processed.
  private final LinkedList<ST> stack = new LinkedList<>();

  // Stack for keeping track of current node path
  private final LinkedList<AstNode> nodePath = new LinkedList<>();

  private final int USING_PASS    = 1;
  private final int TYPE_PASS     = 2;
  private final int ROUTINE_PASS  = 3;
  private final int VARIABLE_PASS = 4;
  private final int CLASS_PASS    = 5;

  // There should be several passes: using declarations, types (e.g. forward
  // class declarations), function prototypes, global variables, class
  // declarations.
  private int pass = USING_PASS;

  // Tracks modifier passes
  private int modifiersPass = 0;

  public Generator3a (AstNode input) {
    super(input);
    templateDirectoryUrl = this.getClass().getClassLoader().getResource("templates");
    group = new STGroupDir(templateDirectoryUrl);
  }

  public ST process () {
    return visit(root);
  }

  public ST visit (AstNode node) {
    nodePath.push(node);
    var st = node.accept(this);
    nodePath.pop();
    return st;
  }


  // DECLARATIONS *************************************************************

  // OTHER DECLARATIONS

  // Each visit to other declarations will increment a pass counter, which will
  // cause declarations to appear in the following order: types, routines,
  // variables, classes.

  public ST visit (OtherDeclarations node) {
    var st = group.getInstanceOf("implementation/declaration/otherDeclarations");
    for (var child : node.getChildren())
      st.add("otherDeclaration", visit(child));
    pass += 1;
    return st;
  }

  public ST visit (Modifier node) {
    var token = node.getToken();
    var text = switch (token.getKind()) {
      case Token.Kind.ABSTRACT -> "= 0";
      default -> token.getLexeme();
    };
    return new ST(text);
  }

  // CLASS DECLARATIONS

  // Might need declarations and definitions passes just as with routines

  public ST visit (ClassDeclaration node) {
    if (pass == TYPE_PASS) {
      if (node.hasExportSpecifier()) {
        var st = group.getInstanceOf("common/declaration/classForwardDeclaration");
        st.add("name", visit(node.name()));
        return st;
      } else {
        return null;
      }
    } else if (pass == CLASS_PASS) {
      if (node.hasExportSpecifier()) {
        var st = group.getInstanceOf("common/declaration/classDeclaration");
        if (node.modifiers().hasChildren())
          st.add("modifiers", visit(node.modifiers()));
        st.add("name", visit(node.name()));
        if (node.hasExtendsClause())
          st.add("extendsClause", visit(node.extendsClause()));
        st.add("body", visit(node.body()));
        return st;
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  public ST visit (ClassModifiers node) {
    var st = group.getInstanceOf("common/declaration/classModifiers");
    for (var modifier : node.getModifiers())
      st.add("modifier", visit(modifier));
    return st;
  }

  public ST visit (ClassName node) {
    return new ST(node.getToken().getLexeme());
  }

//  @Deprecated
//  public ST visit (ClassExtendsClause node) {
//    var st = group.getInstanceOf("common/declaration/classExtendsClause");
//    st.add("baseClasses", visit(node.baseClasses()));
//    return st;
//  }

  public ST visit (BaseClasses node) {
    var st = group.getInstanceOf("common/declaration/baseClasses");
    for (var child : node.getChildren())
      st.add("baseClass", visit(child));
    return st;
  }

  public ST visit (BaseClass node) {
    var st = group.getInstanceOf("common/declaration/baseClass");
    st.add("name", node.getToken().getLexeme());
    return st;
  }

  public ST visit (ClassBody node) {
    var st = group.getInstanceOf("common/declaration/classBody");
    for (var child : node.getChildren())
      st.add("memberDeclaration", visit(child));
    return st;
  }

  public ST visit (MemberAccessSpecifier node) {
    var st = group.getInstanceOf("common/declaration/memberAccessSpecifier");
    st.add("value", node.getToken().getLexeme());
    return st;
  }

  public ST visit (MemberTypealiasDeclaration node) {
    var st = group.getInstanceOf("common/declaration/memberUsingDeclaration");
    if (node.hasAccessSpecifier())
      st.add("accessSpecifier", visit(node.accessSpecifier()));
    else
      st.add("accessSpecifier", "public");
    st.add("name", visit(node.name()));
    stack.push(emptyDeclarator());
    st.add("type", visit(node.type()));
    st.add("declarator", stack.pop());
    return st;
  }

  public ST visit (MemberRoutineDeclaration node) {
    var st = group.getInstanceOf("common/declaration/memberFunctionDeclaration");
    if (node.hasAccessSpecifier())
      st.add("accessSpecifier", visit(node.accessSpecifier()));
    else
      st.add("accessSpecifier", "public");
    if (node.modifiers().hasChildren()) {
      st.add("modifiers1", visit(node.modifiers()));
      st.add("modifiers2", visit(node.modifiers()));
    }
    st.add("name", visit(node.name()));
    st.add("parameters", visit(node.parameters()));
    if (node.cvQualifiers().hasChildren())
      st.add("cvQualifiers", visit(node.cvQualifiers()));
    if (node.refQualifiers().hasChildren())
      st.add("refQualifiers", visit(node.refQualifiers()));
    if (node.hasNoexceptSpecifier())
      st.add("noexceptSpecifier", visit(node.noexceptSpecifier()));
    if (node.hasReturnType())
      st.add("returnType", visit(node.returnType()));
    return st;
  }

  public ST visit (MemberRoutineModifiers node) {
    var st = group.getInstanceOf("common/declaration/memberFunctionModifiers");
    if (modifiersPass == 0) {
      for (var modifier : node.getModifiers()) {
        var kind = modifier.getToken().getKind();
        if (
          kind == Token.Kind.CONSTEXPR ||
          kind == Token.Kind.STATIC    ||
          kind == Token.Kind.VIRTUAL
        ) {
          st.add("modifier", visit(modifier));
        }
      }
    } else {
      for (var modifier : node.getModifiers()) {
        var kind = modifier.getToken().getKind();
        if (
          kind == Token.Kind.ABSTRACT ||
          kind == Token.Kind.FINAL    ||
          kind == Token.Kind.OVERRIDE
        ) {
          st.add("modifier", visit(modifier));
        }
      }
    }
    // Alternate between first and second pass
    modifiersPass = (modifiersPass + 1) % 2;
    return st;
  }

  public ST visit (CVQualifiers node) {
    var st = group.getInstanceOf("common/declaration/cvQualifiers");
    for (var qualifier : node.getQualifiers())
      st.add("qualifier", visit(qualifier));
    return st;
  }

  public ST visit (CVQualifier node) {
    return new ST(node.getToken().getLexeme());
  }

  public ST visit (RefQualifiers node) {
    var st = group.getInstanceOf("common/declaration/refQualifiers");
    for (var qualifier : node.getQualifiers())
      st.add("qualifier", visit(qualifier));
    return st;
  }

  public ST visit (RefQualifier node) {
    return new ST(node.getToken().getLexeme());
  }

  public ST visit (MemberVariableDeclaration node) {
    var st = group.getInstanceOf("common/declaration/memberVariableDeclaration");
    if (node.hasAccessSpecifier())
      st.add("accessSpecifier", visit(node.accessSpecifier()));
    else
      st.add("accessSpecifier", "public");
    if (node.modifiers().hasChildren())
      st.add("modifiers", visit(node.modifiers()));
    stack.push(visit(node.name()));
    if (node.hasTypeSpecifier())
      st.add("typeSpecifier", visit(node.typeSpecifier()));
    st.add("declarator", stack.pop());
    if (node.hasInitializer())
      st.add("initializer", visit(node.initializer()));
    return st;
  }

  public ST visit (MemberVariableModifiers node) {
    var st = group.getInstanceOf("common/declaration/memberVariableModifiers");
    for (var modifier : node.getModifiers())
      st.add("modifier", visit(modifier));
    return st;
  }

  // TYPEALIAS DECLARATIONS

  public ST visit (TypealiasDeclaration node) {
    if (pass == TYPE_PASS) {
      if (node.hasExportSpecifier()) {
        var st = group.getInstanceOf("common/declaration/usingTypealiasDeclaration");
        st.add("name", visit(node.name()));
        stack.push(emptyDeclarator());
        st.add("type", visit(node.type()));
        st.add("declarator", stack.pop());
        return st;
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  public ST visit (TypealiasName node) {
    return new ST(node.getToken().getLexeme());
  }

  // ROUTINE DECLARATIONS

  public ST visit (RoutineDeclaration node) {
    if (pass == ROUTINE_PASS) {
      if (node.hasExportSpecifier()) {
        var st = group.getInstanceOf("common/declaration/functionDeclaration");
        if (node.getModifiers().hasChildren()) {
          st.add("modifiers", visit(node.getModifiers()));
        }
        st.add("name", visit(node.getName()));
        st.add("parameters", visit(node.getParameters()));
        if (node.hasNoexceptSpecifier())
          st.add("noexceptSpecifier", visit(node.noexceptSpecifier()));
        if (node.hasReturnTypeSpecifier())
          st.add("returnType", visit(node.getReturnTypeSpecifier()));
        return st;
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  public ST visit (RoutineModifiers node) {
    var st = group.getInstanceOf("common/declaration/functionModifiers");
    for (var modifier : node.children()) {
      var kind = modifier.getToken().getKind();
      if (kind == Token.Kind.CONSTEXPR) {
        st.add("modifier", visit(modifier));
      }
    }
    return st;
  }

  public ST visit (RoutineName node) {
    return new ST(node.getToken().getLexeme());
  }

  public ST visit (RoutineParameters node) {
    var st = group.getInstanceOf("common/declaration/functionParameters");
    for (var child : node.getChildren())
      st.add("functionParameter", visit(child));
    return st;
  }

  public ST visit (RoutineParameter node) {
    var st = group.getInstanceOf("common/declaration/functionParameter");
    visit(node.getName());
    st.add("typeSpecifier", visit(node.getTypeSpecifier()));
    st.add("declarator", stack.pop());
    return st;
  }

  public ST visit(RoutineParameterName node) {
    var st = group.getInstanceOf("common/declarator/simpleDeclarator");
    st.add("name", node.getToken().getLexeme());
    stack.push(st);
    return null;
  }

  public ST visit (RoutineParameterTypeSpecifier node) {
    var st = group.getInstanceOf("common/declaration/functionParameterTypeSpecifier");
    st.add("type", visit(node.getDeclarator()));
    return st;
  }

  public ST visit (NoexceptSpecifier node) {
    return new ST(node.getToken().getLexeme());
  }

  public ST visit (RoutineReturnTypeSpecifier node) {
    var st = group.getInstanceOf("common/declaration/functionReturnType");
    stack.push(emptyDeclarator());
    st.add("type", visit(node.getDeclarator()));
    st.add("declarator", stack.pop());
    return st;
  }

  public ST visit (RoutineBody node) {
    var st = group.getInstanceOf("common/declaration/functionBody");
    st.add("compoundStatement", visit(node.compoundStatement()));
    return st;
  }

  private ST emptyDeclarator () {
    return new ST("");
  }

  // VARIABLE DECLARATIONS

  // In C++, any pointer or raw array portions of the type specifier are transferred to the name to form a so-called
  // "declarator".

  // The variable name is placed on a stack, transformed into a declarator, and then retrieved from the stack. Since
  // the stack never has more than one element, it doesn't actually need to be a stack.

  public ST visit (VariableDeclaration node) {
    if (pass == VARIABLE_PASS) {
      if (node.hasExportSpecifier()) {
        var st = group.getInstanceOf("common/declaration/variableDeclaration");
        if (node.getModifiers().hasChildren())
          st.add("modifiers", visit(node.getModifiers()));
        stack.push(visit(node.getName()));
        if (node.hasTypeSpecifier())
          st.add("typeSpecifier", visit(node.getTypeSpecifier()));
        st.add("declarator", stack.pop());
        if (node.hasInitializer())
          st.add("initializer", visit(node.getInitializer()));
        return st;
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  public ST visit (VariableModifiers node) {
    var st = group.getInstanceOf("common/declaration/variableModifiers");
    for (var modifier : node.children())
      st.add("modifier", visit(modifier));
    return st;
  }

  // The variable name becomes a "simple declarator", which is the core of the overall C++ declarator that gets built
  // up. A C++ declaration is of the form "typeSpecifier declarator", which is essentially the reverse of the cobolt
  // declaration of the form "var variableName: typeSpecifier" (setting aside the fact that the term "type specifier"
  // has a different interpretation between the two). Due to the need to swap the variable name with the base type from
  // the type specifier, and that the base type may be several levels down in the type expression tree, we will use an
  // explicit stack to facilitate the exchange.

  public ST visit (VariableName node) {
    var st = group.getInstanceOf("common/declarator/simpleDeclarator");
    st.add("name", node.getToken().getLexeme());
    return st;
  }

  public ST visit (VariableTypeSpecifier node) {
    var st = group.getInstanceOf("common/declaration/variableTypeSpecifier");
    st.add("type", visit(node.getDeclarator()));
    return st;
  }

  public ST visit (VariableInitializer node) {
    if (node.hasChildren()) {
      var st = group.getInstanceOf("common/declaration/variableInitializer");
      st.add("expression", visit(node.getExpression()));
      return st;
    }
    else
      return null;
  }

  // EXPRESSIONS **************************************************************

  public ST visit (Expression node) {
    var st = group.getInstanceOf("common/expression/expression");
    st.add("value", visit(node.getChild(0)));
    return st;
  }

  // Perhaps if we know this is the root node, we don't need parenthesis.

  public ST visit (BinaryExpression node) {
    var st = group.getInstanceOf("common/expression/binaryExpression");
    st.add("operation", node.getToken().getLexeme());
    st.add("leftExpression",  visit(node.getLeft()));
    st.add("rightExpression", visit(node.getRight()));
    return st;
  }

  public ST visit (UnaryExpression node) {
    var st = group.getInstanceOf("common/expression/unaryExpression");
    st.add("operation", node.getToken().getLexeme());
    st.add("expression",  visit(node.expression()));
    return st;
  }

  public ST visit (CastExpression node) {
    var st = group.getInstanceOf("common/expression/castExpression");
    var operation = switch (node.getToken().getKind()) {
      case Token.Kind.CAST -> "static_cast";
      case Token.Kind.DIVINE -> "dynamic_cast";
      case Token.Kind.TRANSMUTE -> "reinterpret_cast";
      default -> null;
    };
    st.add("operation", operation);
    st.add("type", visit(node.type()));
    st.add("expression", visit(node.expression()));
    return st;
  }

  // Its not clear that we actually need new/delete in Generator 3a. This pass
  // is intended for declarations. Definitions are implemented in Generator 3b.

  public ST visit (DeleteExpression node) {
    var st = group.getInstanceOf("common/expression/deleteExpression");
    st.add("arrayFlag", node.getArrayFlag());
    st.add("expression", visit(node.expression()));
    return st;
  }

  public ST visit (NewExpression node) {
    var st = group.getInstanceOf("common/expression/newExpression");
    stack.push(emptyDeclarator());
    st.add("type", visit(node.type()));
    st.add("declarator", stack.pop());
    if (node.hasInitializer())
      st.add("initializer", visit(node.initializer()));
    return st;
  }

  public ST visit (NewInitializer node) {
    var st = group.getInstanceOf("common/expression/newInitializer");
    for (var argument : node.arguments())
      st.add("argument", visit(argument));
    return st;
  }

  public ST visit (ArraySubscript node) {
    var st = group.getInstanceOf("common/expression/arraySubscript");
    st.add("name", visit(node.name()));
    st.add("subscript", visit(node.subscript()));
    return st;
  }

  public ST visit (DereferencingMemberAccess node) {
    var st = group.getInstanceOf("common/expression/dereferencingMemberAccess");
    st.add("name", visit(node.name()));
    st.add("member", visit(node.member()));
    return st;
  }

  public ST visit (MemberAccess node) {
    var st = group.getInstanceOf("common/expression/memberAccess");
    st.add("name", visit(node.name()));
    st.add("member", visit(node.member()));
    return st;
  }

  public ST visit (RoutineCall node) {
    var st = group.getInstanceOf("common/expression/functionCall");
    st.add("name", visit(node.name()));
    st.add("arguments", visit(node.routineArguments()));
    return st;
  }

  public ST visit (RoutineArguments node) {
    var st = group.getInstanceOf("common/expression/functionArguments");
    for (var routineArgument : node.getChildren())
      st.add("argument", visit(routineArgument));
    return st;
  }

  public ST visit (RoutineArgument node) {
    var st = group.getInstanceOf("common/expression/functionArgument");
    st.add("expression", visit(node.expression()));
    return st;
  }

  public ST visit (Name node) {
    var st = group.getInstanceOf("common/expression/name");
    st.add("value", node.getToken().getLexeme());
    return st;
  }

  public ST visit (NullLiteral node) {
    var st = group.getInstanceOf("common/expression/expression");
    st.add("value", "nullptr");
    return st;
  }

  public ST visit (BooleanLiteral node) {
    var st = group.getInstanceOf("common/expression/expression");
    st.add("value", node.getToken().getLexeme());
    return st;
  }

  public ST visit (CharacterLiteral node) {
    var st = group.getInstanceOf("common/expression/expression");
    st.add("value", node.getToken().getLexeme());
    return st;
  }

  public ST visit (FloatingPointLiteral node) {
    var st = group.getInstanceOf("common/expression/expression");
    st.add("value", node.getToken().getLexeme());
    return st;
  }

  public ST visit (IntegerLiteral node) {
    var st = group.getInstanceOf("common/expression/expression");
    st.add("value", node.getToken().getLexeme());
    return st;
  }

  public ST visit (StringLiteral node) {
    var st = group.getInstanceOf("common/expression/expression");
    st.add("value", node.getToken().getLexeme());
    return st;
  }

  // TYPES ********************************************************************

  /*
  public ST visit (ArrayType node) {
    var st = group.getInstanceOf("common/declarator/arrayDeclarator");
    st.add("cop", nodePath.get(1) instanceof PointerType);
    st.add("directDeclarator", stack.pop());
    if (node.getChildCount() == 2)
      st.add("expression", visit(node.expression()));
    stack.push(st);
    return visit(node.baseType());
  }
   */

  public ST visit (NominalDeclarator node) {
    var st = group.getInstanceOf("common/type/nominalType");
    st.add("name", node.getToken().getLexeme());
    return st;
  }

  public ST visit (TemplateInstantiation node) {
    var st = group.getInstanceOf("common/type/templateInstantiation");
    st.add("name", visit(node.getChild(0)));
    st.add("arguments", visit(node.getChild(1)));
    return st;
  }

  public ST visit (TemplateArguments node) {
    var st = group.getInstanceOf("common/type/templateArguments");
    for (var child : node.getChildren())
      st.add("argument", visit(child));
    return st;
  }

  public ST visit (TemplateArgument node) {
    // Put empty declarator on the stack
    stack.push(new ST(""));
    var st = group.getInstanceOf("common/type/templateArgument");
    st.add("type", visit(node.getChild(0)));
    st.add("declarator", stack.pop());
    return st;
  }

  // To do: Another thing we need to do is determine what kind of template
  // argument it is. If it is a type argument, then we need to parse it as a
  // type; whereas if it is a non-type argument (e.g. variable), then we need
  // to parse it as an expression. This determination can be made via symbol
  // table lookup, but for now just assume it is a type argument.

  /*
  public ST visit (PointerType node) {
    var st = group.getInstanceOf("common/declarator/pointerDeclarator");
    st.add("directDeclarator", stack.pop());
    stack.push(st);
    return visit(node.baseType());
  }
   */

  public ST visit (PrimitiveDeclarator node) {
    var st = group.getInstanceOf("common/type/primitiveType");
    st.add("name", node.getToken().getLexeme());
    return st;
  }

  public ST visit (RoutinePointerType node) {
    var st = group.getInstanceOf("common/declarator/routinePointerDeclarator");
    st.add("directDeclarator", stack.pop());
    for (int i=0; i<node.getChildCount()-1; i++) {
      st.add("parameters", visit(node.getChild(i)));
    }
    stack.push(st);
    // Return type goes here?
    return visit(node.getChild(node.getChildCount()-1));
  }

}
