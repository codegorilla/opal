memberVariableDeclaration (accessSpecifier, modifiers, typeSpecifier, declarator, initializer) ::= <%
<if(accessSpecifier)><accessSpecifier>: <endif>
<if(modifiers)><modifiers; separator=" "> <endif>
<typeSpecifier> <declarator>
<if(initializer)> <initializer><endif>;
%>
