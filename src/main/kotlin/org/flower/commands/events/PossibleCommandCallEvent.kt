package org.flower.commands.events

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.flower.commands.Command

/**
 * A [MessageReceivedEvent] subclass used to gather
 * information within a [Command] with the
 * [Command.isCalled] method.
 *
 * @author T.Francois
 */
open class PossibleCommandCallEvent(
    api: JDA,
    responseNumber: Long,
    message: Message,
    var initiator: Command?,
    var alias: String,
    var prefix: String,
    var args: List<String>,
    var separator: String = " "): MessageReceivedEvent(api, responseNumber, message) {
}