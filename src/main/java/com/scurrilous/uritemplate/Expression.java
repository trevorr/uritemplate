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
package com.scurrilous.uritemplate;

import java.util.List;

/**
 * Represents a URI Template expression, which consists of a variable list,
 * prefixed by an optional {@linkplain Operator operator}, within curly braces.
 * 
 * @author Trevor Robinson
 */
public final class Expression
{
    private final Operator operator;
    private final List<Variable> variables;
    final int startIndex;
    final int endIndex;

    Expression(Operator operator, List<Variable> variables, int startIndex, int endIndex)
    {
        this.operator = operator;
        this.variables = variables;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    /**
     * Returns the operator used in this expression.
     * 
     * @return the operator prefixing this expression
     */
    public Operator getOperator()
    {
        return operator;
    }

    /**
     * Returns the list of variables used in this expression.
     * 
     * @return the variable list using in this expression
     */
    public List<Variable> getVariables()
    {
        return variables;
    }

    @Override
    public int hashCode()
    {
        return operator.hashCode() ^ variables.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        final Expression other = (Expression) obj;
        return operator.equals(other.operator) && variables.equals(other.variables);
    }

    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append('{');
        if (operator != Operator.NONE)
            buf.append(operator.getSymbol());
        boolean first = true;
        for (final Variable var : variables)
        {
            if (!first)
                buf.append(',');
            else
                first = false;
            buf.append(var);
        }
        buf.append('}');
        return buf.toString();
    }
}
