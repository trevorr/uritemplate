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
 * Represents a variable within a URI Template {@linkplain Expression
 * expression}, which consists of a name, followed by an optional
 * {@linkplain Modifier modifier}.
 * 
 * @author Trevor Robinson
 */
public final class Variable
{
    private final String name;
    private final Modifier modifier;

    Variable(String name, Modifier modifier)
    {
        this.name = name;
        this.modifier = modifier;
    }

    /**
     * Returns the name of this template variable.
     * 
     * @return the name of this variable
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns whether this variable has a modifier.
     * 
     * @return true if and only if the modifier kind is not
     *         {@link ModifierKind#NONE}
     */
    public boolean hasModifier()
    {
        return modifier.getKind() == ModifierKind.NONE;
    }

    /**
     * Returns the modifier associated with this variable.
     * 
     * @return the modifier for this variable
     */
    public Modifier getModifier()
    {
        return modifier;
    }

    @Override
    public int hashCode()
    {
        return name.hashCode() ^ modifier.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        final Variable other = (Variable) obj;
        return name.equals(other.name) && modifier.equals(other.modifier);
    }

    @Override
    public String toString()
    {
        return name + modifier;
    }
}
