package org.flower.commands


/**
 * An annotation to declare some field on a function command,
 * to ease the creation of one with the asFunctionCommand()
 * extension.
 *
 * @see FunctionCommand
 * @author T.Francois
 * @since 1.0
 */
@Target(AnnotationTarget.FUNCTION)
annotation class FlowerCommand(

    /**
     * The name of the linked command.
     */
    val name: String = "",

    /**
     * The aliases of the linked command.
     */
    val aliases: Array<String> = [],

    /**
     * A short string explaining what the linked command
     * should do.
     */
    val help: String = "",

    /**
     * Prefix of the linked command.
     */
    val prefix: String = "",

    /**
     * The id of the command, used to create subcommands.
     */
    val id: String = "",

    /**
     * The parent id for subcommand calls.
     */
    val parentId: String = "",

    /**
     * If this command can be run by bots.
     */
    val botRunnable: Boolean = false,

    /**
     * An example of a certain command.
     */
    val example: String = "",

    /**
     * Usable globals flags.
     */
    val usableGlobalFlags: Array<GlobalFlag> = [GlobalFlag.AUTO_DELETE],

    /**
     * See part of the help
     */
    val see: String = "",

)
{

}