import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

import kotlin.math.floor

fun main(args: Array<String>) {
    when (args.size) {
        0 -> {
            println("Нет аргументов!")
            return
        }
        1 -> {
            val urlAddress = "https://api.openweathermap.org/data/2.5/weather?q=${args[0]}" +
                    "&appid=0f21bd02afdc24c032045afd167ff588&units=metric&lang=ru"
            val output = getWeatherInfo(urlAddress)
            if (output.isNotBlank()) {
                val obj = JSONObject(output)
                println(generalInfo(obj))
                println(temperatureInfo(obj))
                println(windInfo(obj))
            }
        }
        else -> {
            println("Слишком много аргументов!")
            return
        }
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
    var result = String()
    try {
        val urlConnection = URL(urlAddress).openConnection()
        val stream = InputStreamReader(urlConnection.getInputStream())
        result = stream.buffered().use(BufferedReader::readText)
    } catch (e: Exception) {
        println("Такой город не найден!")
    }
    return result
}