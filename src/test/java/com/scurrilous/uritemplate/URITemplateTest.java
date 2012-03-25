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

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

@SuppressWarnings("javadoc")
public class URITemplateTest
{
    private final static Map<String, Object> VARS;

    static
    {
        // 3.2. Expression Expansion

        final Builder<String, Object> builder = ImmutableMap.builder();
        builder.put("count", ImmutableList.of("one", "two", "three"));
        builder.put("dom", ImmutableList.of("example", "com"));
        builder.put("dub", "me/too");
        builder.put("hello", "Hello World!");
        builder.put("half", "50%");
        builder.put("var", "value");
        builder.put("who", "fred");
        builder.put("base", "http://example.com/home/");
        builder.put("path", "/foo/bar");
        builder.put("list", ImmutableList.of("red", "green", "blue"));
        builder.put("keys", ImmutableMap.of("semi", ";", "dot", ".", "comma", ","));
        builder.put("v", "6");
        builder.put("x", "1024");
        builder.put("y", "768");
        builder.put("empty", "");
        builder.put("empty_keys", ImmutableMap.of());
        // undef := null
        VARS = builder.build();
    }

    @Test
    public void test() throws URISyntaxException
    {
        // 3.2.1. Variable Expansion

        checkExpansion("{count}", "one,two,three");
        checkExpansion("{count*}", "one,two,three");
        checkExpansion("{/count}", "/one,two,three");
        checkExpansion("{/count*}", "/one/two/three");
        checkExpansion("{;count}", ";count=one,two,three");
        checkExpansion("{;count*}", ";count=one;count=two;count=three");
        checkExpansion("{?count}", "?count=one,two,three");
        checkExpansion("{?count*}", "?count=one&count=two&count=three");
        checkExpansion("{&count*}", "&count=one&count=two&count=three");

        // 3.2.2. Simple String Expansion: {var}

        checkExpansion("{var}", "value");
        checkExpansion("{hello}", "Hello%20World%21");
        checkExpansion("{half}", "50%25");
        checkExpansion("O{empty}X", "OX");
        checkExpansion("O{undef}X", "OX");
        checkExpansion("{x,y}", "1024,768");
        checkExpansion("{x,hello,y}", "1024,Hello%20World%21,768");
        checkExpansion("?{x,empty}", "?1024,");
        checkExpansion("?{x,undef}", "?1024");
        checkExpansion("?{undef,y}", "?768");
        checkExpansion("{var:3}", "val");
        checkExpansion("{var:30}", "value");
        checkExpansion("{list}", "red,green,blue");
        checkExpansion("{list*}", "red,green,blue");
        checkExpansion("{keys}", "semi,%3B,dot,.,comma,%2C");
        checkExpansion("{keys*}", "semi=%3B,dot=.,comma=%2C");

        // 3.2.3. Reserved expansion: {+var}

        checkExpansion("{+var}", "value");
        checkExpansion("{+hello}", "Hello%20World!");
        checkExpansion("{+half}", "50%25");

        checkExpansion("{base}index", "http%3A%2F%2Fexample.com%2Fhome%2Findex");
        checkExpansion("{+base}index", "http://example.com/home/index");
        checkExpansion("O{+empty}X", "OX");
        checkExpansion("O{+undef}X", "OX");

        checkExpansion("{+path}/here", "/foo/bar/here");
        checkExpansion("here?ref={+path}", "here?ref=/foo/bar");
        checkExpansion("up{+path}{var}/here", "up/foo/barvalue/here");
        checkExpansion("{+x,hello,y}", "1024,Hello%20World!,768");
        checkExpansion("{+path,x}/here", "/foo/bar,1024/here");

        checkExpansion("{+path:6}/here", "/foo/b/here");
        checkExpansion("{+list}", "red,green,blue");
        checkExpansion("{+list*}", "red,green,blue");
        checkExpansion("{+keys}", "semi,;,dot,.,comma,,");
        checkExpansion("{+keys*}", "semi=;,dot=.,comma=,");

        // 3.2.4. Fragment expansion: {#var}

        checkExpansion("{#var}", "#value");
        checkExpansion("{#hello}", "#Hello%20World!");
        checkExpansion("{#half}", "#50%25");
        checkExpansion("foo{#empty}", "foo#");
        checkExpansion("foo{#undef}", "foo");
        checkExpansion("{#x,hello,y}", "#1024,Hello%20World!,768");
        checkExpansion("{#path,x}/here", "#/foo/bar,1024/here");
        checkExpansion("{#path:6}/here", "#/foo/b/here");
        checkExpansion("{#list}", "#red,green,blue");
        checkExpansion("{#list*}", "#red,green,blue");
        checkExpansion("{#keys}", "#semi,;,dot,.,comma,,");
        checkExpansion("{#keys*}", "#semi=;,dot=.,comma=,");

        // 3.2.5. Label expansion with dot-prefix: {.var}

        checkExpansion("{.who}", ".fred");
        checkExpansion("{.who,who}", ".fred.fred");
        checkExpansion("{.half,who}", ".50%25.fred");
        checkExpansion("www{.dom*}", "www.example.com");
        checkExpansion("X{.var}", "X.value");
        checkExpansion("X{.empty}", "X.");
        checkExpansion("X{.undef}", "X");
        checkExpansion("X{.var:3}", "X.val");
        checkExpansion("X{.list}", "X.red,green,blue");
        checkExpansion("X{.list*}", "X.red.green.blue");
        checkExpansion("X{.keys}", "X.semi,%3B,dot,.,comma,%2C");
        checkExpansion("X{.keys*}", "X.semi=%3B.dot=..comma=%2C");
        checkExpansion("X{.empty_keys}", "X");
        checkExpansion("X{.empty_keys*}", "X");

        // 3.2.6. Path segment expansion: {/var}

        checkExpansion("{/who}", "/fred");
        checkExpansion("{/who,who}", "/fred/fred");
        checkExpansion("{/half,who}", "/50%25/fred");
        checkExpansion("{/who,dub}", "/fred/me%2Ftoo");
        checkExpansion("{/var}", "/value");
        checkExpansion("{/var,empty}", "/value/");
        checkExpansion("{/var,undef}", "/value");
        checkExpansion("{/var,x}/here", "/value/1024/here");
        checkExpansion("{/var:1,var}", "/v/value");
        checkExpansion("{/list}", "/red,green,blue");
        checkExpansion("{/list*}", "/red/green/blue");
        checkExpansion("{/list*,path:4}", "/red/green/blue/%2Ffoo");
        checkExpansion("{/keys}", "/semi,%3B,dot,.,comma,%2C");
        checkExpansion("{/keys*}", "/semi=%3B/dot=./comma=%2C");

        // 3.2.7. Path-style parameter expansion: {;var}

        checkExpansion("{;who}", ";who=fred");
        checkExpansion("{;half}", ";half=50%25");
        checkExpansion("{;empty}", ";empty");
        checkExpansion("{;v,empty,who}", ";v=6;empty;who=fred");
        checkExpansion("{;v,bar,who}", ";v=6;who=fred");
        checkExpansion("{;x,y}", ";x=1024;y=768");
        checkExpansion("{;x,y,empty}", ";x=1024;y=768;empty");
        checkExpansion("{;x,y,undef}", ";x=1024;y=768");
        checkExpansion("{;hello:5}", ";hello=Hello");
        checkExpansion("{;list}", ";list=red,green,blue");
        checkExpansion("{;list*}", ";list=red;list=green;list=blue");
        checkExpansion("{;keys}", ";keys=semi,%3B,dot,.,comma,%2C");
        checkExpansion("{;keys*}", ";semi=%3B;dot=.;comma=%2C");

        // 3.2.8. Form-style query expansion: {?var}

        checkExpansion("{?who}", "?who=fred");
        checkExpansion("{?half}", "?half=50%25");
        checkExpansion("{?x,y}", "?x=1024&y=768");
        checkExpansion("{?x,y,empty}", "?x=1024&y=768&empty=");
        checkExpansion("{?x,y,undef}", "?x=1024&y=768");
        checkExpansion("{?var:3}", "?var=val");
        checkExpansion("{?list}", "?list=red,green,blue");
        checkExpansion("{?list*}", "?list=red&list=green&list=blue");
        checkExpansion("{?keys}", "?keys=semi,%3B,dot,.,comma,%2C");
        checkExpansion("{?keys*}", "?semi=%3B&dot=.&comma=%2C");

        // 3.2.9. Form-style query continuation: {&var}

        checkExpansion("{&who}", "&who=fred");
        checkExpansion("{&half}", "&half=50%25");
        checkExpansion("?fixed=yes{&x}", "?fixed=yes&x=1024");
        checkExpansion("{&x,y,empty}", "&x=1024&y=768&empty=");
        checkExpansion("{&x,y,undef}", "&x=1024&y=768");

        checkExpansion("{&var:3}", "&var=val");
        checkExpansion("{&list}", "&list=red,green,blue");
        checkExpansion("{&list*}", "&list=red&list=green&list=blue");
        checkExpansion("{&keys}", "&keys=semi,%3B,dot,.,comma,%2C");
        checkExpansion("{&keys*}", "&semi=%3B&dot=.&comma=%2C");
    }

    private void checkExpansion(final String template, final String expansion)
        throws URISyntaxException
    {
        assertEquals(expansion, new URITemplate(template).expand(VARS).toString());
    }
}
