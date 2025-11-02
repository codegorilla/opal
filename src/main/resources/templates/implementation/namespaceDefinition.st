namespaceDefinition (
  packageName,
  usingDeclarations,
  typeDeclarations,
  routineDeclarations,
  variableDeclarations,
  classDeclarations,
  definitions
 ) ::= <<
namespace <packageName; separator="::"> {
  // Declarations

  <usingDeclarations; separator="\n\n">

  <typeDeclarations; separator="\n\n">

  <routineDeclarations; separator="\n\n">

  <variableDeclarations; separator="\n\n">

  <classDeclarations; separator="\n\n">

  // Definitions

  <definitions; separator="\n\n">
}
>>
