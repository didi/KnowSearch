/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.painless;

import org.elasticsearch.test.ESTestCase;

import static java.util.Collections.singletonMap;

public class EqualsTests extends ScriptTestCase {
    public void testTypesEquals() {
        assertEquals(true, exec("return false === false;"));
        assertEquals(false, exec("boolean x = false; boolean y = true; return x === y;"));
        assertEquals(true, exec("boolean x = false; boolean y = false; return x === y;"));
        assertEquals(false, exec("return (byte)3 === (byte)4;"));
        assertEquals(true, exec("byte x = 3; byte y = 3; return x === y;"));
        assertEquals(false, exec("return (char)3 === (char)4;"));
        assertEquals(true, exec("char x = 3; char y = 3; return x === y;"));
        assertEquals(false, exec("return (short)3 === (short)4;"));
        assertEquals(true, exec("short x = 3; short y = 3; return x === y;"));
        assertEquals(false, exec("return (int)3 === (int)4;"));
        assertEquals(true, exec("int x = 3; int y = 3; return x === y;"));
        assertEquals(false, exec("return (long)3 === (long)4;"));
        assertEquals(true, exec("long x = 3; long y = 3; return x === y;"));
        assertEquals(false, exec("return (float)3 === (float)4;"));
        assertEquals(true, exec("float x = 3; float y = 3; return x === y;"));
        assertEquals(false, exec("return (double)3 === (double)4;"));
        assertEquals(true, exec("double x = 3; double y = 3; return x === y;"));

        assertEquals(true, exec("return false == false;"));
        assertEquals(false, exec("boolean x = false; boolean y = true; return x == y;"));
        assertEquals(true, exec("boolean x = false; boolean y = false; return x == y;"));
        assertEquals(false, exec("return (byte)3 == (byte)4;"));
        assertEquals(true, exec("byte x = 3; byte y = 3; return x == y;"));
        assertEquals(false, exec("return (char)3 == (char)4;"));
        assertEquals(true, exec("char x = 3; char y = 3; return x == y;"));
        assertEquals(false, exec("return (short)3 == (short)4;"));
        assertEquals(true, exec("short x = 3; short y = 3; return x == y;"));
        assertEquals(false, exec("return (int)3 == (int)4;"));
        assertEquals(true, exec("int x = 3; int y = 3; return x == y;"));
        assertEquals(false, exec("return (long)3 == (long)4;"));
        assertEquals(true, exec("long x = 3; long y = 3; return x == y;"));
        assertEquals(false, exec("return (float)3 == (float)4;"));
        assertEquals(true, exec("float x = 3; float y = 3; return x == y;"));
        assertEquals(false, exec("return (double)3 == (double)4;"));
        assertEquals(true, exec("double x = 3; double y = 3; return x == y;"));
    }

    public void testTypesNotEquals() {
        assertEquals(false, exec("return true !== true;"));
        assertEquals(true, exec("boolean x = true; boolean y = false; return x !== y;"));
        assertEquals(false, exec("boolean x = false; boolean y = false; return x !== y;"));
        assertEquals(true, exec("return (byte)3 !== (byte)4;"));
        assertEquals(false, exec("byte x = 3; byte y = 3; return x !== y;"));
        assertEquals(true, exec("return (char)3 !== (char)4;"));
        assertEquals(false, exec("char x = 3; char y = 3; return x !== y;"));
        assertEquals(true, exec("return (short)3 !== (short)4;"));
        assertEquals(false, exec("short x = 3; short y = 3; return x !== y;"));
        assertEquals(true, exec("return (int)3 !== (int)4;"));
        assertEquals(false, exec("int x = 3; int y = 3; return x !== y;"));
        assertEquals(true, exec("return (long)3 !== (long)4;"));
        assertEquals(false, exec("long x = 3; long y = 3; return x !== y;"));
        assertEquals(true, exec("return (float)3 !== (float)4;"));
        assertEquals(false, exec("float x = 3; float y = 3; return x !== y;"));
        assertEquals(true, exec("return (double)3 !== (double)4;"));
        assertEquals(false, exec("double x = 3; double y = 3; return x !== y;"));

        assertEquals(false, exec("return true != true;"));
        assertEquals(true, exec("boolean x = true; boolean y = false; return x != y;"));
        assertEquals(false, exec("boolean x = false; boolean y = false; return x != y;"));
        assertEquals(true, exec("return (byte)3 != (byte)4;"));
        assertEquals(false, exec("byte x = 3; byte y = 3; return x != y;"));
        assertEquals(true, exec("return (char)3 != (char)4;"));
        assertEquals(false, exec("char x = 3; char y = 3; return x != y;"));
        assertEquals(true, exec("return (short)3 != (short)4;"));
        assertEquals(false, exec("short x = 3; short y = 3; return x != y;"));
        assertEquals(true, exec("return (int)3 != (int)4;"));
        assertEquals(false, exec("int x = 3; int y = 3; return x != y;"));
        assertEquals(true, exec("return (long)3 != (long)4;"));
        assertEquals(false, exec("long x = 3; long y = 3; return x != y;"));
        assertEquals(true, exec("return (float)3 != (float)4;"));
        assertEquals(false, exec("float x = 3; float y = 3; return x != y;"));
        assertEquals(true, exec("return (double)3 != (double)4;"));
        assertEquals(false, exec("double x = 3; double y = 3; return x != y;"));
    }

