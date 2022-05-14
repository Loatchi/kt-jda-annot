package org.flower.example

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.dv8tion.jda.api.JDABuilder
import org.flower.commands.*
import org.flower.commands.events.TypedCommandCallEvent
import org.flower.types.C_TYPES
import java.io.InputStreamReader
import kotlin.system.exitProcess

val USED_C_TYPES = (C_TYPES).toMutableList()
var CUSTOM_COMMAND_HANDLER = CommandHandler(emptyList<Command>().toMutableList(), USED_C_TYPES)
val COMMANDS_DATA: Map<String, Map<String, Any>> = Gson().fromJson(
    InputStreamReader(object {}.javaClass.getResourceAsStream("/commands.json") ?: throw Exception("")),
    object : TypeToken<Map<String, Any>>(){}.type)

fun main(args: Array<String>) {

    if(args.isEmpty()) {
        println("No discord token provided")
        exitProcess(1)
    }

    val jda = JDABuilder.createDefault(args[0]).build().awaitReady()
    val functions = getAllFunctionCommandWithinPackage("org.flower", jda, USED_C_TYPES, ::exception, COMMANDS_DATA)
    CUSTOM_COMMAND_HANDLER.commands.addAll(functions)
    CUSTOM_COMMAND_HANDLER.commands.forEach { println(it.name(null)) }
    jda.addEventListener(CUSTOM_COMMAND_HANDLER)

    do {
        println("Type \"STOP\" to stop the bot.")
    } while(readln().lowercase() != "stop")
    println("Shutting down.")
    jda.shutdown()
}

@FlowerException(id = "NULL")
fun exception(event: TypedCommandCallEvent, error: CommandException){
    event.message.reply(error.toString()).queue()
}
