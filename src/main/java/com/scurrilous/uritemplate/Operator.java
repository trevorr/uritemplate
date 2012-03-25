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

/**
 * Represents a URI Template {@linkplain Expression expression} operator.
 * 
 * @author Trevor Robinson
 */
public enum Operator
{
    /**
     * Indicates that no operator is specified, and that simple string expansion
     * (with one or more variables) should be used.
     */
    NONE('\0', ','),
    /**
     * Reserved string expansion operator, which is identical to simple string
     * expansion except that the substituted values may also contain
     * percent-encoded triplets and characters in the reserved set.
     */
    RESERVED('+', ','),
    /**
     * Fragment expansion (aka crosshatch-prefixed) operator, which is identical
     * to reserved expansion except that a crosshatch character (fragment
     * delimiter) is appended first to the result string if any of the variables
     * are defined.
     */
    FRAGMENT('#', ','),
    /**
     * Label expansion (aka dot-prefixed) operator, which is useful for
     * describing URI spaces with varying domain names or path selectors (e.g.,
     * filename extensions). For each defined variable in the variable-list,
     * append "." to the result string and then perform simple variable
     * expansion, with the allowed characters being those in the unreserved set.
     */
    LABEL('.', '.'),
    /**
     * Path segment operator (slash-prefixed), which is useful for describing
     * URI path hierarchies. For each defined variable in the variable-list,
     * append "/" to the result string and then perform simple variable
     * expansion, with the allowed characters being those in the unreserved set
     * (except "/").
     */
    PATH('/', '/'),
    /**
     * Path-style (matrix) parameter (aka semicolon-prefixed) operator, which is
     * useful for describing URI path parameters, such as "path;property" or
     * "path;name=value".
     */
    MATRIX(';', ';'),
    /**
     * Form-style query (aka ampersand-separated) operator, which is useful for
     * describing an entire optional query component.
     */
    QUERY('?', '&'),
    /**
     * Form-style query continuation (aka ampersand-prefixed) operator, which is
     * useful for describing optional &name=value pairs in a template that
     * already contains a literal query component with fixed parameters.
     */
    QUERY_CONTINUATION('&', '&');

    private final char symbol;
    private final char separator;

    private Operator(char symbol, char separator)
    {
        this.symbol = symbol;
        this.separator = separator;
    }

    /**
     * Returns the symbol used to represent this operator in a URI Template, or
     * {@code '\0'} if the operator has no symbol (e.g. {@link #NONE}).
     * 
     * @return the character symbol representing this operator
     */
    public char getSymbol()
    {
        return symbol;
    }

    /**
     * Returns the default separator character used with this operator.
     * 
     * @return the default separator character
     */
    public char getSeparator()
    {
        return separator;
    }
}
