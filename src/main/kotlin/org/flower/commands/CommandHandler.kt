package org.flower.commands

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.flower.commands.events.CommandCallEvent
import org.flower.commands.events.PossibleCommandCallEvent
import org.flower.commands.events.TypedCommandCallEvent
import org.flower.types.CType

/**
 * A command handler for the commands.
 *
 * @see Command
 */
open class CommandHandler(
    val commands: MutableList<Command>,
    var types: List<CType<*>>
): ListenerAdapter() {

    override fun onMessageReceived(event: MessageReceivedEvent) {

        val raw = event.message.contentRaw
        val split = raw.split(" ")

        val arguments = split.drop(1)

        val pos = PossibleCommandCallEvent(
            api = event.jda,
            responseNumber = event.responseNumber,
            message = event.message,
            initiator = null,
            alias = "",
            prefix = "",
            args = arguments,
        )

        for(c in commands.toList()){
            if(c.isCalled(pos)){
                val e = TypedCommandCallEvent(CommandCallEvent(pos), types)
                c.invoke(e)
            }
        }

    }
}