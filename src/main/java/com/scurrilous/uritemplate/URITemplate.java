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

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.BufferedTokenStream;
import org.antlr.runtime.RecognitionException;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * Represents a URI Template as defined by the <a
 * href="http://tools.ietf.org/html/draft-gregorio-uritemplate-08">IETF URI
 * Template Internet Draft, Version 8</a>
 * 
 * @author Trevor Robinson
 */
public final class URITemplate implements Serializable
{
    private static final long serialVersionUID = -4992577976294097545L;

    private final String template;
    private transient List<Expression> expressions;

    /**
     * Constructs a new {@link URITemplate} based on parsing the given string.
     * 
     * @param template a URI Template string to parse
     * @throws IllegalArgumentException if the given string does not represent a
     *             valid URI Template
     */
    public URITemplate(String template)
    {
        Preconditions.checkNotNull(template);
        this.template = template;
        readResolve();
    }

    private Object readResolve()
    {
        final URITemplateLexer lexer = new URITemplateLexer(new ANTLRStringStream(template));
        final URITemplateParser parser = new URITemplateParser(new BufferedTokenStream(lexer));
        try
        {
            expressions = parser.expressions();
        }
        catch (RecognitionException e)
        {
            throw new IllegalArgumentException("invalid URI template", e);
        }
        return this;
    }

    /**
     * Returns an immutable list of the variable expansion
     * {@linkplain Expression expressions} used in this URI Template.
     * 
     * @return the list of expressions in this template
     */
    public List<Expression> getExpressions()
    {
        return expressions;
    }

    /**
     * Returns an immutable set of variable names contained in the expressions
     * of this URI Template.
     * 
     * @return the set of variables names in this template
     */
    public Set<String> getVariableNames()
    {
        final ImmutableSet.Builder<String> sb = ImmutableSet.builder();
        for (final Expression expr : expressions)
            for (final Variable var : expr.getVariables())
                sb.add(var.getName());
        return sb.build();
    }

    /**
     * Expands this URI Template into a URI using the given mapping of variable
     * names to values.
     * 
     * @param values a mapping of variable names to values
     * @return a URI based on expanding this template using the given values
     * @throws URISyntaxException if the expanded template is not a valid URI
     */
    public URI expand(Map<String, Object> values) throws URISyntaxException
    {
        if (expressions.isEmpty())
            return new URI(template);

        final StringBuilder buf = new StringBuilder();
        int prevEndIndex = 0;
        for (final Expression expr : expressions)
        {
            // append intervening literal characters to result buffer
            appendEncoded(buf, template, prevEndIndex, expr.startIndex, true);

            // decode operator properties
            final Operator op = expr.getOperator();
            final boolean query = op == Operator.QUERY || op == Operator.QUERY_CONTINUATION;
            final boolean named = query || op == Operator.MATRIX;
            final boolean allowReserved = op == Operator.RESERVED || op == Operator.FRAGMENT;
            final char separator = op.getSeparator();

            boolean firstVar = true;
            for (final Variable var : expr.getVariables())
            {
                final String name = var.getName();
                final Object value = values.get(name);

                // do nothing if name is unknown or value is undefined
                if (isUndefined(value))
                    continue;

                // append the operator's initial or separator character
                if (!firstVar)
                    buf.append(separator);
                else
                {
                    if (op != Operator.NONE && op != Operator.RESERVED)
                        buf.append(op.getSymbol());
                    firstVar = false;
                }

                // decode modifier
                boolean explode = false;
                int maxLength = 0;
                final Modifier mod = var.getModifier();
                switch (mod.getKind())
                {
                case EXPLODE:
                    explode = true;
                    break;
                case PREFIX:
                    maxLength = ((PrefixModifier) mod).getMaxLength();
                    break;
                }

                if (value instanceof Map)
                {
                    final Map<?, ?> map = (Map<?, ?>) value;
                    if (explode)
                    {
                        if (named)
                            appendMap(buf, map, '=', query, separator, allowReserved);
                        else
                            appendMap(buf, map, '=', true, separator, allowReserved);
                    }
                    else
                    {
                        if (named)
                        {
                            appendLiteral(buf, name);
                            buf.append('=');
                        }
                        appendMap(buf, map, ',', true, ',', allowReserved);
                    }
                }
                else if (value instanceof Iterable)
                {
                    final Iterable<?> list = (Iterable<?>) value;
                    if (explode)
                    {
                        if (named)
                            appendList(buf, list, name, query, separator, allowReserved);
                        else
                            appendList(buf, list, null, false, separator, allowReserved);
                    }
                    else
                    {
                        if (named)
                        {
                            appendLiteral(buf, name);
                            buf.append('=');
                        }
                        appendList(buf, list, null, false, ',', allowReserved);
                    }
                }
                else
                {
                    final String str = value.toString();
                    if (named)
                    {
                        appendLiteral(buf, name);
                        if (query || !str.isEmpty())
                            buf.append('=');
                    }
                    if (maxLength <= 0)
                        appendEncoded(buf, str, allowReserved);
                    else
                        appendEncoded(buf, str, 0, codePointIndex(str, maxLength), allowReserved);
                }
            }

            prevEndIndex = expr.endIndex;
        }

        // append remaining literal characters to result buffer
        appendEncoded(buf, template, prevEndIndex, template.length(), true);

        return new URI(buf.toString());
    }

