package com.openmobilehub.android.storage.core.utils

import org.json.JSONArray
import org.json.JSONObject

fun JSONArray?.toJSONObjectList(): List<JSONObject> {
    if (this == null) return emptyList()
    val list = mutableListOf<JSONObject>()
    for (i in 0 until this.length()) {
        val jsonObject = this.optJSONObject(i)
        if (jsonObject != null) {
            list.add(jsonObject)
        }
    }
    return list
}
