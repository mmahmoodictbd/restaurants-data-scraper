package com.unloadbrain.assignement.takeaway.common.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;

public class MessageSerializerTest {

    @Test
    public void shouldSerializeObjectToByteArray() {

        // Given

        TestObjectMessageSerializer serializer = new TestObjectMessageSerializer();

        TestObject testObject = new TestObject("Hello!");

        // When
        String serializedObject = new String(serializer.serialize(any(String.class), testObject));

        // Then
        assertEquals("{\"key\":\"Hello!\"}", serializedObject);
    }

    static class TestObjectMessageSerializer extends MessageSerializer<TestObject> {

    }

    @Getter
    @Setter
    @AllArgsConstructor
    static class TestObject {

        private String key;
    }
}