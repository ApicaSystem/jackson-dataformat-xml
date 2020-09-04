package com.fasterxml.jackson.dataformat.xml.failing;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.junit.Ignore;

@Ignore
public class Unwrapped374Test extends XmlTestBase
{
    @JacksonXmlRootElement(localName = "Root")
    @JsonRootName("Root")
    static class Root {
        public int id = 1;
    }

    private final XmlMapper MAPPER = mapperBuilder()
            .enable(SerializationFeature.WRAP_ROOT_VALUE)
            .enable(DeserializationFeature.UNWRAP_ROOT_VALUE)
            .build();

    public void testUnwrappedRoundTrip() throws Exception
    {
        String xml = MAPPER.writeValueAsString(new Root());
System.err.println("XML: "+xml);
//        assertEquals("<Root><id>hello</id></Root>", xml);
        Root result = MAPPER.readValue(xml, Root.class);
        assertNotNull(result);
    }
}
