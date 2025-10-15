variableDeclaration (modifiers, typeSpecifier, declarator, initializer) ::= <%
<if(modifiers)><modifiers; separator=" "> <endif>
<typeSpecifier> <declarator>
<if(initializer)> <initializer><endif>;
%>
