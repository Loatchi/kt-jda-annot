package org.flower.example

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.flower.commands.FlowerArgumentsPredicate
import org.flower.commands.FlowerCommand
import org.flower.commands.FlowerNamed
import org.flower.commands.events.TypedCommandCallEvent
import org.flower.types.Named
import org.flower.types.Quote
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.InputStreamReader
import java.text.Normalizer
import kotlin.math.min

private const val HUGE_ID = "HUGE"

private const val MAX_LINE_SIZE = 8

private const val TXT = "txt"
private const val COLOR = "color"
private const val BACKGROUND = "background"

val DICTIONARY: Map<Char, List<String>> =
    Gson().fromJson(InputStreamReader(object {}.javaClass.getResourceAsStream("/huge.json") ?: throw Exception("")),
        object : TypeToken<Map<Char, List<String>>>(){}.type)

private val AUTHORIZED_CHARS = DICTIONARY.keys.joinToString("")

private val REGEX_UN_ACCENT = "\\p{InCombiningDiacriticalMarks}+".toRegex()

fun CharSequence.unAccent(): String {
    val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
    return REGEX_UN_ACCENT.replace(temp, "")
}

fun createHugeText(string: String, colorAsAnsi: String = "", opcl: Boolean = true): String{

    val builder = StringBuilder(if(opcl) "```ansi\n${colorAsAnsi}" else "")

    for(i in 0..5){
        for(char in string){
            val list = DICTIONARY[char]

            if(list != null)
                builder.append(list[i])

        }
        builder.append("\n")
    }

    return builder.append(if (opcl) "```" else "").toString()
}

@FlowerCommand(id = HUGE_ID)
fun huge(event: TypedCommandCallEvent,
         quote: Quote,
         @FlowerNamed([TXT]) txt: Named<Boolean> = Named("", false),
         @FlowerNamed([COLOR]) color: Named<String> = Named("", "null"),
         @FlowerNamed([BACKGROUND]) background: Named<String> = Named("", "null"),
){

    if(txt.value){
        val hg = createHugeText(quote.message, opcl = false)
        val tmp = File("./tmp/tmp.txt")
        val writer = FileWriter(tmp)
        writer.write(hg)
        writer.flush()
        writer.close()
        event.channel.sendFile(tmp).queue()
        tmp.delete()
        return
    }

    val rawSplit = quote.message.split(" ")
    val result = mutableListOf<String>()
    val split = mutableListOf<String>()

    // We need to work the split word so that no word in split is more
    // than MAX_LINE_SIZE length
    for(word in rawSplit){

        //we don't want to parse empty word as it may break the command
        if(word.isEmpty())
            continue

        if(word.length <= MAX_LINE_SIZE){
            split.add(word)
        } else{
            for(i in 0 .. (word.length / MAX_LINE_SIZE)){
                split.add(word.substring(i * MAX_LINE_SIZE, min((i + 1)*MAX_LINE_SIZE, word.length)))
            }
        }
    }

    var j = 0
    var k = 0
    var sum = 0

    while(k < split.size){

        val wordLen = split[j].length

        if(sum + wordLen <= MAX_LINE_SIZE){
            sum += wordLen
            k++
        } else{
            // there is no case for j==k as the array does not contain word longer than
            // MAX_LINE_SIZE
            result.add(split.subList(j, k).joinToString(" "))
            sum = 0
            j = k
        }
    }

    // not forgetting the remaining word
    if(sum != 0){
        result.add(split.subList(j, k).joinToString(" "))
    }

    val colorCode= when(color.value.lowercase()){
        "white" -> 37
        "cyan" -> 36
        "red" -> 31
        "green" -> 32
        "yellow" -> 33
        "blue" -> 34
        "pink" -> 35
        "null" -> -1
        else -> throw Exception("color error, weird !")
    }

    val backCode= when(background.value.lowercase()){
        "white" -> 47
        "orange" -> 41
        "gray" -> 44
        "black" -> 40
        "purple" -> 45
        "null" -> -1
        else -> throw Exception("color error, weird !")
    }

    val colorAsAnsi =
        if(colorCode != -1 || backCode != -1)
            "\u001b[0${ if(colorCode != -1) ";$colorCode" else ""}${if(backCode != -1) ";$backCode" else ""}m"
        else
            ""

    for(part in result){
        event.message.reply(createHugeText(part, colorAsAnsi)).queue()
    }
}

@FlowerArgumentsPredicate(id = HUGE_ID)
fun hugePredicate(event: TypedCommandCallEvent,
                  quote: Quote,
                  txt: Named<Boolean> = Named("", false),
                  color: Named<String> = Named("", "null"),
                  background: Named<String> = Named("", "null")
): Boolean{

    val toParse = quote.message.unAccent().lowercase()
    val original = quote.message
    quote.message = toParse
    val a = quote.message.length in 1..50

    if(color.value.lowercase() !in listOf("white", "red", "cyan", "yellow", "pink", "blue", "green", "null")){
        event.message.reply("Couleur (front) non supportée: `${color.value}`").queue()
        return false
    }

    if(background.value.lowercase() !in listOf("white", "orange", "gray", "black", "purple", "null")){
        event.message.reply("Couleur (back) non supportée: `${color.value}`").queue()
        return false
    }

    if(!a && !txt.value){
        event.message.reply("Il faut une chaine de caractères de longueur L: 1 <= L <= 50").queue()
        return false
    }

    val illegalCharIdx = quote.message.indexOfFirst { it !in AUTHORIZED_CHARS }

    if(illegalCharIdx != -1){
        val opener = "Caractère interdit: "
        val start = original.substring(0, illegalCharIdx)
        val badChar = original[illegalCharIdx]
        val end = original.substring(illegalCharIdx + 1)
        val toPrint = "$opener\"$start`$badChar`$end\""
        event.message.reply(toPrint).queue()
        return false
    }

    return true
}