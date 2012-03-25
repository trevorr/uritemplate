/*
 * Copyright 2012 Trevor Robinson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
grammar URITemplate;

options
{
    language = Java;
}

@header
{
package com.scurrilous.uritemplate;
import java.util.List;
import com.google.common.collect.ImmutableList;
}

@lexer::header
{
package com.scurrilous.uritemplate;
}

@lexer::members
{
private boolean inExpr = false;
}

LITERAL :
    {!inExpr}?=> ~'{'*;

OPEN_BRACE :
    {!inExpr}?=> '{' 
                     {
                      inExpr = true;
                     };

CLOSE_BRACE :
    {inExpr}?=> '}' 
                    {
                     inExpr = false;
                    };

fragment ALPHA :
    'A'..'Z'
    | 'a'..'z';

fragment DIGIT :
    '0'..'9';

fragment NONZERO_DIGIT :
    '1'..'9';

fragment HEX_DIGIT :
    DIGIT
    | 'A'..'F'
    | 'a'..'f';

fragment PCT_ENCODING :
    '%' HEX_DIGIT HEX_DIGIT;

fragment VAR_CHAR :
    ALPHA
    | DIGIT
    | '_'
    | PCT_ENCODING;

POS_INT :
    {inExpr}?=> NONZERO_DIGIT (DIGIT (DIGIT DIGIT?)?)?;

VAR_NAME :
    {inExpr}?=> VAR_CHAR ('.'? VAR_CHAR)*;

expressions returns [List < Expression > exprs] :
    
     {
      ImmutableList.Builder<Expression> exprlist = ImmutableList.builder();
     }
    LITERAL? (expr = expression 
                                {
                                 exprlist.add(expr);
                                }
    LITERAL?)* EOF 
                   {
                    exprs = exprlist.build();
                   };

expression returns [Expression expr] :
    
     {
      ImmutableList.Builder<Variable> varlist = ImmutableList.builder();
     }
    ot = OPEN_BRACE op = operator var = variableSpec 
                                                     {
                                                      varlist.add(var);
                                                     }
    (',' var = variableSpec 
                            {
                             varlist.add(var);
                            }
)* ct = CLOSE_BRACE 
                    {
                     expr = new Expression(op, varlist.build(), ot.getCharPositionInLine(),
                     		ct.getCharPositionInLine() + 1);
                    };

operator returns [Operator op] :
    '+' 
        {
         $op = Operator.RESERVED;
        }
    | '#' 
          {
           $op = Operator.FRAGMENT;
          }
    | '.' 
          {
           $op = Operator.LABEL;
          }
    | '/' 
          {
           $op = Operator.PATH;
          }
    | ';' 
          {
           $op = Operator.MATRIX;
          }
    | '?' 
          {
           $op = Operator.QUERY;
          }
    | '&' 
          {
           $op = Operator.QUERY_CONTINUATION;
          }
    | 
      {
       $op = Operator.NONE;
      };

variableSpec returns [Variable var] :
    (
    name = VAR_NAME
    | name = POS_INT
    )
    mod = variableModifier 
                           {
                            $var = new Variable($name.getText(), mod);
                           };

variableModifier returns [Modifier mod] :
    '*' 
        {
         $mod = Modifier.EXPLODE;
        }
    | ':' i = POS_INT 
                      {
                       $mod = new PrefixModifier(Integer.valueOf($i.getText()));
                      }
    | 
      {
       $mod = Modifier.NONE;
      };
