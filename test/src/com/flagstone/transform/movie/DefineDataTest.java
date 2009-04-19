/*
 * DefineDataTest.java
 * Transform
 *
 * Copyright (c) 2009 Flagstone Software Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of Flagstone Software Ltd. nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.flagstone.transform.movie;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import com.flagstone.transform.coder.CoderException;
import com.flagstone.transform.coder.SWFContext;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;

@SuppressWarnings( { 
	"PMD.LocalVariableCouldBeFinal",
	"PMD.JUnitAssertionsShouldIncludeMessage" 
})
public final class DefineDataTest {
	
	private transient final int identifier = 1;
	private transient final byte[] data = new byte[] {1,2,3,4};
	
	private transient DefineData fixture;
	
	private transient final byte[] empty = new byte[] { (byte)0xC6, 0x15, 0x00, 
			0x00, 0x00, 0x00, 0x00, 0x00};

	private transient final byte[] encoded = new byte[] { (byte)0xCA, 0x15, 0x01, 
			0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x02, 0x03, 0x04};
	
	private transient final byte[] extended = new byte[] { (byte)0xFF, 0x15, 0x0A,
			0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x02, 0x03, 0x04};

	@Test(expected=IllegalArgumentException.class)
	public void checkAccessorForIdentifierWithLowerBound() {
		fixture = new DefineData(0, data);
	}

	@Test(expected=IllegalArgumentException.class)
	public void checkAccessorForIdentifierWithUpperBound() {
		fixture = new DefineData(65536, data);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void checkAccessorForDataWithNull() {
		fixture = new DefineData(1, null);
	}
	
	@Test
	public void checkCopy() {
		fixture = new DefineData(identifier, data);
		DefineData copy = fixture.copy();

		assertNotSame(fixture, copy);
		assertEquals(fixture.getIdentifier(), copy.getIdentifier());
		assertNotSame(fixture.getData(), copy.getData());
		assertEquals(fixture.toString(), copy.toString());
	}
	
	@Test
	public void encode() throws CoderException {		
		SWFEncoder encoder = new SWFEncoder(encoded.length);		
		SWFContext context = new SWFContext();

		fixture = new DefineData(identifier, data);
		assertEquals(encoded.length, fixture.prepareToEncode(encoder, context));
		fixture.encode(encoder, context);
		
		assertTrue(encoder.eof());
		assertArrayEquals(encoded, encoder.getData());
	}
	
	@Test
	public void encodeExtended() throws CoderException {

		SWFEncoder encoder = new SWFEncoder(112);
		SWFContext context = new SWFContext();

		fixture = new DefineData(identifier, new byte[100]);
		assertEquals(112, fixture.prepareToEncode(encoder, context));		
		fixture.encode(encoder, context);
		
		assertTrue(encoder.eof());
	}
	
	@Test
	public void decode() throws CoderException {
		SWFDecoder decoder = new SWFDecoder(encoded);
		SWFContext context = new SWFContext();

		fixture = new DefineData(decoder, context);
		
		assertTrue(decoder.eof());
		assertEquals(identifier, fixture.getIdentifier());
		assertArrayEquals(data, fixture.getData());
	}
	
	@Test
	public void decodeExtended() throws CoderException {
		SWFDecoder decoder = new SWFDecoder(extended);
		SWFContext context = new SWFContext();

		fixture = new DefineData(decoder, context);
		
		assertTrue(decoder.eof());
		assertEquals(identifier, fixture.getIdentifier());
		assertArrayEquals(data, fixture.getData());
	}
}