//translationUnit (packageDeclaration, importDeclarations, packageName, declarations, definitions) ::= <<
//// Implementation unit

//<packageDeclaration>

//namespace <packageName; separator="::"> {
//  <declarations; separator="\n\n">

//  <definitions; separator="\n\n">
//}
//>>

// Migh change to elements instead of declarations. Or if we want, we can pass
//things up via stack and let them be rendered in translation unit.

translationUnit (elements) ::= <<
// Implementation unit

<elements>
>>
