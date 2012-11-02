/*
 * Copyright 2012 Hippo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hippoecm.frontend.editor.plugins.resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author obourgeois
 */
public class ResourceHelperTest {

    public ResourceHelperTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of handlePdfAndSetHippoTextProperty method, of class ResourceHelper.
     * CMS7-6516: Exceptions logged in terminal[...]
     */
    @Test
    public void testHandlePdf() {
        InputStream inputStream = null; 
        ByteArrayInputStream byteInputStream = null;
        String content = null;

        //Should not throw SaxParseException
        inputStream = getClass().getResourceAsStream("/test-tika.pdf");
        byteInputStream = null;
        try {
            content = extractContent(inputStream, byteInputStream, 10);
        } catch (TikaException ex) {
            fail();
        }
        assertEquals(10, content.length());


        inputStream = getClass().getResourceAsStream("/test-tika.pdf");
        byteInputStream = null;
        try {
            content = extractContent(inputStream, byteInputStream, -1);
        } catch (TikaException ex) {
            fail();
        }
        assertNotNull(content);
    }

    @Test
    public void testCorruptedPdf() {
        InputStream inputStream = null; 
        ByteArrayInputStream byteInputStream = null;
        String content = null;

        //This PDF doesn't open in Adobe Reader
        inputStream = getClass().getResourceAsStream("/corrupt_pdf_file_testen.pdf");
        byteInputStream = null;
        try {
            content = extractContent(inputStream, byteInputStream, -1);
        } catch (TikaException ex) {
            ex.printStackTrace();
            fail();
        }
        assertNotNull(content);
    }
    
    private String extractContent(InputStream inputStream, ByteArrayInputStream byteInputStream, int length) throws TikaException {
        String content = null;
        try {
            Tika tika = new Tika();
            tika.setMaxStringLength(length);
            Metadata metadata = new Metadata();
            content = tika.parseToString(inputStream, metadata);
            byteInputStream = new ByteArrayInputStream(content.getBytes());  
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        } finally {
            IOUtils.closeQuietly(byteInputStream);
            IOUtils.closeQuietly(inputStream);
            return content;
        }

    }
}
