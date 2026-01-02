package org.opal;

import org.opal.ast.AstNode;
import org.opal.ast.TranslationUnit;
import org.opal.ast.declaration.OtherDeclarations;
import org.opal.ast.declaration.VariableDeclaration;
import org.opal.ast.declaration.VariableInitializer;
import org.opal.ast.declaration.VariableTypeSpecifier;
import org.opal.ast.expression.*;
import org.opal.ast.type.*;
import org.opal.symbol.PrimitiveTypeSymbol;
import org.opal.symbol.Scope;
import org.opal.type.ArrayType;
import org.opal.type.PointerType;
import org.opal.type.PrimitiveType;
import org.opal.type.Type;

import java.util.LinkedList;

// The purpose of this pass is compute expression types and maybe
// perform type checking, coercion, etc.

public class Pass40 extends BaseVisitor {

  private final LinkedList<Type> typeStack = new LinkedList<>();

  private Scope currentScope = null;

  private BaseSymbolVisitor symbolVisitor = new BaseSymbolVisitor();

  public Pass40 (AstNode input) {
    super(input);
  }

  public void process () {
    visit((TranslationUnit)root);
  }

  public void visit (TranslationUnit node ) {
    // This is package scope
    currentScope = node.getScope();
    node.otherDeclarations().accept(this);
  }

  public void visit (OtherDeclarations node ) {
    for (var otherDeclaration : node.children())
      otherDeclaration.accept(this);
  }

  public void visit (VariableDeclaration node ) {
    if (node.hasInitializer())
      node.getInitializer().accept(this);
  }

  public void visit (VariableInitializer node) {
    node.getExpression().accept(this);
  }

  // Does every expression have a sub-expression?

  public void visit (Expression node) {
    System.out.println("GOT EXPR");
    node.getSubExpression().accept(this);
  }

  // There are a couple of differences in how conversions are handled between
  // Opal and C++. First, booleans are not considered arithmetic types and
  // cannot be converted to arithmetic types. Second, small integral types
  // (int8, int16, uint8, and uint16) are always promoted to int32, and never
  // uint32. Lastly, after promotion, no implicit conversion between signed and
  // unsigned types is allowed.

  // To do: Note that the integer promotions and conversions are only done for
  // certain binary and unary expressions, not all. For example, it is not done
  // for the (unary) pointer dereference operator.

  // For usual arithmetic conversions, see the following reference:
  // https://en.cppreference.com/w/cpp/language/usual_arithmetic_conversions.html

  public void visit (BinaryExpression node) {
    System.out.println("BIN EXPR");
    node.getLeft().accept(this);
    node.getRight().accept(this);
    var leftType = node.getLeft().getType();
    var rightType = node.getRight().getType();
    if (isFloatingPoint(leftType) && isFloatingPoint(rightType)) {
      if (leftType == rightType) {
        // No conversion required
        node.setType(leftType);
      } else {
        // Convert operand with lower rank to type of other operand
        var leftRank = computeRank(leftType);
        var rightRank = computeRank(rightType);
        if (leftRank < rightRank) {
          // Convert left operand to type of right operand
          var convertNode = new ImplicitConvertExpression();
          convertNode.setType(rightType);
          convertNode.setOperand(node.getLeft());
          node.setLeft(convertNode);
          node.setType(convertNode.getType());
        } else if (leftRank > rightRank) {
          // Convert right operand to type of left operand
          var convertNode = new ImplicitConvertExpression();
          convertNode.setType(leftType);
          convertNode.setOperand(node.getRight());
          node.setRight(convertNode);
          node.setType(convertNode.getType());
        }
      }
    } else if (isIntegral(leftType) && isFloatingPoint(rightType)) {
      // Convert left operand to type of right operand
      var convertNode = new ImplicitConvertExpression();
      convertNode.setType(rightType);
      convertNode.setOperand(node.getLeft());
      node.setLeft(convertNode);
      node.setType(convertNode.getType());
    } else if (isFloatingPoint(leftType) && isIntegral(rightType)) {
      // Convert right operand to type of left operand
      var convertNode = new ImplicitConvertExpression();
      convertNode.setType(leftType);
      convertNode.setOperand(node.getRight());
      node.setRight(convertNode);
      node.setType(convertNode.getType());
    } else if (isIntegral(leftType) && isIntegral(rightType)) {
      // Both operands are of integral type. All small integers (signed or not)
      // are promoted to type int32 before any other operations are performed.
      if (isSmall(leftType)) {
        var promoteNode = new ImplicitPromoteExpression();
        promoteNode.setType(PrimitiveType.INT32);
        promoteNode.setOperand(node.getLeft());
        node.setLeft(promoteNode);
        leftType = promoteNode.getType();
      }
      if (isSmall(rightType)) {
        var promoteNode = new ImplicitPromoteExpression();
        promoteNode.setType(PrimitiveType.INT32);
        promoteNode.setOperand(node.getRight());
        node.setRight(promoteNode);
        rightType = promoteNode.getType();
      }
      if (leftType == rightType) {
        // No conversion required
        node.setType(leftType);
      } else {
        if (isSigned(leftType) == isSigned(rightType)) {
          // Convert operand with lower rank to type of other operand
          var leftRank = computeRank(leftType);
          var rightRank = computeRank(rightType);
          if (leftRank < rightRank) {
            // Convert left operand to type of right operand
            var convertNode = new ImplicitConvertExpression();
            convertNode.setType(rightType);
            convertNode.setOperand(node.getLeft());
            node.setLeft(convertNode);
            node.setType(convertNode.getType());
          } else if (leftRank > rightRank) {
            // Convert right operand to type of left operand
            var convertNode = new ImplicitConvertExpression();
            convertNode.setType(leftType);
            convertNode.setOperand(node.getRight());
            node.setRight(convertNode);
            node.setType(convertNode.getType());
          }
        } else {
          // Different signedness: no implicit conversion permitted
          System.out.println("semantic error: implicit conversion between signed and unsigned types");
        }
      }
    }
  }

