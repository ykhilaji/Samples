grammar Calculator;

error : UNEXPECTED_CHAR {
     throw new RuntimeException("UNEXPECTED_CHAR=" + $UNEXPECTED_CHAR.text); };

expression: '(' inner=expression ')' # Parentheses
            | left=expression op=POW right=expression # Pow
            | left=expression op=(MULT | DIV) right=expression # Multdiv
            | left=expression op=(PLUS | MINUS) right=expression # Plusminus
            | NUMBER # Literal;

NUMBER : [0-9]+;

PLUS : '+';

MINUS : '-';

MULT : '*';

DIV : '/';

POW : '^';

WS : [ \r\n\t] + -> channel(HIDDEN);

UNEXPECTED_CHAR : .;