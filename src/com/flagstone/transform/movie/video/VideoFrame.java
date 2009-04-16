/*
 * VideoFrame.java
 * Transform
 * 
 * Copyright (c) 2001-2008 Flagstone Software Ltd. All rights reserved.
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

package com.flagstone.transform.movie.video;

import java.util.Arrays;

import com.flagstone.transform.coder.CoderException;
import com.flagstone.transform.coder.SWFContext;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.movie.MovieTag;
import com.flagstone.transform.movie.Strings;
import com.flagstone.transform.movie.Types;


/**
 * VideoFrame contains the video data displayed in a single frame of a Flash
 * movie (.swf).
 * 
 * <p>Each frame of video is displayed whenever display list is updated using the
 * ShowFrame object - any timing information stored within the video data is
 * ignored. Since the video is updated at the same time as the display list the
 * frame rate of the video may be the same or less than the frame rate of the
 * Flash movie but not higher.</p>
 * 
 * @see DefineVideo
 */
public final class VideoFrame implements MovieTag
{
	private static final String FORMAT = "VideoFrame: { identifier=%d; frameNumber=%d; data=%d }";
	
	protected int identifier;
	protected int frameNumber;
	protected byte[] data;
	
	private transient int length;

	public VideoFrame(final SWFDecoder coder, final SWFContext context) throws CoderException
	{
		length = coder.readWord(2, false) & 0x3F;
		
		if (length == 0x3F) {
			length = coder.readWord(4, false);
		}

		identifier = coder.readWord(2, false);
		frameNumber = coder.readWord(2, false);
		data = coder.readBytes(new byte[length-4]);
	}

	/**
	 * Constructs a new VideoFrame object which will display the specified
	 * frame of video data in the DefineVideo object that matches the
	 * identifier.
	 * 
	 * @param uid
	 *            the unique identifier of the DefineVideo object. Must be in
	 *            the range 1..65535.
	 * @param frameNumber
	 *            the number of the frame. Must be in the range 1..65535.
	 * @param data
	 *            the encoded video data. For Flash 6 this is encoded in the
	 *            H263 format. In Flash 7 H263 and ScreenVideo is supported.
	 */
	public VideoFrame(int uid, int frameNumber, byte[] data)
	{
		setIdentifier(uid);
		setFrameNumber(frameNumber);
		setData(data);
	}

	public VideoFrame(VideoFrame object)
	{
		identifier = object.identifier;
		frameNumber = object.frameNumber;
		data = Arrays.copyOf(object.data, object.data.length);
	}

	/**
	 * Get the identifier of the DefineVideo object where the frame will be
	 * displayed.
	 */
	public int getIdentifier()
	{
		return identifier;
	}

	/**
	 * Sets the identifier of the DefineVideo object where the frame will be
	 * displayed.
	 * 
	 * @param uid
	 *            the unique identifier of the DefineVideo object. Must be in
	 *            the range 1..65535.
	 */
	public void setIdentifier(int uid)
	{
		if (uid < 1 || uid > 65535) {
			throw new IllegalArgumentException(Strings.IDENTIFIER_OUT_OF_RANGE);
		}
		identifier = uid;
	}

	/**
	 * Returns the number of the frame.
	 */
	public int getFrameNumber()
	{
		return frameNumber;
	}

	/**
	 * Sets the number of the frame.
	 * 
	 * @param number
	 *            the frame number. Must be in the range 1..65535.
	 */
	public void setFrameNumber(int number)
	{
		if (number < 1 || number > 65535) {
			throw new IllegalArgumentException(Strings.FRAME_OUT_OF_RANGE);
		}
		frameNumber = number;
	}

	/**
	 * Returns the encoded video data. In Flash 6 modified H263 encoded video is
	 * supported. Flash 7 supports both modified H263 and ScreenVideo.
	 */
	public byte[] getData()
	{
		return data;
	}

	/**
	 * Sets the encoded video data. In Flash 6 modified H263 encoded video is
	 * supported. Flash 7 supports both modified H263 and ScreenVideo,
	 * 
	 * @param data
	 *            the encoded video data. Must not be null.
	 */
	public void setData(byte[] data)
	{
		if (data == null) {
			throw new IllegalArgumentException(Strings.DATA_CANNOT_BE_NULL);
		}
		this.data = data;
	}

	/**
	 * Creates and returns a deep copy of this object.
	 */
	public VideoFrame copy() 
	{
		return new VideoFrame(this);
	}

	@Override
	public String toString()
	{
		return String.format(FORMAT, identifier, frameNumber, data.length);
	}

	public int prepareToEncode(final SWFEncoder coder, final SWFContext context)
	{
		length = 4 + data.length;

		return (length > 62 ? 6:2) + length;
	}

	public void encode(final SWFEncoder coder, final SWFContext context) throws CoderException
	{
		if (length >= 63) {
			coder.writeWord((Types.VIDEO_FRAME << 6) | 0x3F, 2);
			coder.writeWord(length, 4);
		} else {
			coder.writeWord((Types.VIDEO_FRAME << 6) | length, 2);
		}
		
		coder.writeWord(identifier, 2);
		coder.writeWord(frameNumber, 2);
		coder.writeBytes(data);
	}
}