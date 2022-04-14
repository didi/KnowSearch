package com.didi.arius.gateway.elasticsearch.client.utils;

import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.xcontent.XContentLocation;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentParser.Token;

import java.io.IOException;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * A set of static methods to get {@link Token} from {@link XContentParser}
 * while checking for their types and throw {@link ParsingException} if needed.
 */
public final class XContentParserUtils {

    private XContentParserUtils() {
    }

    /**
     * Makes sure that current token is of type {@link Token#FIELD_NAME} and the field name is equal to the provided one
     * @throws ParsingException if the token is not of type {@link Token#FIELD_NAME} or is not equal to the given field name
     */
    public static void ensureFieldName(XContentParser parser, Token token, String fieldName) throws IOException {
        ensureExpectedToken(Token.FIELD_NAME, token, parser::getTokenLocation);
        String currentName = parser.currentName();
        if (!currentName.equals(fieldName)) {
            String message = "Failed to parse object: expecting field with name [%s] but found [%s]";
            throw new ParsingException(parser.getTokenLocation(), String.format(Locale.ROOT, message, fieldName, currentName));
        }
    }

    /**
     * @throws ParsingException with a "unknown field found" reason
     */
    public static void throwUnknownField(String field, XContentLocation location) {
        String message = "Failed to parse object: unknown field [%s] found";
        throw new ParsingException(location, String.format(Locale.ROOT, message, field));
    }

    /**
     * @throws ParsingException with a "unknown token found" reason
     */
    public static void throwUnknownToken(Token token, XContentLocation location) {
        String message = "Failed to parse object: unexpected token [%s] found";
        throw new ParsingException(location, String.format(Locale.ROOT, message, token));
    }

    /**
     * Makes sure that provided token is of the expected type
     *
     * @throws ParsingException if the token is not equal to the expected type
     */
    public static void ensureExpectedToken(Token expected, Token actual, Supplier<XContentLocation> location) {
        if (actual != expected) {
            String message = "Failed to parse object: expecting token of type [%s] but found [%s]";
            throw new ParsingException(location.get(), String.format(Locale.ROOT, message, expected, actual));
        }
    }

    /**
     * Parse the current token depending on its token type. The following token types will be
     * parsed by the corresponding parser methods:
     * <ul>
     *    <li>{@link Token#VALUE_STRING}: {@link XContentParser#text()}</li>
     *    <li>{@link Token#VALUE_NUMBER}: {@link XContentParser#numberValue()} ()}</li>
     *    <li>{@link Token#VALUE_BOOLEAN}: {@link XContentParser#booleanValue()} ()}</li>
     *    <li>{@link Token#VALUE_EMBEDDED_OBJECT}: {@link XContentParser#binaryValue()} ()}</li>
     *    <li>{@link Token#VALUE_NULL}: returns null</li>
     *    <li>{@link Token#START_OBJECT}: {@link XContentParser#mapOrdered()} ()}</li>
     *    <li>{@link Token#START_ARRAY}: {@link XContentParser#listOrderedMap()} ()}</li>
     * </ul>
     *
     * @throws ParsingException if the token is none of the allowed values
     */
    public static Object parseFieldsValue(XContentParser parser) throws IOException {
        Token token = parser.currentToken();
        Object value = null;
        if (token == Token.VALUE_STRING) {
            //binary values will be parsed back and returned as base64 strings when reading from json and yaml
            value = parser.text();
        } else if (token == Token.VALUE_NUMBER) {
            value = parser.numberValue();
        } else if (token == Token.VALUE_BOOLEAN) {
            value = parser.booleanValue();
        } else if (token == Token.VALUE_EMBEDDED_OBJECT) {
            //binary values will be parsed back and returned as BytesArray when reading from cbor and smile
            value = new BytesArray(parser.binaryValue());
        } else if (token == Token.VALUE_NULL) {
            value = null;
        } else if (token == Token.START_OBJECT) {
            value = parser.mapOrdered();
        } else if (token == Token.START_ARRAY) {
            value = parser.listOrderedMap();
        } else {
            throwUnknownToken(token, parser.getTokenLocation());
        }
        return value;
    }
}
