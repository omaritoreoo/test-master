package com.example.test.data

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.IOException

/**
 * A Gson TypeAdapter for handling cases where a JSON field that is expected to be a String
 * might sometimes be an empty JSON object ({}). This adapter will parse an empty object
 * as null, and a string as its value. It also writes null or empty strings as JSON nulls.
 */
class EmptyObjectToNullStringAdapter : TypeAdapter<String>() {

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: String?) {
        // If the string is null or empty, write a JSON null.
        if (value == null || value.isEmpty()) {
            out.nullValue()
        } else {
            out.value(value)
        }
    }

    @Throws(IOException::class)
    override fun read(reader: JsonReader): String? {
        return when (reader.peek()) {
            // If the token is a BEGIN_OBJECT (meaning an empty {}), consume it and return null.
            com.google.gson.stream.JsonToken.BEGIN_OBJECT -> {
                reader.skipValue() // Consume the object token
                null
            }
            // If the token is a STRING, read and return the string value.
            com.google.gson.stream.JsonToken.STRING -> reader.nextString()
            // If the token is a NULL, consume it and return null.
            com.google.gson.stream.JsonToken.NULL -> {
                reader.nextNull()
                null
            }
            // For any other unexpected token, skip it and return null (or throw an error if strict).
            else -> {
                reader.skipValue()
                null
            }
        }
    }
}