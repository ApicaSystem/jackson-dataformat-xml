package com.fasterxml.jackson.dataformat.xml.failing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import org.junit.Ignore;

@Ignore //dups handling from databind 2.12 is required
public class JsonNodeMixedContent402Test extends XmlTestBase
{
    final private ObjectMapper XML_MAPPER = newMapper();

    final private ObjectMapper JSON_MAPPER = new JsonMapper();

    public void testMixedContentBefore() throws Exception
    {
        // First, before elements:
        assertEquals(JSON_MAPPER.readTree(a2q("{'':'before','a':1,'b':'2'}")),
                XML_MAPPER.readTree("<root>before<a>1</a><b>2</b></root>"));
    }

    public void testMixedContentBetween() throws Exception
    {
        // Second, between
        assertEquals(JSON_MAPPER.readTree(a2q("{'a':1,'':'between','b':'2'}")),
                XML_MAPPER.readTree("<root><a>1</a>between<b>2</b></root>"));
    }

    public void testMixedContentAfter() throws Exception
    {
        // and then after
        assertEquals(JSON_MAPPER.readTree(a2q("{'a':1,'b':'2','':'after'}")),
                XML_MAPPER.readTree("<root><a>1</a><b>2</b>after</root>"));
    }

    public void testMultipleMixedContent() throws Exception
    {
        // and then after
        assertEquals(JSON_MAPPER.readTree(a2q("{'':['first','second','third'],'a':1,'b':'2','':'after'}")),
                XML_MAPPER.readTree("<root>first<a>1</a>second<b>2</b>this</root>"));
    }
}
