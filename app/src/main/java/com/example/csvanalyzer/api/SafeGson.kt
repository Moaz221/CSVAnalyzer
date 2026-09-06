package com.example.csvanalyzer.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

/**
 * Adapter آمن لأي قيمة Int
 * بيقبل: Int, Long, Double, String, Boolean, null, Object
 */
object SafeIntAdapter : TypeAdapter<Int?>() {

    override fun write(out: JsonWriter, value: Int?) {
        if (value == null) out.nullValue() else out.value(value)
    }

    override fun read(reader: JsonReader): Int? {
        return when (reader.peek()) {
            JsonToken.NULL -> {
                reader.nextNull()
                null
            }
            JsonToken.NUMBER -> {
                // ممكن يجي Double فنقربه
                reader.nextDouble().toInt()
            }
            JsonToken.STRING -> {
                val s = reader.nextString()
                s.toDoubleOrNull()?.toInt()
            }
            JsonToken.BOOLEAN -> {
                if (reader.nextBoolean()) 1 else 0
            }
            else -> {
                // أي حاجة تانية = نتخطاها
                reader.skipValue()
                null
            }
        }
    }
}

/**
 * Adapter آمن لأي قيمة Long
 * (بديل آمن بدل استخدام SafeDoubleAdapter على Long عشان مفيش ClassCastException)
 */
object SafeLongAdapter : TypeAdapter<Long?>() {

    override fun write(out: JsonWriter, value: Long?) {
        if (value == null) out.nullValue() else out.value(value)
    }

    override fun read(reader: JsonReader): Long? {
        return when (reader.peek()) {
            JsonToken.NULL -> {
                reader.nextNull()
                null
            }
            JsonToken.NUMBER -> {
                reader.nextDouble().toLong()
            }
            JsonToken.STRING -> {
                val s = reader.nextString()
                s.toDoubleOrNull()?.toLong()
            }
            JsonToken.BOOLEAN -> {
                if (reader.nextBoolean()) 1L else 0L
            }
            else -> {
                reader.skipValue()
                null
            }
        }
    }
}

/**
 * Adapter آمن لأي قيمة Double
 */
object SafeDoubleAdapter : TypeAdapter<Double?>() {

    override fun write(out: JsonWriter, value: Double?) {
        if (value == null) out.nullValue() else out.value(value)
    }

    override fun read(reader: JsonReader): Double? {
        return when (reader.peek()) {
            JsonToken.NULL -> {
                reader.nextNull()
                null
            }
            JsonToken.NUMBER -> reader.nextDouble()
            JsonToken.STRING -> {
                val s = reader.nextString()
                s.toDoubleOrNull()
            }
            JsonToken.BOOLEAN -> {
                if (reader.nextBoolean()) 1.0 else 0.0
            }
            else -> {
                reader.skipValue()
                null
            }
        }
    }
}

/**
 * Adapter آمن لأي String
 * بيقبل رقم أو نص وبيحوله String
 */
object SafeStringAdapter : TypeAdapter<String?>() {

    override fun write(out: JsonWriter, value: String?) {
        if (value == null) out.nullValue() else out.value(value)
    }

    override fun read(reader: JsonReader): String? {
        return when (reader.peek()) {
            JsonToken.NULL -> {
                reader.nextNull()
                null
            }
            JsonToken.STRING -> reader.nextString()
            JsonToken.NUMBER -> reader.nextDouble().toString()
            JsonToken.BOOLEAN -> reader.nextBoolean().toString()
            else -> {
                reader.skipValue()
                null
            }
        }
    }
}

/**
 * الـ Gson الآمن الجاهز لكل الحالات
 */
object SafeGson {
    val instance: Gson by lazy {
        GsonBuilder()
            .setLenient()
            .registerTypeAdapter(Int::class.java, SafeIntAdapter)
            .registerTypeAdapter(Int::class.javaObjectType, SafeIntAdapter)
            .registerTypeAdapter(java.lang.Integer::class.java, SafeIntAdapter)

            .registerTypeAdapter(Double::class.java, SafeDoubleAdapter)
            .registerTypeAdapter(Double::class.javaObjectType, SafeDoubleAdapter)
            .registerTypeAdapter(java.lang.Double::class.java, SafeDoubleAdapter)

            // Long Adapter مخصص (مش DoubleAdapter)
            .registerTypeAdapter(Long::class.java, SafeLongAdapter)
            .registerTypeAdapter(Long::class.javaObjectType, SafeLongAdapter)
            .registerTypeAdapter(java.lang.Long::class.java, SafeLongAdapter)

            .registerTypeAdapter(String::class.java, SafeStringAdapter)

            .serializeNulls()
            .create()
    }
}
