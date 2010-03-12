/*
 * SWFFontDecoder.java
 * Transform
 *
 * Copyright (c) 2009-2010 Flagstone Software Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of Flagstone Software Ltd. nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.flagstone.transform.util.font;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;

import com.flagstone.transform.CharacterEncoding;
import com.flagstone.transform.Movie;
import com.flagstone.transform.coder.MovieTag;
import com.flagstone.transform.datatype.Bounds;
import com.flagstone.transform.font.DefineFont;
import com.flagstone.transform.font.DefineFont2;
import com.flagstone.transform.font.FontInfo;
import com.flagstone.transform.font.FontInfo2;
import com.flagstone.transform.font.Kerning;
import com.flagstone.transform.shape.Shape;

/**
 * <p>
 * Font is used to add embedded fonts to a movie.
 * </p>
 *
 * <p>
 * Flash supports two types of font definition: embedded fonts where the Flash
 * file contains the glyphs that are drawn to represents the text characters and
 * device fonts where the font is provided by the Flash Player showing the
 * movie. Embedded fonts are preferred since the movie will always look the same
 * regardless of where it is played - if a Flash Player does not contain a
 * device font it will substitute it with another.
 * </p>
 *
 * <p>
 * Device fonts can be added to a movie by simply creating a DefineFont or
 * DefineFont2 object which contain the name of the font. An embedded font must
 * contain all the information to draw and layout the glyphs representing the
 * text to be displayed. The Font class hides all this detail and makes it easy
 * to add embedded fonts to a movie.
 * <p>
 *
 * <p>
 * The Font class can be used to create embedded fonts in three ways:
 * </p>
 *
 * <ol>
 * <li>Using TrueType or OpenType font definition stored in a file.</li>
 * <li>Using an existing font definition from a flash file.</li>
 * <li>Using a given Java AWT font as a template.</li>
 * </ol>
 *
 * <P>
 * For OpenType or TrueType fonts, files with the extensions ".otf" or ".ttf"
 * may be used. Files containing collections of fonts ".otc" are not currently
 * supported.
 * </p>
 *
 * <p>
 * Using an existing Flash font definition is the most interesting. Fonts can
 * initially be created using AWT Font objects or TrueType files and all the
 * visible characters included. If the generated Flash definition is saved to a
 * file it can easily and quickly be loaded. Indeed the overhead of parsing an
 * AWT or TrueType font is significant (sometimes several seconds) so creating
 * libraries of "pre-parsed" flash fonts is the preferred way of use fonts.
 * </p>
 */
//TODO(class)
public final class SWFFontDecoder implements FontProvider, FontDecoder {

    private Map<Integer,Font>fonts;
    
    public SWFFontDecoder() {
        fonts = new LinkedHashMap<Integer,Font>();
    }

    /** TODO(method). */
    public FontDecoder newDecoder() {
        return new SWFFontDecoder();
    }

    /** TODO(method). */
    public void read(final File file) throws IOException, DataFormatException {
        final FileInputStream stream = new FileInputStream(file);
        try {
            decode(stream);
        } finally {
            stream.close();
        }
    }

    /** TODO(method). */
    public void read(final URL url) throws IOException, DataFormatException {
        final URLConnection connection = url.openConnection();

        if (connection.getContentLength() < 0) {
            throw new FileNotFoundException(url.getFile());
        }

        final InputStream stream = connection.getInputStream();

        try {
            decode(stream);
        } finally {
            stream.close();
        }
    }

    /** TODO(method). */
    public List<Font> getFonts() {
        return new ArrayList<Font>(fonts.values());
    }

    private void decode(final InputStream stream) throws IOException, DataFormatException {
        final Movie movie = new Movie();
        movie.decodeFromStream(stream);

        fonts.clear();

        SWFFontDecoder decoder = new SWFFontDecoder();

        for (MovieTag obj : movie.getObjects()) {
            if (obj instanceof DefineFont2) {
                decoder.decode((DefineFont2) obj);
            }
        }
    }

