package com.dvede.weather

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import org.json.JSONObject
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.net.URL
import java.net.UnknownHostException
import java.util.*
import kotlin.math.floor

private const val BLUE = "\u001b[0m" // Text Reset
private const val RESET = "\u001b[0;34m" // BLUE
private const val RED_BOLD = "\u001b[1;31m" // RED

private val scanner = Scanner(System.`in`, Charsets.UTF_8)

class WeatherInfo : CliktCommand() {
    private val wind: Boolean by option("-w", "--wind", help = "Get info about wind").flag()
    private val temperature: Boolean by option("-t", "--temp", help = "Get additional temperature info").flag()
    private val apiKey: String by option("-a", "--api", help = "Change to custom API-key")
        .default("0f21bd02afdc24c032045afd167ff588")
    private val units: UnitsTemp by option("-u", "--unit", help = "Change temperature measurement unit")
        .enum<UnitsTemp>(ignoreCase = true).default(UnitsTemp.METRIC)

    override fun run() {
        while (true) {
            val cities = scanner.nextLine().split(",")
            if (cities.first() == "") return
            for (city in cities) {
                try {
                    val cityFixed = city.trimStart().trimEnd()
                    val output = getWeatherInfo(cityFixed, units, apiKey)
                    val obj = JSONObject(output)
                    println(generalInfo(obj, units.str))
                    if (temperature) print(temperatureInfo(obj, units.str))
                    if (wind) print(windInfo(obj))
                    println("$RESET----------------------------------$BLUE\n")
                }
                catch (e: FileNotFoundException) {
                    println("${RED_BOLD}Такой город не найден: $city$RESET\n")
                }
                catch (e: UnknownHostException) {
                    println("${RED_BOLD}Нет подключения к интернету$RESET\n")
                }
            }
        }
    }

}
fun generalInfo(obj: JSONObject, str: String) = buildString {
    append("${RED_BOLD}${obj.getString("name")}$RESET\n")
    append("$RESET---------Общая информация---------$BLUE\n")
    append("Широта: ${obj.getJSONObject("coord").getDouble("lon")}\n")
    append("Долгота: ${obj.getJSONObject("coord").getDouble("lat")}\n")
    append("Температура: ${obj.getJSONObject("main").getDouble("temp")} $str\n")
    append("Погода: ${obj.getJSONArray("weather").getJSONObject(0).getString("description")}")
}

fun temperatureInfo(obj: JSONObject, str: String) = buildString {
    append("$RESET-----Информация о температуре-----$BLUE\n")
    append("Ощущается: ${obj.getJSONObject("main").getDouble("feels_like")} $str\n")
    append("Минимальная: ${obj.getJSONObject("main").getDouble("temp_min")} $str\n")
    append("Максимальная: ${obj.getJSONObject("main").getDouble("temp_max")} $str\n")
    append("Давление: ${obj.getJSONObject("main").getDouble("pressure")} мм рт. ст.\n")
    append("Влажность: ${obj.getJSONObject("main").getDouble("humidity")}%\n")
}

fun windInfo(obj: JSONObject) = buildString {
    append("$RESET--------Информация о ветре--------$BLUE\n")
    append("Скорость ветра: ${obj.getJSONObject("wind").getDouble("speed")} м/c\n")
    append("Направление ветра: ${getWindDirection(obj.getJSONObject("wind").getDouble("deg"))}\n")
}

fun getWindDirection(degrees: Double): String {
    val section = floor(degrees / 45)
    val arr = listOf("Северный","Северо-Западный","Западный","Юго-Западный",
        "Южный","Юго-Восточный", "Восточный", "Северо-Восточный")
    return arr[(section % 8).toInt()]
}

fun getWeatherInfo(city: String, unit: UnitsTemp, apiKey: String): String {
    val urlAddress = "https://api.openweathermap.org/data/2.5/weather?q=$city" +
            "&appid=$apiKey&units=${unit.name}&lang=ru"
    val result: String
    val urlConnection = URL(urlAddress).openConnection()
    val stream = InputStreamReader(urlConnection.getInputStream())
    result = stream.buffered().use(BufferedReader::readText)
    return result
}