    public void testEquals() {
        assertEquals(true, exec("return 3 == 3;"));
        assertEquals(false, exec("int x = 4; int y = 5; x == y"));
        assertEquals(true, exec("int[] x = new int[1]; Object y = x; return x == y;"));
        assertEquals(true, exec("int[] x = new int[1]; Object y = x; return x === y;"));
        assertEquals(false, exec("int[] x = new int[1]; Object y = new int[1]; return x == y;"));
        assertEquals(false, exec("int[] x = new int[1]; Object y = new int[1]; return x === y;"));
        assertEquals(false, exec("Map x = new HashMap(); List y = new ArrayList(); return x == y;"));
        assertEquals(false, exec("Map x = new HashMap(); List y = new ArrayList(); return x === y;"));
    }

    public void testNotEquals() {
        assertEquals(false, exec("return 3 != 3;"));
        assertEquals(true, exec("int x = 4; int y = 5; x != y"));
        assertEquals(false, exec("int[] x = new int[1]; Object y = x; return x != y;"));
        assertEquals(false, exec("int[] x = new int[1]; Object y = x; return x !== y;"));
        assertEquals(true, exec("int[] x = new int[1]; Object y = new int[1]; return x != y;"));
        assertEquals(true, exec("int[] x = new int[1]; Object y = new int[1]; return x !== y;"));
        assertEquals(true, exec("Map x = new HashMap(); List y = new ArrayList(); return x != y;"));
        assertEquals(true, exec("Map x = new HashMap(); List y = new ArrayList(); return x !== y;"));
    }

    public void testBranchEquals() {
        assertEquals(0, exec("def a = (char)'a'; def b = (char)'b'; if (a == b) return 1; else return 0;"));
        assertEquals(1, exec("def a = (char)'a'; def b = (char)'a'; if (a == b) return 1; else return 0;"));
        assertEquals(1, exec("def a = 1; def b = 1; if (a === b) return 1; else return 0;"));
        assertEquals(1, exec("def a = (char)'a'; def b = (char)'a'; if (a === b) return 1; else return 0;"));
        assertEquals(1, exec("def a = (char)'a'; Object b = a; if (a === b) return 1; else return 0;"));
        assertEquals(1, exec("def a = 1; Number b = a; Number c = a; if (c === b) return 1; else return 0;"));
        assertEquals(0, exec("def a = 1; Object b = new HashMap(); if (a === (Object)b) return 1; else return 0;"));
    }
    
    public void testEqualsDefAndPrimitive() {
        /* This test needs an Integer that isn't cached by Integer.valueOf so we draw one randomly. We can't use any fixed integer because
         * we can never be sure that the JVM hasn't configured itself to cache that Integer. It is sneaky like that. */
        int uncachedAutoboxedInt = randomValueOtherThanMany(i -> Integer.valueOf(i) == Integer.valueOf(i), ESTestCase::randomInt);
        assertEquals(true, exec("def x = params.i; int y = params.i; return x == y;", singletonMap("i", uncachedAutoboxedInt), true));
        assertEquals(false, exec("def x = params.i; int y = params.i; return x === y;", singletonMap("i", uncachedAutoboxedInt), true));
        assertEquals(true, exec("def x = params.i; int y = params.i; return y == x;", singletonMap("i", uncachedAutoboxedInt), true));
        assertEquals(false, exec("def x = params.i; int y = params.i; return y === x;", singletonMap("i", uncachedAutoboxedInt), true));

        /* Now check that we use valueOf with the boxing used for comparing primitives to def. For this we need an
         * integer that is cached by Integer.valueOf. The JLS says 0 should always be cached. */
        int cachedAutoboxedInt = 0;
        assertSame(Integer.valueOf(cachedAutoboxedInt), Integer.valueOf(cachedAutoboxedInt));
        assertEquals(true, exec("def x = params.i; int y = params.i; return x == y;", singletonMap("i", cachedAutoboxedInt), true));
        assertEquals(true, exec("def x = params.i; int y = params.i; return x === y;", singletonMap("i", cachedAutoboxedInt), true));
        assertEquals(true, exec("def x = params.i; int y = params.i; return y == x;", singletonMap("i", cachedAutoboxedInt), true));
        assertEquals(true, exec("def x = params.i; int y = params.i; return y === x;", singletonMap("i", cachedAutoboxedInt), true));
    }

