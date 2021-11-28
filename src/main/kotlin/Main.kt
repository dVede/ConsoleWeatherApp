import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.vararg
import org.json.JSONObject
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.net.URL
import java.net.UnknownHostException
import kotlin.math.floor

private const val API_KEY = "0f21bd02afdc24c032045afd167ff588"

private const val BLUE = "\u001b[0m" // Text Reset
private const val RESET = "\u001b[0;34m" // BLUE
private const val RED_BOLD = "\u001b[1;31m" // RED

fun main(args: Array<String>) {
    val parser = ArgParser("WeatherApp")
    val cites by parser.argument(
        type = ArgType.String,
        description = "The city name where you want to find out the weather").vararg()
    val unit by parser.option(
        type = ArgType.Choice<Units>(),
        shortName = "u",
        description = "Result measurement unit").default(Units.METRIC)
    val wind by parser.option(
        type = ArgType.Boolean,
        shortName = "w",
        description = "Info about wind").default(false)
    val temperature by parser.option(
        type = ArgType.Boolean,
        shortName = "t",
        description = "Additional temperature info").default(false)
    parser.parse(args)

    try {
        for (city in cites) {
            val output = getWeatherInfo(city, unit.name )
            val obj = JSONObject(output)
            println(generalInfo(obj, unit.str))
            if (temperature) print(temperatureInfo(obj, unit.str))
            if (wind) print(windInfo(obj))
            println("$RESET----------------------------------$BLUE\n")
        }
    }
    catch (e: FileNotFoundException) {
        println("${RED_BOLD}Такой город не найден")
    }
    catch (e: UnknownHostException) {
        println("${RED_BOLD}Нет подключения к интернету")
    }
}

private fun generalInfo(obj: JSONObject, str: String) = buildString {

    append("${RED_BOLD}${obj.getString("name")}$RESET\n")
    append("$RESET---------Общая информация---------$BLUE\n")
    append("Широта: ${obj.getJSONObject("coord").getDouble("lon")}\n")
    append("Долгота: ${obj.getJSONObject("coord").getDouble("lat")}\n")
    append("Температура: ${obj.getJSONObject("main").getDouble("temp")} $str\n")
    append("Погода: ${obj.getJSONArray("weather").getJSONObject(0).getString("description")}")
}

private fun temperatureInfo(obj: JSONObject, str: String) = buildString {
    append("$RESET-----Информация о температуре-----$BLUE\n")
    append("Ощущается: ${obj.getJSONObject("main").getDouble("feels_like")} $str\n")
    append("Минимальная: ${obj.getJSONObject("main").getDouble("temp_min")} $str\n")
    append("Максимальная: ${obj.getJSONObject("main").getDouble("temp_max")} $str\n")
    append("Давление: ${obj.getJSONObject("main").getDouble("pressure")} мм рт. ст.\n")
    append("Влажность: ${obj.getJSONObject("main").getDouble("humidity")}%\n")
}

private fun windInfo(obj: JSONObject) = buildString {
    append("$RESET--------Информация о ветре--------$BLUE\n")
    append("Скорость ветра: ${obj.getJSONObject("wind").getDouble("speed")} м/c\n")
    append("Направление ветра: ${getWindDirection(obj.getJSONObject("wind").getDouble("deg"))}\n")
}

private fun getWindDirection(degrees: Double): String {
    val section = floor(degrees / 45)
    val arr = listOf("Северный","Северо-Западный","Западный","Юго-Западный",
        "Южный","Юго-Восточный", "Восточный", "Северо-Восточный")
    return arr[(section % 8).toInt()]
}

private fun getWeatherInfo(city: String, unit: String): String {
    val urlAddress = "https://api.openweathermap.org/data/2.5/weather?q=$city" +
            "&appid=$API_KEY&units=$unit&lang=ru"
    val result: String
    val urlConnection = URL(urlAddress).openConnection()
    val stream = InputStreamReader(urlConnection.getInputStream())
    result = stream.buffered().use(BufferedReader::readText)
    return result
}

enum class Units(val str: String) {
    IMPERIAL("°F"),
    METRIC( "°C"),
    STANDARD("K")
}