import org.json.JSONObject
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.net.URL
import java.net.UnknownHostException

import kotlin.math.floor

private val units = listOf("imperial","metric","standard")
private const val API_KEY = "0f21bd02afdc24c032045afd167ff588"

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Нет аргументов!")
        return
    }
    else if (args.size > 2) {
        println("Слишком много аргументов!")
        return
    }
    var unit = "standard"
    if (args.size == 2) {
        if (units.contains(args[1].lowercase()))
            unit = args[1]
        else
            println("Единица измерения ${args[1]} не найдена, использована единица измерения $unit")
    }
    val urlAddress = "https://api.openweathermap.org/data/2.5/weather?q=${args[0]}" +
            "&appid=$API_KEY&units=$unit&lang=ru"
    try {
        val output = getWeatherInfo(urlAddress)
        val obj = JSONObject(output)
        println(generalInfo(obj))
        println(temperatureInfo(obj))
        println(windInfo(obj))
    }
    catch (e: FileNotFoundException) {
        println("Такой город не найден")
    }
    catch (e: UnknownHostException) {
        println("Нет подключения к интернету")
    }
}

private fun generalInfo(obj: JSONObject) = buildString {
    append("-----Общая информация-----\n")
    append("Город: ${obj.getString("name")}\n")
    append("Широта: ${obj.getJSONObject("coord").getDouble("lon")}\n")
    append("Долгота: ${obj.getJSONObject("coord").getDouble("lat")}\n")
    append("Погода: ${obj.getJSONArray("weather").getJSONObject(0).getString("description")}\n")
}

private fun temperatureInfo(obj: JSONObject) = buildString {
    append("-----Информация о температуре-----\n")
    append("Температура: ${obj.getJSONObject("main").getDouble("temp")}\n")
    append("Ощущается: ${obj.getJSONObject("main").getDouble("feels_like")}\n")
    append("Минимальная: ${obj.getJSONObject("main").getDouble("temp_min")}\n")
    append("Максимальная: ${obj.getJSONObject("main").getDouble("temp_max")}\n")
    append("Давление: ${obj.getJSONObject("main").getDouble("pressure")}\n")
    append("Влажность: ${obj.getJSONObject("main").getDouble("humidity")}\n")
}

private fun windInfo(obj: JSONObject) = buildString {
    append("-----Информация о ветре-----\n")
    append("Скорость ветра: ${obj.getJSONObject("wind").getDouble("speed")}\n")
    append("Направление ветра: ${getWindDirection(obj.getJSONObject("wind").getDouble("deg"))}\n")
}

private fun getWindDirection(degrees: Double): String {
    val section = floor(degrees / 45)
    val arr = listOf("Северный","Северо-Западный","Западный","Юго-Западный",
        "Южный","Юго-Восточный", "Восточный", "Северо-Восточный")
    return arr[(section % 8).toInt()]
}

private fun getWeatherInfo(urlAddress: String): String {
    val result: String
    val urlConnection = URL(urlAddress).openConnection()
    val stream = InputStreamReader(urlConnection.getInputStream())
    result = stream.buffered().use(BufferedReader::readText)
    return result
}