import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

fun main(args: Array<String>) {
    when (args.size) {
        0 -> {
            println("Arguments not found")
            return
        }
        1 -> {
            val urlAddress = "https://api.openweathermap.org/data/2.5/weather?q=${args[0]}&appid=0f21bd02afdc24c032045afd167ff588&units=metric"
            val output = getWeatherInfo(urlAddress)
            if (output.isNotBlank()) {
                val obj = JSONObject(output)
                println("Температура: " + obj.getJSONObject("main").getDouble("temp"))
            }
        }
        else -> {
            println("Too much arguments")
            return
        }
    }
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