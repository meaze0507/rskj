package org.ethereum.rpc.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.googlecode.jsonrpc4j.ErrorResolver.JsonError;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Nazaret García on 26/05/2021
 */

public class RskErrorResolverTest {

    private RskErrorResolver rskErrorResolver;

    @Before
    public void setup() {
        rskErrorResolver = new RskErrorResolver();
    }

    @Test
    public void test_resolveError_givenRskJsonRpcRequestException_returnsJsonErrorAsExpected() throws NoSuchMethodException {
        // Given
        Integer code = 1;
        String message = "message";
        RskJsonRpcRequestException exception = new RskJsonRpcRequestException(code, message);

        Method methodMock = this.getClass().getMethod("mockMethod");
        List<JsonNode> jsonNodeListMock = new ArrayList<>();

        // When
        JsonError result = rskErrorResolver.resolveError(exception, methodMock, jsonNodeListMock);

        // Then
        Assert.assertNotNull(result);
        Assert.assertEquals(code, (Integer) result.code);
        Assert.assertEquals(message, result.message);
        Assert.assertNull(result.data);
    }

    @Test
    public void test_resolveError_givenInvalidFormatException_returnsJsonErrorAsExpected() throws NoSuchMethodException {
        // Given
        InvalidFormatException exception = mock(InvalidFormatException.class);

        Method methodMock = this.getClass().getMethod("mockMethod");
        List<JsonNode> jsonNodeListMock = new ArrayList<>();

        // When
        JsonError result = rskErrorResolver.resolveError(exception, methodMock, jsonNodeListMock);

        // Then
        Assert.assertNotNull(result);
        Assert.assertEquals(-32603, result.code);
        Assert.assertEquals("Internal server error, probably due to invalid parameter type", result.message);
        Assert.assertNull(result.data);
    }

    @Test
    public void test_resolveError_givenUnrecognizedPropertyException_nullPropertyName_returnsJsonErrorWithDefaultMessageAsExpected() throws NoSuchMethodException {
        // Given
        UnrecognizedPropertyException exception = mock(UnrecognizedPropertyException.class);
        when(exception.getPropertyName()).thenReturn(null);
        when(exception.getKnownPropertyIds()).thenReturn(new ArrayList<>());

        Method methodMock = this.getClass().getMethod("mockMethod");
        List<JsonNode> jsonNodeListMock = new ArrayList<>();

        // When
        JsonError result = rskErrorResolver.resolveError(exception, methodMock, jsonNodeListMock);

        // Then
        Assert.assertNotNull(result);
        Assert.assertEquals(-32602, result.code);
        Assert.assertEquals("Invalid parameters", result.message);
        Assert.assertNull(result.data);
    }

    @Test
    public void test_resolveError_givenUnrecognizedPropertyException_nullKnownPropertyIds_returnsJsonErrorWithDefaultMessageAsExpected() throws NoSuchMethodException {
        // Given
        UnrecognizedPropertyException exception = mock(UnrecognizedPropertyException.class);
        when(exception.getPropertyName()).thenReturn("propertyName");
        when(exception.getKnownPropertyIds()).thenReturn(null);

        Method methodMock = this.getClass().getMethod("mockMethod");
        List<JsonNode> jsonNodeListMock = new ArrayList<>();

        // When
        JsonError result = rskErrorResolver.resolveError(exception, methodMock, jsonNodeListMock);

        // Then
        Assert.assertNotNull(result);
        Assert.assertEquals(-32602, result.code);
        Assert.assertEquals("Invalid parameters", result.message);
        Assert.assertNull(result.data);
    }

    @Test
    public void test_resolveError_givenUnrecognizedPropertyException_nullPropertyNameAndNullKnownPropertyIds_returnsJsonErrorWithDefaultMessageAsExpected() throws NoSuchMethodException {
        // Given
        UnrecognizedPropertyException exception = mock(UnrecognizedPropertyException.class);
        when(exception.getPropertyName()).thenReturn(null);
        when(exception.getKnownPropertyIds()).thenReturn(null);

        Method methodMock = this.getClass().getMethod("mockMethod");
        List<JsonNode> jsonNodeListMock = new ArrayList<>();

        // When
        JsonError result = rskErrorResolver.resolveError(exception, methodMock, jsonNodeListMock);

        // Then
        Assert.assertNotNull(result);
        Assert.assertEquals(-32602, result.code);
        Assert.assertEquals("Invalid parameters", result.message);
        Assert.assertNull(result.data);
    }

    @Test
    public void test_resolveError_givenUnrecognizedPropertyException_returnsJsonErrorWithDescriptiveMessageAsExpected() throws NoSuchMethodException {
        // Given
        String propertyName = "propertyName";

        String propertyId1 = "propertyId.1";
        String propertyId2 = "propertyId.2";
        List<Object> knownPropertyIds = new ArrayList<>();
        knownPropertyIds.add(propertyId1);
        knownPropertyIds.add(propertyId2);

        UnrecognizedPropertyException exception = mock(UnrecognizedPropertyException.class);
        when(exception.getPropertyName()).thenReturn(propertyName);
        when(exception.getKnownPropertyIds()).thenReturn(knownPropertyIds);

        Method methodMock = this.getClass().getMethod("mockMethod");
        List<JsonNode> jsonNodeListMock = new ArrayList<>();

        // When
        JsonError result = rskErrorResolver.resolveError(exception, methodMock, jsonNodeListMock);

        // Then
        Assert.assertNotNull(result);
        Assert.assertEquals(-32602, result.code);
        Assert.assertEquals("Unrecognized field \"propertyName\" (2 known properties: [\"propertyId.1\", \"propertyId.2\"])", result.message);
        Assert.assertNull(result.data);
    }

    @Test
    public void test_resolveError_givenUnrecognizedPropertyException_withZeroKnownProperties_returnsJsonErrorWithDescriptiveMessageAsExpected() throws NoSuchMethodException {
        // Given
        String propertyName = "propertyName";

        UnrecognizedPropertyException exception = mock(UnrecognizedPropertyException.class);
        when(exception.getPropertyName()).thenReturn(propertyName);
        when(exception.getKnownPropertyIds()).thenReturn(new ArrayList<>());

        Method methodMock = this.getClass().getMethod("mockMethod");
        List<JsonNode> jsonNodeListMock = new ArrayList<>();

        // When
        JsonError result = rskErrorResolver.resolveError(exception, methodMock, jsonNodeListMock);

        // Then
        Assert.assertNotNull(result);
        Assert.assertEquals(-32602, result.code);
        Assert.assertEquals("Unrecognized field \"propertyName\" (0 known properties: [])", result.message);
        Assert.assertNull(result.data);
    }

    @Test
    public void test_resolveError_givenGenericException_returnsJsonErrorWithDefaultMessageAsExpected() throws NoSuchMethodException {
        // Given
        Exception exception = mock(Exception.class);

        Method methodMock = this.getClass().getMethod("mockMethod");
        List<JsonNode> jsonNodeListMock = new ArrayList<>();

        // When
        JsonError result = rskErrorResolver.resolveError(exception, methodMock, jsonNodeListMock);

        // Then
        Assert.assertNotNull(result);
        Assert.assertEquals(-32603, result.code);
        Assert.assertEquals("Internal server error", result.message);
        Assert.assertNull(result.data);
    }

    public void mockMethod() { }

}
