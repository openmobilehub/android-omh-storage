package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class CurrentUserAccountResponse(
    @JsonProperty("root_info")
    val rootInfo: RootInfo
)

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class RootInfo(
    @JsonProperty("root_namespace_id")
    val rootNamespaceId: String
)

// {
//  "account_id": "dbid:<REMOVED>",
//  "name": {
//    "given_name": "Imaginary",
//    "surname": "User",
//    "familiar_name": "Imaginary User",
//    "display_name": "Imaginary User",
//    "abbreviated_name": "IU"
//  },
//  "email": "test@test.com",
//  "email_verified": true,
//  "disabled": false,
//  "country": "US",
//  "locale": "en",
//  "referral_link": "https://www.dropbox.com/referrals/UNKNOWN",
//  "is_paired": false,
//  "account_type": {
//    ".tag": "basic"
//  },
//  "root_info": {
//    ".tag": "user",
//    "root_namespace_id": "<REMOVED>",
//    "home_namespace_id": "<REMOVED>"
//  }
// }