    private static boolean isUndefined(Object value)
    {
        return value == null || (value instanceof Map && isUndefinedMap((Map<?, ?>) value))
            || (value instanceof Iterable && Iterables.isEmpty((Iterable<?>) value));
    }

    private static boolean isUndefinedMap(Map<?, ?> map)
    {
        if (map.isEmpty())
            return true;
        for (final Object value : map.values())
            if (!isUndefined(value))
                return false;
        return true;
    }

    private static void appendList(StringBuilder buf, Iterable<?> list, String varName,
        boolean forceValueSeparator, char separator, boolean allowReserved)
    {
        boolean firstItem = true;
        for (final Object item : list)
        {
            if (isUndefined(item))
                continue;
            if (!firstItem)
                buf.append(separator);
            else
                firstItem = false;
            final String itemStr = item.toString();
            if (varName != null)
            {
                appendLiteral(buf, varName);
                if (forceValueSeparator || !itemStr.isEmpty())
                    buf.append('=');
            }
            appendEncoded(buf, itemStr, allowReserved);
        }
    }

    private static void appendMap(StringBuilder buf, Map<?, ?> map, char valueSeparator,
        boolean forceValueSeparator, char pairSeparator, boolean allowReserved)
    {
        boolean firstItem = true;
        for (final Entry<?, ?> entry : map.entrySet())
        {
            final Object entryValue = entry.getValue();
            if (isUndefined(entryValue))
                continue;
            if (!firstItem)
                buf.append(pairSeparator);
            else
                firstItem = false;
            appendEncoded(buf, entry.getKey().toString(), allowReserved);
            final String entryStr = entryValue.toString();
            if (forceValueSeparator || !entryStr.isEmpty())
                buf.append(valueSeparator);
            appendEncoded(buf, entryStr, allowReserved);
        }
    }

    private static void appendLiteral(StringBuilder buf, String s)
    {
        appendEncoded(buf, s, 0, s.length(), true);
    }

    private static void appendEncoded(StringBuilder buf, String s, boolean allowReserved)
    {
        appendEncoded(buf, s, 0, s.length(), allowReserved);
    }

