package com.husenansari.mykotlindemo.api.model

import java.io.Serializable

class AddAddressRequest : Serializable {
    var user_id: String? = null
    var address_type: String? = null
    var name: String? = null
    var address: String? = null
    var phone: String? = null
    var landmark: String? = null
    var other_type: String? = null
}
