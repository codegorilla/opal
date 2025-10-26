package org.opal;

import org.opal.ast.AstNode;
import org.opal.ast.TranslationUnit;
import org.opal.ast.declaration.*;
import org.opal.ast.expression.*;
import org.opal.ast.type.*;

import org.stringtemplate.v4.*;

import java.net.URL;
import java.util.LinkedList;

// The purpose of this pass is to create a module interface unit.

public class Generator2 extends ResultBaseVisitor <ST> {

  private final URL templateDirectoryUrl;
  private final STGroupDir group;

  // Stack for facilitating out-of-order operations on types. For example, we
  // need to swap the base type for the variable name in order to form a
  // declarator. We also need to invert the order in which arrays and pointers
  // are processed.
  private final LinkedList<ST> stack = new LinkedList<>();

  // Stack for facilitating out-of-order operations
  private final LinkedList<ST> genStack = new LinkedList<>();

  // Stack for keeping track of current node path
  private final LinkedList<AstNode> nodePath = new LinkedList<>();

  // Tracks modifier passes
  private int modifiersPass = 0;

  public Generator2 (AstNode input) {
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

  public ST visit (TranslationUnit node) {
    var st = group.getInstanceOf("interface/translationUnit");
    st.add("packageDeclaration", visit(node.packageDeclaration()));
    st.add("packageName", genStack.pop());
    st.add("importDeclarations", visit(node.importDeclarations()));
    st.add("declarations", visit(node.declarations()));
    System.out.println("---");
    System.out.println(st.render());
    return null;
  }

  // DECLARATIONS **************************************************

  // PACKAGE DECLARATIONS

  // Package declaration is special in that there is only one, and it must
  // appear at the top of the translation unit.

  public ST visit (PackageDeclaration node) {
    var st = group.getInstanceOf("interface/declaration/packageDeclaration");
    visit(node.packageName());
    st.add("packageName", genStack.getFirst());
    return st;
  }

  // For now just support single word package names

  // Normally, we would return string templates, but in this case, we use the
  // general stack to facilitate re-use of the package name in more than one
  // place.

  public ST visit (PackageName node) {
    var st = new ST(node.getToken().getLexeme());
    genStack.push(st);
    return null;
  }

  // IMPORT DECLARATIONS

  public ST visit (ImportDeclarations node) {
    var st = group.getInstanceOf("interface/declaration/importDeclarations");
    for (var child : node.getChildren())
      st.add("importDeclaration", visit(child));
    return st;
  }

  public ST visit (ImportDeclaration node) {
    var st = group.getInstanceOf("interface/declaration/importDeclaration");
    for (var name : node.names())
      st.add("name", visit(name));
    return st;
  }

  public ST visit (ImportName node) {
    return new ST(node.getToken().getLexeme());
  }

  // COMMON DECLARATIONS

  public ST visit (Declarations node) {
    var st = group.getInstanceOf("interface/declaration/declarations");
    for (var child : node.getChildren())
      st.add("declaration", visit(child));
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

  public ST visit (ClassDeclaration node) {
    if (!node.hasExportSpecifier()) {
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

  public ST visit (ClassExtendsClause node) {
    var st = group.getInstanceOf("common/declaration/classExtendsClause");
    st.add("baseClasses", visit(node.baseClasses()));
    return st;
  }

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
    for (var child : node.getChildren()) {
      st.add("memberDeclaration", visit(child));
    }
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

  // TYPEALIAS DECLARATIONS

  public ST visit (TypealiasDeclaration node) {
    if (!node.hasExportSpecifier()) {
      var st = group.getInstanceOf("common/declaration/usingDeclaration");
      st.add("name", visit(node.name()));
      stack.push(emptyDeclarator());
      st.add("type", visit(node.type()));
      st.add("declarator", stack.pop());
      return st;
    } else return null;
  }

  public ST visit (TypealiasName node) {
    return new ST(node.getToken().getLexeme());
  }

  // ROUTINE DECLARATIONS

  // For now, return types are always explicitly required unless the routine
  // returns nothing, in which case it can be omitted. Later on, we will
  // consider various options for return type deduction.

  public ST visit (RoutineDeclaration node) {
    if (!node.hasExportSpecifier()) {
      var st = group.getInstanceOf("common/declaration/functionDeclaration");
      if (node.modifiers().hasChildren())
        st.add("modifiers", visit(node.modifiers()));
      st.add("name", visit(node.name()));
      st.add("parameters", visit(node.parameters()));
      if (node.hasNoexceptSpecifier())
        st.add("noexceptSpecifier", visit(node.noexceptSpecifier()));
      if (node.hasReturnType())
        st.add("returnType", visit(node.returnType()));
      return st;
    } else {
      return null;
    }
  }

  public ST visit (RoutineModifiers node) {
    var st = group.getInstanceOf("common/declaration/functionModifiers");
    for (var modifier : node.getModifiers()) {
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
    visit(node.routineParameterName());
    st.add("typeSpecifier", visit(node.routineParameterTypeSpecifier()));
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
    st.add("type", visit(node.type()));
    return st;
  }

  public ST visit (NoexceptSpecifier node) {
    return new ST(node.getToken().getLexeme());
  }

  public ST visit (RoutineReturnType node) {
    var st = group.getInstanceOf("common/declaration/functionReturnType");
    stack.push(emptyDeclarator());
    st.add("type", visit(node.type()));
    st.add("declarator", stack.pop());
    return st;
  }

  private ST emptyDeclarator () {
    return new ST("");
  }

  // VARIABLE DECLARATIONS

  public ST visit (VariableDeclaration node) {
    if (!node.hasExportSpecifier()) {
      var st = group.getInstanceOf("common/declaration/variableDeclaration");
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
    else
      return null;
  }

  public ST visit (VariableModifiers node) {
    var st = group.getInstanceOf("common/declaration/variableModifiers");
    for (var modifier : node.getModifiers())
      st.add("modifier", visit(modifier));
    return st;
  }

  // In C++, any pointer or raw array portions of the type specifier are
  // transferred to the name to form a so-called "declarator".

  // The variable name is placed on a stack, transformed into a declarator, and
  // then retrieved from the stack.

  // The variable name becomes a "simple declarator", which is the core of the overall C++ declarator that gets built
  // up. A C++ declaration is of the form "typeSpecifier declarator", which is essentially the reverse of the cobalt
  // declaration of the form "var variableName: typeSpecifier" (setting aside the fact that the term "type specifier"
  // has a different interpretation between the two). Due to the need to swap the variable name with the leaf base type
  // from the type specifier, and that the leaf base type may be several levels down in the type expression tree, we
  // will use an explicit stack to facilitate the exchange.

  public ST visit (VariableName node) {
    var st = group.getInstanceOf("common/declarator/simpleDeclarator");
    st.add("name", node.getToken().getLexeme());
    return st;
  }

  public ST visit (VariableTypeSpecifier node) {
    var st = group.getInstanceOf("common/declaration/variableTypeSpecifier");
    st.add("type", visit(node.type()));
    return st;
  }

  public ST visit (VariableInitializer node) {
    var st = group.getInstanceOf("common/declaration/variableInitializer");
    st.add("expression", visit(node.expression()));
    return st;
  }

  // EXPRESSIONS **************************************************

  public ST visit (Expression node) {
    var st = group.getInstanceOf("common/expression/expression");
    st.add("value", visit(node.getChild(0)));
    return st;
  }

  // Perhaps if we know this is the root node, we don't need parenthesis. We
  // can tell if we are the root node because the parent node type will NOT be
  // an expression. We can use not operator and 'instanceof' to check this.

  public ST visit (BinaryExpression node) {
    var st = group.getInstanceOf("common/expression/binaryExpression");
    st.add("operation", node.getToken().getLexeme());
    st.add("leftExpression",  visit(node.leftExpression()));
    st.add("rightExpression", visit(node.rightExpression()));
    return st;
  }

  public ST visit (UnaryExpression node) {
    var st = group.getInstanceOf("common/expression/unaryExpression");
    st.add("operation", node.getToken().getLexeme());
    st.add("expression",  visit(node.expression()));
    return st;
  }

  // Do we need separate string templates for each literal type? It does not appear so.

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

  // TYPES **************************************************

  public ST visit (ArrayType node) {
    var st = group.getInstanceOf("common/declarator/arrayDeclarator");
    st.add("cop", nodePath.get(1) instanceof PointerType);
    st.add("directDeclarator", stack.pop());
    if (node.getChildCount() == 2)
      st.add("expression", visit(node.expression()));
    stack.push(st);
    return visit(node.baseType());
  }

  public ST visit (NominalType node) {
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

  public ST visit (PointerType node) {
    var st = group.getInstanceOf("common/declarator/pointerDeclarator");
    st.add("directDeclarator", stack.pop());
    stack.push(st);
    return visit(node.baseType());
  }

  public ST visit (PrimitiveType node) {
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
