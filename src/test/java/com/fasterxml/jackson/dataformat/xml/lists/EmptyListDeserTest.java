package com.fasterxml.jackson.dataformat.xml.lists;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.xml.annotation.*;

public class EmptyListDeserTest extends XmlTestBase
{
    // [dataformat-xml#177]
    static class Config
    {
        @JacksonXmlProperty(isAttribute=true)
        public String id;
        
        @JacksonXmlElementWrapper(useWrapping=false)
        public List<Entry> entry;
    }

    static class Entry
    {
        @JacksonXmlProperty(isAttribute=true)
        public String id;
    }

    // [dataformat-xml#319]
    static class Value319 {
        public Long orderId, orderTypeId;
    }    
    
    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */

    private final XmlMapper MAPPER = new XmlMapper();

    // [dataformat-xml#177]
    public void testEmptyList() throws Exception
    {
        Config r = MAPPER.readValue(
                "<Config id='123'>\n"+
                "  <entry id='foo'> </entry>\n"+
                "</Config>\n",
                Config.class);
        assertNotNull(r);
        assertEquals("123", r.id);
        assertNotNull(r.entry);
        assertEquals(1, r.entry.size());
        assertEquals("foo", r.entry.get(0).id);
    }

    // [dataformat-xml#319]
    public void testEmptyList319() throws Exception
    {
        final String DOC = "<orders></orders>";

        List<Value319> list = MAPPER.readValue(DOC, new TypeReference<List<Value319>>() { });
        assertNotNull(list);
        assertEquals(0, list.size());

        Value319[] array = MAPPER.readValue(DOC, Value319[].class);
        assertNotNull(array);
        assertEquals(0, array.length);
    }
}
