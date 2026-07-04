package com.receparslan.finance.core.data.remote.serializer

import android.util.Log
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.receparslan.finance.feature.detail.data.remote.dto.KlineDataDto
import java.lang.reflect.Type

class KlineDataDeserializer : JsonDeserializer<KlineDataDto> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): KlineDataDto {
        var klineData = KlineDataDto(0, "", "", "", "", 0L)

        if (json != null && json.isJsonArray) {
            val item = json.asJsonArray
            if (item.size() >= 6) {
                try {
                    klineData = KlineDataDto(
                        openTime = item[0].asLong,
                        open = item[1].asString,
                        high = item[2].asString,
                        low = item[3].asString,
                        close = item[4].asString,
                        closeTime = item[6].asLong
                    )
                } catch (e: Exception) {
                    Log.e("KlineDataDeserializer", "Parse error: ${e.message}")
                }
            } else Log.e("KlineDataDeserializer", "Insufficient data in item: $item")
        } else Log.e("KlineDataDeserializer", "JSON element is null or not an array")

        return klineData
    }
}