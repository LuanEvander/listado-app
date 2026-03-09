package br.com.listado.core.util

import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val localePtBr: Locale = Locale.Builder()
    .setLanguage("pt")
    .setRegion("BR")
    .build()
private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(localePtBr)
private val decimalFormatter = DecimalFormat("0.##")
private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", localePtBr)
private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", localePtBr)

fun Double.asCurrency(): String = currencyFormatter.format(this)

fun Double.asDecimal(): String = decimalFormatter.format(this)

fun Long.asDate(): String = Instant.ofEpochMilli(this)
    .atZone(ZoneId.systemDefault())
    .toLocalDate()
    .format(dateFormatter)

fun Long.asDateTime(): String = Instant.ofEpochMilli(this)
    .atZone(ZoneId.systemDefault())
    .toLocalDateTime()
    .format(dateTimeFormatter)

fun String.toBrazilianDoubleOrNull(): Double? =
    replace(',', '.')
        .toDoubleOrNull()
