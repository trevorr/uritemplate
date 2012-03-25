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
 * Represents a prefix modifier, which indicates that the expansion of the
 * associated variable is limited to a prefix of the variable's value string.
 * 
 * @author Trevor Robinson
 */
public final class PrefixModifier extends Modifier
{
    private final int maxLength;

    PrefixModifier(int maxLength)
    {
        this.maxLength = maxLength;
    }

    /**
     * Returns the maximum number of Unicode characters of the value string that
     * will be used in the expansion.
     * 
     * @return the maximum number of characters used from the value string
     */
    public int getMaxLength()
    {
        return maxLength;
    }

    @Override
    public ModifierKind getKind()
    {
        return ModifierKind.PREFIX;
    }

    @Override
    public String toString()
    {
        return ":" + maxLength;
    }
}