    private static void appendEncoded(StringBuilder buf, String s, int startIndex, int endIndex,
        boolean allowReserved)
    {
        for (int i = startIndex; i < endIndex;)
        {
            final int c = s.codePointAt(i);
            if (isUnreserved(c) || (allowReserved && isReserved(c)))
            {
                buf.append((char) c);
                ++i;
            }
            else if (c == '%' && i + 2 < endIndex && isHex(s.charAt(i + 1))
                && isHex(s.charAt(i + 2)))
            {
                buf.append(s, i, i + 3);
                i += 3;
            }
            else
            {
                if (c < 0x80)
                    appendPct(buf, c);
                else
                {
                    if (c < 0x800)
                        appendPct(buf, 0xC0 | (c >> 6));
                    else
                    {
                        if (c < 0x10000)
                            appendPct(buf, 0xE0 | (c >> 12));
                        else
                        {
                            if (c < 0x200000)
                                appendPct(buf, 0xF0 | (c >> 18));
                            else
                            {
                                if (c < 0x4000000)
                                    appendPct(buf, 0xF8 | (c >> 24));
                                else
                                {
                                    appendPct(buf, 0xFC | ((c >> 30) & 1));
                                    appendPct(buf, 0x80 | ((c >> 24) & 0x3F));
                                }
                                appendPct(buf, 0x80 | ((c >> 18) & 0x3F));
                            }
                            appendPct(buf, 0x80 | ((c >> 12) & 0x3F));
                            ++i;
                        }
                        appendPct(buf, 0x80 | ((c >> 6) & 0x3F));
                    }
                    appendPct(buf, 0x80 | (c & 0x3F));
                }
                ++i;
            }
        }
    }

    private static final String HEX = "0123456789ABCDEF";

    private static void appendPct(StringBuilder buf, int b)
    {
        buf.append('%');
        buf.append(HEX.charAt(b >> 4));
        buf.append(HEX.charAt(b & 15));
    }

    private static int codePointIndex(String str, int codePoints)
    {
        final int length = str.length();
        for (int i = 0; i < length; i += Character.charCount(str.codePointAt(i)), --codePoints)
        {
            if (codePoints == 0)
                return i;
        }
        return length;
    }

    // @formatter:off
    private static final int UNRESERVED_FIRST = 45;
    private static final int UNRESERVED_LAST = 126;
    private static final int[] UNRESERVED_MASK =
    {
        0xfff01ffb, // -*.0123456789.......ABCDEFGHIJKL
        0xfff43fff, // MNOPQRSTUVWXYZ...._.abcdefghijkl
        0x00023fff  // mnopqrstuvwxyz...~..............
    };

    private static final int RESERVED_FIRST = 33;
    private static final int RESERVED_LAST = 93;
    private static final int[] RESERVED_MASK =
    {
        0xd6004fed, // !.#$.&'()*+,../..........:;.=.?@
        0x14000000  // ..........................[.]...
    };

    private static final int HEX_FIRST = 48;
    private static final int HEX_LAST = 102;
    private static final int[] HEX_MASK =
    {
        0x007e03ff, // 0123456789.......ABCDEF.........
        0x007e0000  // .................abcdef.........
    };
    // @formatter:on

    private static boolean checkMask(int[] mask, int index)
    {
        return (mask[index / 32] & (1 << (index & 31))) != 0;
    }

    private static boolean isUnreserved(int c)
    {
        return c >= UNRESERVED_FIRST && c <= UNRESERVED_LAST
            && checkMask(UNRESERVED_MASK, c - UNRESERVED_FIRST);
    }

    private static boolean isReserved(int c)
    {
        return c >= RESERVED_FIRST && c <= RESERVED_LAST
            && checkMask(RESERVED_MASK, c - RESERVED_FIRST);
    }

    private static boolean isHex(int c)
    {
        return c >= HEX_FIRST && c <= HEX_LAST && checkMask(HEX_MASK, c - HEX_FIRST);
    }

    @Override
    public int hashCode()
    {
        return template.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        return template.equals(((URITemplate) obj).template);
    }

    @Override
    public String toString()
    {
        return template;
    }
}