  public boolean isFloatingPoint (Type type) {
    return (
      type == PrimitiveType.FLOAT   ||
      type == PrimitiveType.FLOAT32 ||
      type == PrimitiveType.FLOAT64
    );
  }

  public boolean isIntegral (Type type) {
    return (
      type == PrimitiveType.INT    ||
      type == PrimitiveType.INT8   ||
      type == PrimitiveType.INT16  ||
      type == PrimitiveType.INT32  ||
      type == PrimitiveType.INT64  ||
      type == PrimitiveType.UINT   ||
      type == PrimitiveType.UINT8  ||
      type == PrimitiveType.UINT16 ||
      type == PrimitiveType.UINT32 ||
      type == PrimitiveType.UINT64
    );
  }

  public boolean isSigned (Type type) {
    return (
      type == PrimitiveType.INT   ||
      type == PrimitiveType.INT8  ||
      type == PrimitiveType.INT16 ||
      type == PrimitiveType.INT32 ||
      type == PrimitiveType.INT64
    );
  }

  public boolean isSmall (Type type) {
    return (
      type == PrimitiveType.INT8   ||
      type == PrimitiveType.INT16  ||
      type == PrimitiveType.UINT8  ||
      type == PrimitiveType.UINT16
    );
  }

  public int computeRank (Type type) {
    return (type == PrimitiveType.INT32 || type == PrimitiveType.UINT32) ? 32 : 64;
  }

  public void visit (FloatingPointLiteral node) {
    var kind = node.getToken().getKind();
    if (kind == Token.Kind.FLOAT32_LITERAL)
      node.setType(PrimitiveType.FLOAT32);
    else if (kind == Token.Kind.FLOAT64_LITERAL)
      node.setType(PrimitiveType.FLOAT64);
  }

  public void visit (IntegerLiteral node) {
    var kind = node.getToken().getKind();
    if (kind == Token.Kind.INT32_LITERAL)
      node.setType(PrimitiveType.INT32);
    else if (kind == Token.Kind.INT64_LITERAL)
      node.setType(PrimitiveType.INT64);
  }

  public void visit (UnsignedIntegerLiteral node) {
    var kind = node.getToken().getKind();
    if (kind == Token.Kind.UINT32_LITERAL)
      node.setType(PrimitiveType.UINT32);
    else if (kind == Token.Kind.UINT64_LITERAL)
      node.setType(PrimitiveType.UINT64);
  }


  /*
  public void visit (VariableTypeSpecifier node ) {
    node.getDeclarator().accept(this);
    node.setType(typeStack.pop());
  }

  public void visit (Declarator node) {
    node.getDirectDeclarator().accept(this);
    node.getPointerDeclarators().accept(this);
    node.getArrayDeclarators().accept(this);
  }

  public void visit (ArrayDeclarators node) {
    for (var arrayDeclarator : node.children())
      arrayDeclarator.accept(this);
  }

  public void visit (ArrayDeclarator node) {
    var t = new ArrayType();
    t.setElementType(typeStack.pop());
    // Hard code size for now. This will eventually just be a reference to an
    // AST node representing the root of an expression sub-tree.
    t.setSize(12);
    typeStack.push(t);
  }

  public void visit (NominalDeclarator node) {
    var t = new org.opal.type.NominalType();
    t.setString(node.getToken().getLexeme());
    typeStack.push(t);
  }

  public void visit (PointerDeclarators node) {
    for (var pointerDeclarator : node.children())
      pointerDeclarator.accept(this);
  }

  public void visit (PointerDeclarator node) {
    var t = new PointerType();
    t.setPointeeType(typeStack.pop());
    typeStack.push(t);
  }

  public void visit (PrimitiveDeclarator node) {
    var t = new org.opal.type.PrimitiveType();
    // Hard-code INT for now
    t.setKind(org.opal.type.PrimitiveType.Kind.INT);
    typeStack.push(t);
  }
*/

}
