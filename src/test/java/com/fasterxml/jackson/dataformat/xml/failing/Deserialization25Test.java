package com.fasterxml.jackson.dataformat.xml.failing;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.junit.Ignore;

@Ignore
public class Deserialization25Test extends XmlTestBase
{
    private static class EmptyStrings
    {
        @JacksonXmlProperty(isAttribute=true)
        public String a = "NOT SET";
        public String b = "NOT SET";
    }

    private final XmlMapper MAPPER = new XmlMapper();

    // [dataformat-xml#25]
    public void testEmptyStringFromElemAndAttr() throws Exception
    {
        EmptyStrings ob = MAPPER.readValue("<EmptyString a=''><b /></EmptyString>",
                EmptyStrings.class);
        assertNotNull(ob);
        assertEquals("", ob.a);
        assertEquals("", ob.b);
    }
}
