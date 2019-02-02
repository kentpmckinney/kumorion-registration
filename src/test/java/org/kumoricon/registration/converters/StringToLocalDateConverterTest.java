package org.kumoricon.registration.converters;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class StringToLocalDateConverterTest {
    StringToLocalDateConverter converter = new StringToLocalDateConverter();
    private static final LocalDate CORRECT = LocalDate.of(1990, 12, 15);
    private static final LocalDate CORRECT2010 = LocalDate.of(2010, 1, 1);

    @Test
    public void convertHappyPath() {
        assertEquals(CORRECT, converter.convert("12-15-1990"));
        assertEquals(CORRECT, converter.convert("12/15/1990"));
        assertEquals(CORRECT, converter.convert("12/15/90"));
        assertEquals(CORRECT, converter.convert("121590"));
        assertEquals(CORRECT, converter.convert("12151990"));

        assertEquals(CORRECT2010, converter.convert("1-1-2010"));
        assertEquals(CORRECT2010, converter.convert("1/1/2010"));
        assertEquals(CORRECT2010, converter.convert("1/1/10"));
        assertEquals(CORRECT2010, converter.convert("010110"));
        assertEquals(CORRECT2010, converter.convert("01012010"));
    }

    @Test
    public void convertInvalidDateReturnsNull() {
        assertEquals(null, converter.convert("bob"));
        assertEquals(null, converter.convert(null));
        assertEquals(null, converter.convert("12/32/90"));
    }

}