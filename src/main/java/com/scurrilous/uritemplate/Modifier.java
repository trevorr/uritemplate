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
 * Abstract base class of all {@linkplain Variable variable} modifiers.
 * 
 * @author Trevor Robinson
 */
public abstract class Modifier
{
    /**
     * Returns the kind of modifier represented by this modifier object.
     * 
     * @return what kind of modifier this is
     */
    public abstract ModifierKind getKind();

    static final Modifier NONE = new Modifier()
    {
        @Override
        public ModifierKind getKind()
        {
            return ModifierKind.NONE;
        }

        @Override
        public String toString()
        {
            return "";
        }
    };

    static final Modifier EXPLODE = new Modifier()
    {
        @Override
        public ModifierKind getKind()
        {
            return ModifierKind.EXPLODE;
        }

        @Override
        public String toString()
        {
            return "*";
        }
    };
}
