package com.receparslan.finance.util

import android.util.Log
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.receparslan.finance.model.KlineData
import java.lang.reflect.Type

class KlineDataDeserializer : JsonDeserializer<KlineData> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): KlineData {
        // Initialize with default values
        var klineData = KlineData(0, "", "", "", "", 0L)

        if (json != null && json.isJsonArray) {
            val data = json.asJsonArray

            val item = data.asJsonArray

            if (item.size() >= 7) {
                try {
                    klineData = KlineData(
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