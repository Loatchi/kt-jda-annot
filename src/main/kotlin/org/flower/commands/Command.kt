package org.flower.commands

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageEmbed
import org.flower.commands.events.CommandCallEvent
import org.flower.commands.events.PossibleCommandCallEvent
import java.awt.Color

/**
 * An abstract class representing a basic command tool using
 * only strings. It may be used directly, but it would be better
 * to use [FunctionCommand] or [TypedCommand].
 *
 * @see FunctionCommand
 * @see TypedCommand
 */
abstract class Command(
    val api: JDA,
    var name: (PossibleCommandCallEvent?) -> String,
    var help: (PossibleCommandCallEvent?) -> String,
    var prefix: (PossibleCommandCallEvent?) -> String,
    var aliases: (PossibleCommandCallEvent?) -> MutableList<String>,
    var botRunnable: Boolean,
    val subCommands: MutableList<Command> = mutableListOf(),
    val overloads: MutableList<Command> = mutableListOf(),
    var parent: Command? = null,
){

    open fun embedHelpMessage(event: PossibleCommandCallEvent): MessageEmbed{
        val embedBuilder = EmbedBuilder()
        embedBuilder.setColor(Color.orange)
        embedBuilder.addField("Aliases: ${aliases(event)}", help(event), false)
        embedBuilder.setAuthor(name(event), null,
            "https://cdn.discordapp.com/avatars/780015700193705985/145a0a05ddd9302b4b41b3aa2420853c.webp?size=160")
        return embedBuilder.build()
    }

    /**
     * If the [event] is a subcommand call it returns the command and null
     * otherwise.
     */
    open fun getSubCommand(event: CommandCallEvent): Command?{

        if(event.args.isEmpty())
            return null

        for(c in subCommands){
            if(c.aliases(event).contains(event.args[0]))
                return c
        }

        return null
    }

    /**
     * Return weather a command was called or not.
     * It is also used as a way to gather information
     * on a [CommandCallEvent].
     */
    open fun isCalled(event: PossibleCommandCallEvent, consideredContent: String? = null): Boolean{

        if(!botRunnable && event.author.isBot)
            return false

        val content = consideredContent ?: event.message.contentRaw
        val p = prefix(event)
        val pLen = p.length
        val a = aliases(event)

        return when (content.startsWith(p)){
            true ->{

                val i = a.indexOfFirst {
                    val startsWith = content.startsWith(it, pLen)
                    val isFollowedByNothingOrSeparator =
                        content.startsWith(event.separator, pLen + it.length)
                                || pLen + it.length == content.length

                    return@indexOfFirst startsWith && isFollowedByNothingOrSeparator
                }

                if(i != -1){
                    event.alias = a[i]
                    event.prefix = p
                    event.initiator = this
                    return true
                }

                return false
            }
            false -> false
        }
    }

    /**
     * The method called when a command is called.
     */
    abstract fun invoke(event: CommandCallEvent, invokeHelp: Boolean = false,
                        overloadCall: Boolean = false,
                        errors: MutableList<CommandException> = mutableListOf()
    ): Boolean

}