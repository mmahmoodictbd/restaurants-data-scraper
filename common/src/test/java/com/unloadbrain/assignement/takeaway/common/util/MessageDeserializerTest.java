package com.unloadbrain.assignement.takeaway.common.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

public class MessageDeserializerTest {

    @Test
    public void shouldDeserializeByteArrayToObject() {

        // Given

        TestObjectMessageDeserializer deserializer = new TestObjectMessageDeserializer();

        byte[] data = "{\"key\":\"Hello!\"}".getBytes();

        // When
        TestObject message = deserializer.deserialize(any(String.class), data);

        // Then
        assertNotNull(message);
        assertEquals("Hello!", message.getKey());
    }


    public static class TestObjectMessageDeserializer extends MessageDeserializer<TestObject> {

        public TestObjectMessageDeserializer() {
            super(TestObject.class);
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestObject {

        private String key;
    }
}