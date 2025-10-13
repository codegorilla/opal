variableDeclaration (variableModifiers, typeSpecifier, declarator, initializer) ::= <%
export <if(variableModifiers)><variableModifiers; separator=" "> <endif>
<typeSpecifier> <declarator>
<if(initializer)> <initializer><endif>;
%>
