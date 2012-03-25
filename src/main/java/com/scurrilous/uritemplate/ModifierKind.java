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
 * Identifies the various kinds of variable modifiers.
 * 
 * @author Trevor Robinson
 */
public enum ModifierKind
{
    /**
     * Indicates that a variable has no modifier.
     */
    NONE,
    /**
     * Explode modifier, indicating that the variable is to be treated as a
     * composite value consisting of either a list of values or an associative
     * array of (name, value) pairs. The expansion process is applied to each
     * member of the composite as if it were listed as a separate variable. Note
     * that this kind of variable specification is significantly less
     * self-documenting than non-exploded variables, since there is less
     * correspondence between the variable name and how the URI reference
     * appears after expansion.
     */
    EXPLODE,
    /**
     * Prefix modifier, indicating that the variable expansion is limited to a
     * prefix of the variable's value string. Prefix modifiers are often used to
     * partition an identifier space hierarchically, as is common in reference
     * indices and hash-based storage. It also serves to limit the expanded
     * value to a maximum number of characters. Prefix modifiers are not
     * applicable to variables that have composite values.
     */
    PREFIX
}