    public void testBranchNotEquals() {
        assertEquals(1, exec("def a = (char)'a'; def b = (char)'b'; if (a != b) return 1; else return 0;"));
        assertEquals(0, exec("def a = (char)'a'; def b = (char)'a'; if (a != b) return 1; else return 0;"));
        assertEquals(0, exec("def a = 1; def b = 1; if (a !== b) return 1; else return 0;"));
        assertEquals(0, exec("def a = (char)'a'; def b = (char)'a'; if (a !== b) return 1; else return 0;"));
        assertEquals(0, exec("def a = (char)'a'; Object b = a; if (a !== b) return 1; else return 0;"));
        assertEquals(0, exec("def a = 1; Number b = a; Number c = a; if (c !== b) return 1; else return 0;"));
        assertEquals(1, exec("def a = 1; Object b = new HashMap(); if (a !== (Object)b) return 1; else return 0;"));
    }

    public void testNotEqualsDefAndPrimitive() {
        /* This test needs an Integer that isn't cached by Integer.valueOf so we draw one randomly. We can't use any fixed integer because
         * we can never be sure that the JVM hasn't configured itself to cache that Integer. It is sneaky like that. */
        int uncachedAutoboxedInt = randomValueOtherThanMany(i -> Integer.valueOf(i) == Integer.valueOf(i), ESTestCase::randomInt);
        assertEquals(false, exec("def x = params.i; int y = params.i; return x != y;", singletonMap("i", uncachedAutoboxedInt), true));
        assertEquals(true,  exec("def x = params.i; int y = params.i; return x !== y;", singletonMap("i", uncachedAutoboxedInt), true));
        assertEquals(false, exec("def x = params.i; int y = params.i; return y != x;", singletonMap("i", uncachedAutoboxedInt), true));
        assertEquals(true,  exec("def x = params.i; int y = params.i; return y !== x;", singletonMap("i", uncachedAutoboxedInt), true));

        /* Now check that we use valueOf with the boxing used for comparing primitives to def. For this we need an
         * integer that is cached by Integer.valueOf. The JLS says 0 should always be cached. */
        int cachedAutoboxedInt = 0;
        assertSame(Integer.valueOf(cachedAutoboxedInt), Integer.valueOf(cachedAutoboxedInt));
        assertEquals(false, exec("def x = params.i; int y = params.i; return x != y;", singletonMap("i", cachedAutoboxedInt), true));
        assertEquals(false,  exec("def x = params.i; int y = params.i; return x !== y;", singletonMap("i", cachedAutoboxedInt), true));
        assertEquals(false, exec("def x = params.i; int y = params.i; return y != x;", singletonMap("i", cachedAutoboxedInt), true));
        assertEquals(false,  exec("def x = params.i; int y = params.i; return y !== x;", singletonMap("i", cachedAutoboxedInt), true));
    }

    public void testRightHandNull() {
        assertEquals(false, exec("HashMap a = new HashMap(); return a == null;"));
        assertEquals(false, exec("HashMap a = new HashMap(); return a === null;"));
        assertEquals(true, exec("HashMap a = new HashMap(); return a != null;"));
        assertEquals(true, exec("HashMap a = new HashMap(); return a !== null;"));
    }

    public void testLeftHandNull() {
        assertEquals(false, exec("HashMap a = new HashMap(); return null == a;"));
        assertEquals(false, exec("HashMap a = new HashMap(); return null === a;"));
        assertEquals(true, exec("HashMap a = new HashMap(); return null != a;"));
        assertEquals(true, exec("HashMap a = new HashMap(); return null !== a;"));
    }
}
