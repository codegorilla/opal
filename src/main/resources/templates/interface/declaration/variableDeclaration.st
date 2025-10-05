variableDeclaration (accessSpecifier, variableModifiers, typeSpecifier, declarator, initializer) ::= <%
<if(accessSpecifier)><accessSpecifier> <endif>
<if(variableModifiers)><variableModifiers; separator=" "> <endif>
<typeSpecifier> <declarator> = <initializer>;
%>
