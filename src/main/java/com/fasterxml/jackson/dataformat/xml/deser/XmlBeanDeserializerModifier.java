package com.fasterxml.jackson.dataformat.xml.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.deser.std.CollectionDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.dataformat.xml.util.AnnotationUtil;

/**
 * The main reason for a modifier is to support handling of
 * 'wrapped' Collection types.
 */
public class XmlBeanDeserializerModifier
    extends BeanDeserializerModifier
    implements java.io.Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * Virtual name used for text segments.
     */
    protected String _cfgNameForTextValue = "";

    public XmlBeanDeserializerModifier(String nameForTextValue)
    {
        _cfgNameForTextValue = nameForTextValue;
    }
    
    @Override
    public List<BeanPropertyDefinition> updateProperties(DeserializationConfig config,
            BeanDescription beanDesc, List<BeanPropertyDefinition> propDefs)
    {
        final AnnotationIntrospector intr = config.getAnnotationIntrospector();
        int changed = 0;
        
        for (int i = 0, propCount = propDefs.size(); i < propCount; ++i) {
            BeanPropertyDefinition prop = propDefs.get(i);
            AnnotatedMember acc = prop.getPrimaryMember();
            // should not be null, but just in case:
            if (acc == null) {
                continue;
            }
            /* First: handle "as text"? Such properties
             * are exposed as values of 'unnamed' fields; so one way to
             * map them is to rename property to have name ""... (and
             * hope this does not break other parts...)
             */
            Boolean b = AnnotationUtil.findIsTextAnnotation(intr, acc);
            if (b != null && b.booleanValue()) {
                // unwrapped properties will appear as 'unnamed' (empty String)
                BeanPropertyDefinition newProp = prop.withSimpleName(_cfgNameForTextValue);
                if (newProp != prop) {
                    propDefs.set(i, newProp);
                }
                continue;
            }
            // second: do we need to handle wrapping (for Lists)?
            PropertyName wrapperName = prop.getWrapperName();
            
            if (wrapperName != null && wrapperName != PropertyName.NO_NAME) {
                String localName = wrapperName.getSimpleName();
                if ((localName != null && localName.length() > 0)
                        && !localName.equals(prop.getName())) {
                    // make copy-on-write as necessary
                    if (changed == 0) {
                        propDefs = new ArrayList<BeanPropertyDefinition>(propDefs);
                    }
                    ++changed;
                    propDefs.set(i, prop.withSimpleName(localName));
                    continue;
                }
                // otherwise unwrapped; needs handling but later on
            }
        }
        return propDefs;
    }

    @Override
    public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
            BeanDescription beanDesc, JsonDeserializer<?> deser0)
    {
        if (!(deser0 instanceof BeanDeserializerBase)) {
            return deser0;
        }
        /* 17-Aug-2013, tatu: One important special case first: if we have one "XML Text"
         * property, it may be exposed as VALUE_STRING token (depending on whether any attribute
         * values are exposed): and to deserialize from that, we need special handling unless POJO
         * has appropriate single-string creator method.
         */
        BeanDeserializerBase deser = (BeanDeserializerBase) deser0;

        // Heuristics are bit tricky; but for now let's assume that if POJO
        // can already work with VALUE_STRING, it's ok and doesn't need extra support
        ValueInstantiator inst = deser.getValueInstantiator();
        // 03-Aug-2017, tatu: [dataformat-xml#254] suggests we also should
        //    allow passing `int`/`Integer`/`long`/`Long` cases, BUT
        //    unfortunately we can not simple use default handling. Would need
        //    coercion.
        if (!inst.canCreateFromString()) {
            SettableBeanProperty textProp = _findSoleTextProp(config, deser.properties());
            if (textProp != null) {
                return new XmlTextDeserializer(deser, textProp);
            }
        }
        return new WrapperHandlingDeserializer(deser);
    }

    private SettableBeanProperty _findSoleTextProp(DeserializationConfig config,
            Iterator<SettableBeanProperty> propIt)
    {
        final AnnotationIntrospector ai = config.getAnnotationIntrospector();
        SettableBeanProperty textProp = null;
        while (propIt.hasNext()) {
            SettableBeanProperty prop = propIt.next();
            AnnotatedMember m = prop.getMember();
            if (m != null) {
                // Ok, let's use a simple check: we should have renamed it earlier so:
                PropertyName n = prop.getFullName();
                if (_cfgNameForTextValue.equals(n.getSimpleName())) {
                    // should we verify we only got one?
                    textProp = prop;
                    continue;
                }
                // as-attribute are ok as well
                Boolean b = AnnotationUtil.findIsAttributeAnnotation(ai, m);
                if (b != null && b.booleanValue()) {
                    continue;
                }
            }
            // Otherwise, it's something else; no go
            return null;
        }
        return textProp;
    }

    private static class WhitespaceCollectionDeserHelper extends CollectionDeserializer {

        public WhitespaceCollectionDeserHelper(CollectionDeserializer src) {
            super(src);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Collection<Object> deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {

            JsonToken currentToken = jp.getCurrentToken();
            if (currentToken == JsonToken.VALUE_STRING && jp.getText().matches("^[\\r\\n\\t ]+$")) {
                return (Collection<Object>) _valueInstantiator.createUsingDefault(ctxt);
            }
            return super.deserialize(jp, ctxt);
        }

        @Override
        public CollectionDeserializer createContextual(DeserializationContext ctxt,
            BeanProperty property) throws JsonMappingException {
            return new WhitespaceCollectionDeserHelper(super.createContextual(ctxt, property));
        }
    }


    @Override
    public JsonDeserializer<?> modifyCollectionDeserializer(DeserializationConfig config,
        CollectionType type, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
        if (deserializer instanceof CollectionDeserializer) {
            return new WhitespaceCollectionDeserHelper((CollectionDeserializer) deserializer);
        } else {
            return super.modifyCollectionDeserializer(config, type, beanDesc,
                deserializer);
        }
    }
}
