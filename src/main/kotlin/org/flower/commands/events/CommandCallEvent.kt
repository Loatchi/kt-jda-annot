package org.flower.commands.events

import org.flower.commands.Command

open class CommandCallEvent(event: PossibleCommandCallEvent):
    PossibleCommandCallEvent(
        event.jda,
        event.responseNumber,
        event.message,
        event.initiator,
        event.alias,
        event.prefix,
        event.args,
        event.separator){

        open fun asSubCommandCall(newInitiator: Command?, start: Int = 1, end: Int = args.size): CommandCallEvent{
            return CommandCallEvent(
                PossibleCommandCallEvent(
                    jda,
                    responseNumber,
                    message,
                    newInitiator,
                    alias,
                    prefix,
                    args.subList(start, end),
                    separator)
            )
        }
    }