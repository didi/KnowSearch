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

package org.elasticsearch.common.unit;

import org.elasticsearch.ElasticsearchParseException;
import org.elasticsearch.common.io.stream.Writeable.Reader;
import org.elasticsearch.test.AbstractWireSerializingTestCase;
import org.hamcrest.MatcherAssert;

import java.io.IOException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ByteSizeValueTests extends AbstractWireSerializingTestCase<ByteSizeValue> {
    public void testActualPeta() {
        MatcherAssert.assertThat(new ByteSizeValue(4, ByteSizeUnit.PB).getBytes(), equalTo(4503599627370496L));
    }

    public void testActualTera() {
        MatcherAssert.assertThat(new ByteSizeValue(4, ByteSizeUnit.TB).getBytes(), equalTo(4398046511104L));
    }

    public void testActual() {
        MatcherAssert.assertThat(new ByteSizeValue(4, ByteSizeUnit.GB).getBytes(), equalTo(4294967296L));
    }

    public void testSimple() {
        assertThat(ByteSizeUnit.BYTES.toBytes(10), is(new ByteSizeValue(10, ByteSizeUnit.BYTES).getBytes()));
        assertThat(ByteSizeUnit.KB.toKB(10), is(new ByteSizeValue(10, ByteSizeUnit.KB).getKb()));
        assertThat(ByteSizeUnit.MB.toMB(10), is(new ByteSizeValue(10, ByteSizeUnit.MB).getMb()));
        assertThat(ByteSizeUnit.GB.toGB(10), is(new ByteSizeValue(10, ByteSizeUnit.GB).getGb()));
        assertThat(ByteSizeUnit.TB.toTB(10), is(new ByteSizeValue(10, ByteSizeUnit.TB).getTb()));
        assertThat(ByteSizeUnit.PB.toPB(10), is(new ByteSizeValue(10, ByteSizeUnit.PB).getPb()));
    }

    public void testEquality() {
        String[] equalValues = new String[]{"1GB", "1024MB", "1048576KB", "1073741824B"};
        ByteSizeValue value1 = ByteSizeValue.parseBytesSizeValue(randomFrom(equalValues), "equalTest");
        ByteSizeValue value2 = ByteSizeValue.parseBytesSizeValue(randomFrom(equalValues), "equalTest");
        assertThat(value1, equalTo(value2));
    }

    public void testToString() {
        assertThat("10b", is(new ByteSizeValue(10, ByteSizeUnit.BYTES).toString()));
        assertThat("1.5kb", is(new ByteSizeValue((long) (1024 * 1.5), ByteSizeUnit.BYTES).toString()));
        assertThat("1.5mb", is(new ByteSizeValue((long) (1024 * 1.5), ByteSizeUnit.KB).toString()));
        assertThat("1.5gb", is(new ByteSizeValue((long) (1024 * 1.5), ByteSizeUnit.MB).toString()));
        assertThat("1.5tb", is(new ByteSizeValue((long) (1024 * 1.5), ByteSizeUnit.GB).toString()));
        assertThat("1.5pb", is(new ByteSizeValue((long) (1024 * 1.5), ByteSizeUnit.TB).toString()));
        assertThat("1536pb", is(new ByteSizeValue((long) (1024 * 1.5), ByteSizeUnit.PB).toString()));
    }

    public void testParsing() {
        assertThat(ByteSizeValue.parseBytesSizeValue("42PB", "testParsing").toString(), is("42pb"));
        assertThat(ByteSizeValue.parseBytesSizeValue("42 PB", "testParsing").toString(), is("42pb"));
        assertThat(ByteSizeValue.parseBytesSizeValue("42pb", "testParsing").toString(), is("42pb"));
        assertThat(ByteSizeValue.parseBytesSizeValue("42 pb", "testParsing").toString(), is("42pb"));

        assertThat(ByteSizeValue.parseBytesSizeValue("42P", "testParsing").toString(), is("42pb"));
        assertThat(ByteSizeValue.parseBytesSizeValue("42 P", "testParsing").toString(), is("42pb"));
        assertThat(ByteSizeValue.parseBytesSizeValue("42p", "testParsing").toString(), is("42pb"));
        assertThat(ByteSizeValue.parseBytesSizeValue("42 p", "testParsing").toString(), is("42pb"));

        assertThat(ByteSizeValue.parseBytesSizeValue("54TB", "testParsing").toString(), is("54tb"));
        assertThat(ByteSizeValue.parseBytesSizeValue("54 TB", "testParsing").toString(), is("54tb"));
        assertThat(ByteSizeValue.parseBytesSizeValue("54tb", "testParsing").toString(), is("54tb"));
        assertThat(ByteSizeValue.parseBytesSizeValue("54 tb", "testParsing").toString(), is("54tb"));

        assertThat(ByteSizeValue.parseBytesSizeValue("54T", "testParsing").toString(), is("54tb"));
        assertThat(ByteSizeValue.parseBytesSizeValue("54 T", "testParsing").toString(), is("54tb"));
        assertThat(ByteSizeValue.parseBytesSizeValue("54t", "testParsing").toString(), is("54tb"));
        assertThat(ByteSizeValue.parseBytesSizeValue("54 t", "testParsing").toString(), is("54tb"));

        assertThat(ByteSizeValue.parseBytesSizeValue("12GB", "testParsing").toString(), is("12gb"));
        assertThat(ByteSizeValue.parseBytesSizeValue("12 GB", "testParsing").toString(), is("12gb"));
        assertThat(ByteSizeValue.parseBytesSizeValue("12gb", "testParsing").toString(), is("12gb"));
        assertThat(ByteSizeValue.parseBytesSizeValue("12 gb", "testParsing").toString(), is("12gb"));

        assertThat(ByteSizeValue.parseBytesSizeValue("12G", "testParsing").toString(), is("12gb"));
        assertThat(ByteSizeValue.parseBytesSizeValue("12 G", "testParsing").toString(), is("12gb"));
        assertThat(ByteSizeValue.parseBytesSizeValue("12g", "testParsing").toString(), is("12gb"));
        assertThat(ByteSizeValue.parseBytesSizeValue("12 g", "testParsing").toString(), is("12gb"));

        assertThat(ByteSizeValue.parseBytesSizeValue("12M", "testParsing").toString(), is("12mb"));
        assertThat(ByteSizeValue.parseBytesSizeValue("12 M", "testParsing").toString(), is("12mb"));
        assertThat(ByteSizeValue.parseBytesSizeValue("12m", "testParsing").toString(), is("12mb"));
        assertThat(ByteSizeValue.parseBytesSizeValue("12 m", "testParsing").toString(), is("12mb"));

        assertThat(ByteSizeValue.parseBytesSizeValue("23KB", "testParsing").toString(), is("23kb"));
        assertThat(ByteSizeValue.parseBytesSizeValue("23 KB", "testParsing").toString(), is("23kb"));
        assertThat(ByteSizeValue.parseBytesSizeValue("23kb", "testParsing").toString(), is("23kb"));
        assertThat(ByteSizeValue.parseBytesSizeValue("23 kb", "testParsing").toString(), is("23kb"));

        assertThat(ByteSizeValue.parseBytesSizeValue("23K", "testParsing").toString(), is("23kb"));
        assertThat(ByteSizeValue.parseBytesSizeValue("23 K", "testParsing").toString(), is("23kb"));
        assertThat(ByteSizeValue.parseBytesSizeValue("23k", "testParsing").toString(), is("23kb"));
        assertThat(ByteSizeValue.parseBytesSizeValue("23 k", "testParsing").toString(), is("23kb"));

        assertThat(ByteSizeValue.parseBytesSizeValue("1B", "testParsing").toString(), is("1b"));
        assertThat(ByteSizeValue.parseBytesSizeValue("1 B", "testParsing").toString(), is("1b"));
        assertThat(ByteSizeValue.parseBytesSizeValue("1b", "testParsing").toString(), is("1b"));
        assertThat(ByteSizeValue.parseBytesSizeValue("1 b", "testParsing").toString(), is("1b"));
    }

    public void testFailOnMissingUnits() {
        Exception e = expectThrows(ElasticsearchParseException.class, () -> ByteSizeValue.parseBytesSizeValue("23", "test"));
        assertThat(e.getMessage(), containsString("failed to parse setting [test]"));
    }

    public void testFailOnUnknownUnits() {
        Exception e = expectThrows(ElasticsearchParseException.class, () -> ByteSizeValue.parseBytesSizeValue("23jw", "test"));
        assertThat(e.getMessage(), containsString("failed to parse setting [test]"));
    }

    public void testFailOnEmptyParsing() {
        Exception e = expectThrows(ElasticsearchParseException.class,
                () -> assertThat(ByteSizeValue.parseBytesSizeValue("", "emptyParsing").toString(), is("23kb")));
        assertThat(e.getMessage(), containsString("failed to parse setting [emptyParsing]"));
    }

    public void testFailOnEmptyNumberParsing() {
        Exception e = expectThrows(ElasticsearchParseException.class,
                () -> assertThat(ByteSizeValue.parseBytesSizeValue("g", "emptyNumberParsing").toString(), is("23b")));
        assertThat(e.getMessage(), containsString("failed to parse [g]"));
    }

    public void testNoDotsAllowed() {
        Exception e = expectThrows(ElasticsearchParseException.class, () -> ByteSizeValue.parseBytesSizeValue("42b.", null, "test"));
        assertThat(e.getMessage(), containsString("failed to parse setting [test]"));
    }

    public void testCompareEquality() {
        ByteSizeUnit randomUnit = randomFrom(ByteSizeUnit.values());
        long firstRandom = randomNonNegativeLong() / randomUnit.toBytes(1);
        ByteSizeValue firstByteValue = new ByteSizeValue(firstRandom, randomUnit);
        ByteSizeValue secondByteValue = new ByteSizeValue(firstRandom, randomUnit);
        assertEquals(0, firstByteValue.compareTo(secondByteValue));
    }

    public void testCompareValue() {
        ByteSizeUnit unit = randomFrom(ByteSizeUnit.values());
        long firstRandom = randomNonNegativeLong() / unit.toBytes(1);
        long secondRandom = randomValueOtherThan(firstRandom, () -> randomNonNegativeLong() / unit.toBytes(1));
        ByteSizeValue firstByteValue = new ByteSizeValue(firstRandom, unit);
        ByteSizeValue secondByteValue = new ByteSizeValue(secondRandom, unit);
        assertEquals(firstRandom > secondRandom, firstByteValue.compareTo(secondByteValue) > 0);
        assertEquals(secondRandom > firstRandom, secondByteValue.compareTo(firstByteValue) > 0);
    }

    public void testCompareUnits() {
        long number = randomLongBetween(1, Long.MAX_VALUE/ ByteSizeUnit.PB.toBytes(1));
        ByteSizeUnit randomUnit = randomValueOtherThan(ByteSizeUnit.PB, ()->randomFrom(ByteSizeUnit.values()));
        ByteSizeValue firstByteValue = new ByteSizeValue(number, randomUnit);
        ByteSizeValue secondByteValue = new ByteSizeValue(number, ByteSizeUnit.PB);
        assertTrue(firstByteValue.compareTo(secondByteValue) < 0);
        assertTrue(secondByteValue.compareTo(firstByteValue) > 0);
    }

    public void testOutOfRange() {
        // Make sure a value of > Long.MAX_VALUE bytes throws an exception
        ByteSizeUnit unit = randomValueOtherThan(ByteSizeUnit.BYTES, () -> randomFrom(ByteSizeUnit.values()));
        long size = (long) randomDouble() * unit.toBytes(1) + (Long.MAX_VALUE - unit.toBytes(1));
        IllegalArgumentException exception = expectThrows(IllegalArgumentException.class, () -> new ByteSizeValue(size, unit));
        assertEquals("Values greater than " + Long.MAX_VALUE + " bytes are not supported: " + size + unit.getSuffix(),
                exception.getMessage());

        // Make sure for units other than BYTES a size of -1 throws an exception
        ByteSizeUnit unit2 = randomValueOtherThan(ByteSizeUnit.BYTES, () -> randomFrom(ByteSizeUnit.values()));
        long size2 = -1L;
        exception = expectThrows(IllegalArgumentException.class, () -> new ByteSizeValue(size2, unit2));
        assertEquals("Values less than -1 bytes are not supported: " + size2 + unit2.getSuffix(), exception.getMessage());

        // Make sure for any unit a size < -1 throws an exception
        ByteSizeUnit unit3 = randomFrom(ByteSizeUnit.values());
        long size3 = -1L * randomNonNegativeLong() - 1L;
        exception = expectThrows(IllegalArgumentException.class, () -> new ByteSizeValue(size3, unit3));
        assertEquals("Values less than -1 bytes are not supported: " + size3 + unit3.getSuffix(), exception.getMessage());
    }

    public void testConversionHashCode() {
        ByteSizeValue firstValue = new ByteSizeValue(randomIntBetween(0, Integer.MAX_VALUE), ByteSizeUnit.GB);
        ByteSizeValue secondValue = new ByteSizeValue(firstValue.getBytes(), ByteSizeUnit.BYTES);
        assertEquals(firstValue.hashCode(), secondValue.hashCode());
    }

    @Override
    protected ByteSizeValue createTestInstance() {
        if (randomBoolean()) {
            ByteSizeUnit unit = randomFrom(ByteSizeUnit.values());
            long size = randomNonNegativeLong() / unit.toBytes(1);
            if (size > Long.MAX_VALUE / unit.toBytes(1)) {
                throw new AssertionError();
            }
            return new ByteSizeValue(size, unit);
        } else {
            return new ByteSizeValue(randomNonNegativeLong());
        }
    }

    @Override
    protected Reader<ByteSizeValue> instanceReader() {
        return ByteSizeValue::new;
    }

    @Override
    protected ByteSizeValue mutateInstance(final ByteSizeValue instance) {
        final long instanceSize = instance.getSize();
        final ByteSizeUnit instanceUnit = instance.getUnit();
        final long mutateSize;
        final ByteSizeUnit mutateUnit;
        switch (between(0, 1)) {
        case 0:
            final long unitBytes = instanceUnit.toBytes(1);
            mutateSize = randomValueOtherThan(instanceSize, () -> randomNonNegativeLong() / unitBytes);
            mutateUnit = instanceUnit;
            break;
        case 1:
            mutateUnit = randomValueOtherThan(instanceUnit, () -> randomFrom(ByteSizeUnit.values()));
            final long newUnitBytes = mutateUnit.toBytes(1);
            /*
             * If size is zero we can not reuse zero because zero with any unit will be equal to zero with any other unit so in this case we
             * need to randomize a new size. Additionally, if the size unit pair is such that the representation would be such that the
             * number of represented bytes would exceed Long.Max_VALUE, we have to randomize a new size too.
             */
            if (instanceSize == 0 || instanceSize >= Long.MAX_VALUE / newUnitBytes) {
                mutateSize = randomValueOtherThanMany(
                        v -> v == instanceSize && v >= Long.MAX_VALUE / newUnitBytes, () -> randomNonNegativeLong() / newUnitBytes);
            } else {
                mutateSize = instanceSize;
            }
            break;
        default:
            throw new AssertionError("Invalid randomisation branch");
        }
        return new ByteSizeValue(mutateSize, mutateUnit);
    }

    public void testParse() {
        for (int i = 0; i < NUMBER_OF_TEST_RUNS; i++) {
            ByteSizeValue original = createTestInstance();
            String serialised = original.getStringRep();
            ByteSizeValue copy = ByteSizeValue.parseBytesSizeValue(serialised, "test");
            assertEquals(original, copy);
            assertEquals(serialised, copy.getStringRep());
        }
    }

    public void testParseInvalidValue() {
        ElasticsearchParseException exception = expectThrows(ElasticsearchParseException.class,
                () -> ByteSizeValue.parseBytesSizeValue("-6mb", "test_setting"));
        assertEquals("failed to parse setting [test_setting] with value [-6mb] as a size in bytes", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
    }

    public void testParseDefaultValue() {
        ByteSizeValue defaultValue = createTestInstance();
        assertEquals(defaultValue, ByteSizeValue.parseBytesSizeValue(null, defaultValue, "test"));
    }

    public void testParseSpecialValues() throws IOException {
        ByteSizeValue instance = new ByteSizeValue(-1);
        assertEquals(instance, ByteSizeValue.parseBytesSizeValue(instance.getStringRep(), null, "test"));
        assertSerialization(instance);

        instance = new ByteSizeValue(0);
        assertEquals(instance, ByteSizeValue.parseBytesSizeValue(instance.getStringRep(), null, "test"));
        assertSerialization(instance);
    }

    public void testParseInvalidNumber() throws IOException {
        ElasticsearchParseException exception = expectThrows(ElasticsearchParseException.class,
                () -> ByteSizeValue.parseBytesSizeValue("notANumber", "test"));
        assertEquals("failed to parse setting [test] with value [notANumber] as a size in bytes: unit is missing or unrecognized",
                exception.getMessage());

        exception = expectThrows(ElasticsearchParseException.class, () -> ByteSizeValue.parseBytesSizeValue("notANumberMB", "test"));
        assertEquals("failed to parse [notANumberMB]", exception.getMessage());
    }

    public void testParseFractionalNumber() throws IOException {
        ByteSizeUnit unit = randomValueOtherThan(ByteSizeUnit.BYTES, () -> randomFrom(ByteSizeUnit.values()));
        String fractionalValue = "23.5" + unit.getSuffix();
        ByteSizeValue instance = ByteSizeValue.parseBytesSizeValue(fractionalValue, "test");
        assertEquals(fractionalValue, instance.toString());
        assertWarnings("Fractional bytes values are deprecated. Use non-fractional bytes values instead: [" + fractionalValue
                + "] found for setting [test]");
    }

    public void testGetBytesAsInt() {
        for (int i = 0; i < NUMBER_OF_TEST_RUNS; i++) {
            ByteSizeValue instance = new ByteSizeValue(randomIntBetween(1, 1000), randomFrom(ByteSizeUnit.values()));
            long bytesValue = instance.getBytes();
            if (bytesValue > Integer.MAX_VALUE) {
                IllegalArgumentException exception = expectThrows(IllegalArgumentException.class, () -> instance.bytesAsInt());
                assertEquals("size [" + instance.toString() + "] is bigger than max int", exception.getMessage());
            } else {
                assertEquals((int) bytesValue, instance.bytesAsInt());
            }
        }
    }
}
