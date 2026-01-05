package org.opal;

import org.opal.ast.AstNode;
import org.opal.ast.TranslationUnit;
import org.opal.ast.declaration.*;
import org.opal.ast.expression.*;
import org.opal.symbol.Scope;
import org.opal.symbol.VariableSymbol;
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
    System.out.println("PASS 40");
    visit((TranslationUnit)root);
  }

  public void visit (TranslationUnit node ) {
    // This is global scope or package scope? Lets say global scope for now.
    currentScope = node.getScope();
    node.getPackageDeclaration().accept(this);
    node.getOtherDeclarations().accept(this);
  }

  // We never exit package scope, but global and built-in scopes are outer
  // scopes, so resolution can search the higher scopes when recurse is
  // enabled.

  public void visit (PackageDeclaration node) {
    currentScope = node.getScope();
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
    node.getSubExpression().accept(this);
    node.setType(node.getSubExpression().getType());
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
    node.getLeft().accept(this);
    node.getRight().accept(this);
    var leftType = node.getLeft().getType();
    var rightType = node.getRight().getType();
    var operation = node.getToken().getKind();
    if (isFloatingPoint(leftType) && isFloatingPoint(rightType)) {
      if (leftType == rightType) {
        // No conversion required
        var resultType = isComparison(operation) ? PrimitiveType.BOOL : leftType;
        node.setType(resultType);
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
          var resultType = isComparison(operation) ? PrimitiveType.BOOL : convertNode.getType();
          node.setType(resultType);
        } else if (leftRank > rightRank) {
          // Convert right operand to type of left operand
          var convertNode = new ImplicitConvertExpression();
          convertNode.setType(leftType);
          convertNode.setOperand(node.getRight());
          node.setRight(convertNode);
          var resultType = isComparison(operation) ? PrimitiveType.BOOL : convertNode.getType();
          node.setType(resultType);
        }
      }
    } else if (isIntegral(leftType) && isFloatingPoint(rightType)) {
      // Convert left operand to type of right operand
      var convertNode = new ImplicitConvertExpression();
      convertNode.setType(rightType);
      convertNode.setOperand(node.getLeft());
      node.setLeft(convertNode);
      var resultType = isComparison(operation) ? PrimitiveType.BOOL : convertNode.getType();
      node.setType(resultType);
    } else if (isFloatingPoint(leftType) && isIntegral(rightType)) {
      // Convert right operand to type of left operand
      var convertNode = new ImplicitConvertExpression();
      convertNode.setType(leftType);
      convertNode.setOperand(node.getRight());
      node.setRight(convertNode);
      var resultType = isComparison(operation) ? PrimitiveType.BOOL : convertNode.getType();
      node.setType(resultType);
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
        var resultType = isComparison(operation) ? PrimitiveType.BOOL : leftType;
        node.setType(resultType);
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
            var resultType = isComparison(operation) ? PrimitiveType.BOOL : convertNode.getType();
            node.setType(resultType);
          } else if (leftRank > rightRank) {
            // Convert right operand to type of left operand
            var convertNode = new ImplicitConvertExpression();
            convertNode.setType(leftType);
            convertNode.setOperand(node.getRight());
            node.setRight(convertNode);
            var resultType = isComparison(operation) ? PrimitiveType.BOOL : convertNode.getType();
            node.setType(resultType);
          }
        } else {
          // Different signedness: no implicit conversion permitted
          System.out.println("semantic error: implicit conversion between signed and unsigned types");
        }
      }
    } else {
      // Incompatible types
      System.out.println("semantic error: operation not permitted for incompatible types " + leftType + " and " + rightType);
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

  public boolean isComparison (Token.Kind operation) {
    return (
      operation == Token.Kind.EQUAL_EQUAL       ||
      operation == Token.Kind.EXCLAMATION_EQUAL ||
      operation == Token.Kind.GREATER           ||
      operation == Token.Kind.GREATER_EQUAL     ||
      operation == Token.Kind.LESS              ||
      operation == Token.Kind.LESS_EQUAL
    );
  }

  public void visit (FloatingPointLiteral node) {
    var kind = node.getToken().getKind();
    if (kind == Token.Kind.FLOAT32_LITERAL)
      node.setType(PrimitiveType.FLOAT32);
    else if (kind == Token.Kind.FLOAT64_LITERAL)
      node.setType(PrimitiveType.FLOAT64);
  }

  public void visit (BooleanLiteral node) {
    node.setType(PrimitiveType.BOOL);
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

  // We need to get the type of the variable. But to do that, we need to build
  // a dependency tree and try to figure out its type.

  public void visit (Name node) {
    var symbol = currentScope.resolve(node.getToken().getLexeme(), true);
    if (symbol instanceof VariableSymbol) {
      System.out.println(((VariableSymbol) symbol).getType());
      node.setType(((VariableSymbol) symbol).getType());
    }
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
