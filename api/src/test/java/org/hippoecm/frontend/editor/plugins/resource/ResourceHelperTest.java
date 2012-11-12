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

import java.io.IOException;
import java.io.InputStream;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.utils.ParseUtils;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author obourgeois
 */
public class ResourceHelperTest {

    @Test
    public void testHandlePdf() {
        try {
            InputStream inputStream = getClass().getResourceAsStream("/correct_pdf_file.pdf");
            String content = PdfParser.synchronizedParse(inputStream);
            assertNotNull(content);
            System.out.println(content);
            assertTrue(content.startsWith("Simple pdf for testing the word : foobarlux"));
        } catch (IllegalStateException ex) {
            fail();
        }
    }

    @Test(expected = TikaException.class)
    @Ignore
    public void testWithParseException() throws TikaException, IOException {
        InputStream inputStream = getClass().getResourceAsStream("/long_pdf_file.pdf");
        ParseUtils.getStringContent(inputStream, TikaConfig.getDefaultConfig(), ResourceHelper.MIME_TYPE_PDF);
    }
    
    @Test
    public void testCorruptedPdfDoesNotThrowException() {
        //This PDF doesn't open in Adobe Reader
        try {
            InputStream inputStream = getClass().getResourceAsStream("/broken_pdf_file.pdf");
            String content = PdfParser.synchronizedParse(inputStream);
            assertNotNull(content);
            assertTrue(content.startsWith("A-PDF"));
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
            fail();
        }
    }


}
