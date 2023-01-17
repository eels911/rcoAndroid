package com.sabina.project.base.external.extensions

import java.text.DecimalFormat

fun Double.getRoundedGeo(): Double {
    var formattedDouble = DecimalFormat("#0.00000000").format(this)
    formattedDouble = formattedDouble.replace(",", ".")
    return formattedDouble.toDouble()
}