import com.dvede.weather.*
import com.github.ajalt.clikt.core.NoSuchOption
import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.UsageError
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.FileNotFoundException
import java.io.IOException


internal class WeatherInfoTest {

    private val testAnswer = JSONObject("{\"coord\":{\"lon\":37.6156,\"lat\":55.7522},\"weather\":[{\"id\":804,\"main\":\"Clouds\"," +
            "\"description\":\"пасмурно\",\"icon\":\"04n\"}],\"base\":\"stations\",\"main\":{\"temp\":266.08," +
            "\"feels_like\":262.31,\"temp_min\":264.9,\"temp_max\":266.44,\"pressure\":1006,\"humidity\":97," +
            "\"sea_level\":1006,\"grnd_level\":986},\"visibility\":1430,\"wind\":{\"speed\":2.1,\"deg\":140," +
            "\"gust\":5.16},\"clouds\":{\"all\":96},\"dt\":1641304776,\"sys\":{\"type\":1,\"id\":9027,\"country" +
            "\":\"RU\",\"sunrise\":1641275888,\"sunset\":1641301814},\"timezone\":10800,\"id\":524901,\"name\":" +
            "\"Москва\",\"cod\":200}")

    private val units = listOf("°F", "°C", "K")

    @Test
    @DisplayName("getWindDirection")
    fun degreesToName() {
        assertEquals(getWindDirection(0.0),"Северный")
        assertEquals(getWindDirection(45.0),"Северо-Западный")
        assertEquals(getWindDirection(90.0),"Западный")
        assertEquals(getWindDirection(135.0),"Юго-Западный")
        assertEquals(getWindDirection(180.0),"Южный")
        assertEquals(getWindDirection(225.0),"Юго-Восточный")
        assertEquals(getWindDirection(270.0),"Восточный")
        assertEquals(getWindDirection(315.0),"Северо-Восточный")
        assertEquals(getWindDirection(360.0),"Северный")
        assertEquals(getWindDirection(44.99999),"Северный")
        assertEquals(getWindDirection(45656.0),"Восточный")
    }

    @Test
    @DisplayName("incorrectCommand")
    fun incorrectCommand() {
        var incorrectInput: Set<String> = mutableSetOf("-help")
        var array = incorrectInput.toTypedArray()
        var thrown: Throwable = assertThrows(NoSuchOption::class.java) {
            WeatherInfo().parse(array)
        }
        assertNotNull(thrown.message)
        incorrectInput = mutableSetOf("")
        array = incorrectInput.toTypedArray()
        thrown = assertThrows(UsageError::class.java) {
            WeatherInfo().parse(array)
        }
        assertNotNull(thrown.message)
    }

    @Test
    @DisplayName("helpCommand")
    fun helpCommand() {
        val incorrectInput: Set<String> = mutableSetOf("--help")
        val array = incorrectInput.toTypedArray()
        try {
            WeatherInfo().parse(array)
        } catch (ex: PrintHelpMessage) {
            assertNotNull(ex.error)
        }
    }

    @Test
    @DisplayName("windOutput")
    fun windOutput() {
        val output = windInfo(testAnswer)
        val excepted = buildString {
            appendLine("\u001B[0;34m--------Информация о ветре--------\u001B[0m")
            appendLine("Скорость ветра: 2.1 м/c")
            appendLine("Направление ветра: Юго-Западный")
        }
        assertEquals(excepted, output)
    }

    @Test
    @DisplayName("temperatureOutput")
    fun temperatureOutput() {
        for (i in units.indices) {
            val output = temperatureInfo(testAnswer, units[i])
            val excepted = buildString {
                appendLine("\u001B[0;34m-----Информация о температуре-----\u001B[0m")
                appendLine("Ощущается: 262.31 ${units[i]}")
                appendLine("Минимальная: 264.9 ${units[i]}")
                appendLine("Максимальная: 266.44 ${units[i]}")
                appendLine("Давление: 1006.0 мм рт. ст.")
                appendLine("Влажность: 97.0%")
            }
            assertEquals(excepted, output)
        }
    }

    @Test
    @DisplayName("generalOutput")
    fun generalOutput() {
        for (i in units.indices) {
            println("1")
            val output = generalInfo(testAnswer, units[i])
            val excepted = buildString {
                appendLine("\u001B[1;31mМосква\u001B[0;34m")
                appendLine("\u001B[0;34m---------Общая информация---------\u001B[0m")
                appendLine("Широта: 37.6156")
                appendLine("Долгота: 55.7522")
                appendLine("Температура: 266.08 ${units[i]}")
                append("Погода: пасмурно")
            }
            assertEquals(excepted, output)
        }
    }

    @Test
    @DisplayName("incorrectCity")
    fun incorrectCity() {
        val thrown: Throwable = assertThrows(FileNotFoundException::class.java) {
            getWeatherInfo("Not city", UnitsTemp.METRIC, "0f21bd02afdc24c032045afd167ff588")
        }
        assertNotNull(thrown.message)
    }

    @Test
    @DisplayName("incorrectAPI")
    fun incorrectApiKey() {
        val thrown: Throwable = assertThrows(IOException::class.java) {
            getWeatherInfo("Москва", UnitsTemp.METRIC, "not api key")
        }
        assertNotNull(thrown.message)
    }

    @Test
    @DisplayName("getWeatherInfo")
    fun getWeatherInfo() {
        val weather = getWeatherInfo("Москва", UnitsTemp.METRIC, "0f21bd02afdc24c032045afd167ff588")
        assertNotNull(weather)
    }

}