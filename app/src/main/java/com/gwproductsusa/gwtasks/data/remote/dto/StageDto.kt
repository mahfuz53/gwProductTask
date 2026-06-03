package com.gwproductsusa.gwtasks.data.remote.dto

import com.google.gson.annotations.SerializedName

data class StageDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)
