package org.flower.commands

import org.flower.bot.exception
import org.flower.commands.events.CommandCallEvent
import org.flower.commands.events.TypedCommandCallEvent
import org.flower.types.CType
import org.flower.types.Named
import kotlin.reflect.typeOf

open class CommandException(val event: CommandCallEvent, val message: String){

    companion object{
        val TYPE = typeOf<CommandException>()
    }

    fun buildBase(builder: StringBuilder){
        var command = event.initiator

        val stacktrace = mutableListOf<Command>()
        while (command != null) {
            stacktrace.add(0, command)
            command = command.parent

        }

        builder.append("*")
        stacktrace.forEachIndexed { i, it ->
            if(i != stacktrace.size - 1)
                builder.append(it.name(event) + "\n" + "  ".repeat(i + 1) + "└─")
            else
                builder.append(it.name(event) + "*\n")
        }

        builder.append("Error: $message ")

    }

    override fun toString(): String {
        return "An error occured on ${event.initiator}: $message"
    }
}

open class TypedCommandException(
    event: TypedCommandCallEvent,
    message: String,
    val parameterNumber: Int,
    ): CommandException(event, message){
    override fun toString(): String {
        val builder = StringBuilder()
        buildBase(builder)
        return builder.toString()
    }
}

open class ParameterCommandException(event: TypedCommandCallEvent,
                                     message: String,
                                     parameterNumber: Int,
                                     private val parameterMessage: String
): TypedCommandException(event, message, parameterNumber){

    override fun toString(): String {

        val builder = StringBuilder()
        val event = event as TypedCommandCallEvent

        buildBase(builder)

        builder.append("__*on parameter #$parameterNumber*__ - **[/!\\\\LAZY EVAL /!\\\\]**\n**" +
                "=>** ${event.prefix + event.alias + event.separator}")
        val rawStrings = event.typedArgs.map { it.raw }

        rawStrings.forEachIndexed { i, it ->

            if (i == parameterNumber)
                builder.append("`")

            builder.append(it)

            if (i == parameterNumber)
                builder.append("`")

            if (i != rawStrings.size - 1)
                builder.append(event.separator)
        }

        builder.append("\n$parameterMessage")

        return builder.toString()

    }
}

open class WrongTypeCommandException(event: TypedCommandCallEvent,
                                     message: String,
                                     parameterNumber: Int,
                                     expected: CType<*>
): ParameterCommandException(event, message, parameterNumber,
    "TYPE: ${event.typedArgs[parameterNumber].clazz.innerToString()}\n" + "EXPECTED: ${expected.innerToString()}")

class CommandDoestUseGlobalFlag(e: TypedCommandCallEvent, s: String, j: Int, g: GlobalFlag):
    ParameterCommandException(e, s, j, "FLAG: This global flag `${g.flagName}` is not in use here.")

class IncompleteCommandCallCommandException(event: TypedCommandCallEvent,
                                            message: String,
                                            parameterNumber: Int
): TypedCommandException(event, message, parameterNumber){

    override fun toString(): String {
        val builder = StringBuilder()
        buildBase(builder)

        val command = event.initiator as TypedCommand

        val needed = command.commandSignature.mandatory
            .subList(parameterNumber, command.commandSignature.mandatory.size).map { it.cls.simpleName }

        builder
            .append(", missing at least ${command.commandSignature.mandatory.size - parameterNumber} parameters\n")
            .append("**=>** `${needed}`\n")

        return builder.toString()
    }
}

class MemberDoesntHavePermissionForFlag(e: TypedCommandCallEvent, s: String, j: Int, flag: Flag):
    ParameterCommandException(e, s, j, "FLAG: Author does not have permission to use `${flag.flags}` flags."){
}

class FlagDoesNotExistException(e: TypedCommandCallEvent, s: String, j: Int, triedFlag: Named<*>):
        ParameterCommandException(e, s, j, "FLAG: This named argument does not exist `${triedFlag.name}`.")

class OverloadException(event: TypedCommandCallEvent, val exceptions: MutableList<CommandException>): CommandException(event, ""){
    override fun toString(): String {
        return "Error on an overloaded command: ${exceptions.size} total error(s)\nFirst one:\n${exceptions[0]}"
    }
}