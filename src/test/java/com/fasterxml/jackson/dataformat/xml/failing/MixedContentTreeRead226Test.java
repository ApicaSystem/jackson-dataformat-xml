package com.fasterxml.jackson.dataformat.xml.failing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import org.junit.Ignore;

@Ignore
public class MixedContentTreeRead226Test extends XmlTestBase
{
    private final ObjectMapper MAPPER = newMapper();

    public void testMixed226() throws Exception
    {
        final String XML = "<root>\n<a>lorem <b>ipsum</b> dolor</a>\n</root>";
        JsonNode root = MAPPER.readTree(XML);
        assertNotNull(root);
    }
}
