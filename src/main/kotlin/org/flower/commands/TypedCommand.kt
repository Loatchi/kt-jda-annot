package org.flower.commands

import net.dv8tion.jda.api.JDA
import org.flower.commands.events.CommandCallEvent
import org.flower.commands.events.PossibleCommandCallEvent
import org.flower.commands.events.TypedCommandCallEvent

abstract class TypedCommand(
    api: JDA,
    name: (PossibleCommandCallEvent?) -> String,
    help: (PossibleCommandCallEvent?) -> String,
    prefix: (PossibleCommandCallEvent?) -> String,
    aliases: (PossibleCommandCallEvent?) -> MutableList<String>,
    var commandSignature: CommandSignature,
    botRunnable: Boolean,
    subCommands: MutableList<Command> = mutableListOf(),
    overloads: MutableList<Command> = mutableListOf()
): Command(api, name, help, prefix, aliases, botRunnable, subCommands, overloads){


    open fun createTypedArguments(event: TypedCommandCallEvent, types: List<TypedCommandCallEvent.TypedArgument<*>>): CommandCall{
        return commandSignature.getCallArguments(event, types)
    }

    abstract fun invokeException(event: TypedCommandCallEvent, exe: CommandException)
    abstract override fun invoke(event: CommandCallEvent, invokeHelp: Boolean,
                                 overloadCall: Boolean, errors: MutableList<CommandException>): Boolean
}