    /**
     * Initialise this object with the information from a flash font definition.
     *
     * @param glyphs
     *            a DefineFont object which contains the definition of the
     *            glyphs.
     *
     * @param info
     *            a FontInfo object that contains information on the font name,
     *            weight, style and character codes.
     */
    public void decode(final DefineFont glyphs) {
        
        Font font = new Font();
        
        font.setAscent(0);
        font.setDescent(0);
        font.setLeading(0);
        
        int glyphCount = glyphs.getShapes().size();

        font.setMissingGlyph(0);
        font.setNumberOfGlyphs(glyphCount);
        font.setHighestChar((char)glyphCount);

        if (glyphCount > 0) {
            
            Shape shape;
             
            for (int i=0; i<glyphCount; i++) {
                shape = glyphs.getShapes().get(i);
                font.addGlyph((char)i, new Glyph(shape));
            }
        }
        
        fonts.put(glyphs.getIdentifier(), font);
    }

    /**
     * Initialise this object with the information from a flash font definition.
     *
     * @param glyphs
     *            a DefineFont object which contains the definition of the
     *            glyphs.
     *
     * @param info
     *            a FontInfo object that contains information on the font name,
     *            weight, style and character codes.
     */
    public void decode(final FontInfo info) {
        
        Font font = fonts.get(info.getIdentifier());
        
        font.setFace(new FontFace(info.getName(),
                info.isBold(), info.isItalic()));
        
        font.setEncoding(info.getEncoding());
        
        int glyphCount = font.getNumberOfGlyphs();
        int highest = info.getCodes().get(glyphCount);

        font.setHighestChar((char)highest);

        if (glyphCount > 0) {
            
            Glyph glyph;
            int code;
            
            for (int i=0; i<glyphCount; i++) {
                glyph = font.getGlyph(i);
                code = info.getCodes().get(i);
                
                font.addGlyph((char)code, glyph);
            }
        }
    }

    /**
     * Initialise this object with the information from a flash font definition.
     *
     * @param glyphs
     *            a DefineFont object which contains the definition of the
     *            glyphs.
     *
     * @param info
     *            a FontInfo2 object that contains information on the font name,
     *            weight, style and character codes.
     */
    public void decode(final FontInfo2 info) {
        
        Font font = fonts.get(info.getIdentifier());
        
        font.setFace(new FontFace(info.getName(),
                info.isBold(), info.isItalic()));
        
        font.setEncoding(info.getEncoding());
        
        int glyphCount = font.getNumberOfGlyphs();
        int highest = info.getCodes().get(glyphCount);

        font.setHighestChar((char)highest);

        if (glyphCount > 0) {
            
            Glyph glyph;
            int code;
            
            for (int i=0; i<glyphCount; i++) {
                glyph = font.getGlyph(i);
                code = info.getCodes().get(i);
                
                font.addGlyph((char)code, glyph);
            }
        }
    }

    /**
     * Initialise this object with the information from a flash font definition.
     *
     * @param object
     *            a DefineFont2 object that contains information on the font
     *            name, weight, style and character codes as well as the glyph
     *            definitions.
     */
    public void decode(final DefineFont2 object) {
        
        Font font = new Font();
        
        font.setFace(new FontFace(object.getName(),
                object.isBold(), object.isItalic()));
        
        font.setEncoding(object.getEncoding());
        font.setAscent(object.getAscent());
        font.setDescent(object.getDescent());
        font.setLeading(object.getLeading());
        
        int glyphCount = object.getShapes().size();
        int highest = object.getCodes().get(glyphCount);

        font.setMissingGlyph(0);
        font.setNumberOfGlyphs(glyphCount);
        font.setHighestChar((char)highest);

        if (glyphCount > 0) {
            
            Shape shape;
            Bounds bounds;
            int advance;
            int code;
            
            for (int i=0; i<glyphCount; i++) {
                shape = object.getShapes().get(i);
                
                if (object.getBounds() != null) {
                    bounds = object.getBounds().get(i);
                } else {
                    bounds = null;
                }
                if (object.getAdvances() != null) {
                    advance = object.getAdvances().get(i);
                } else {
                    advance = 0;
                }
                code = object.getCodes().get(i);
                
                font.addGlyph((char)code, new Glyph(shape, bounds, advance));
            }
        }
        
        fonts.put(object.getIdentifier(), font);
    }
}
