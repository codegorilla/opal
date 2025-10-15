variableDeclaration (modifiers, typeSpecifier, declarator, initializer) ::= <%
export<\ >
<if(modifiers)><modifiers; separator=" "> <endif>
<typeSpecifier> <declarator>
<if(initializer)> <initializer><endif>;
%>
