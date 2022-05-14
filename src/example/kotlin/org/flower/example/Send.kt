package org.flower.example

import net.dv8tion.jda.api.entities.TextChannel
import org.flower.commands.FlowerCommand
import org.flower.commands.FlowerNamed
import org.flower.commands.events.TypedCommandCallEvent
import org.flower.types.EMPTY_TEXT_CHANNEL
import org.flower.types.Named
import org.flower.types.Quote

const val SEND_ID = "SEND"

@FlowerCommand(id = SEND_ID)
fun send(event: TypedCommandCallEvent,
         quote: Quote,
         @FlowerNamed(["pipe"]) pipe: Named<TextChannel> = Named("", EMPTY_TEXT_CHANNEL)
){
    val channel = if(pipe.value === EMPTY_TEXT_CHANNEL) event.channel else pipe.value
    channel.sendMessage(quote.message).queue()
}