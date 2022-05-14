package org.flower.commands

import io.github.classgraph.ClassGraph
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageEmbed
import org.flower.commands.events.CommandCallEvent
import org.flower.commands.events.PossibleCommandCallEvent
import org.flower.commands.events.TypedCommandCallEvent
import org.flower.exceptions.*
import org.flower.types.CType
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.createType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.kotlinFunction

/**
 * A class representing a function command. It is used to transform
 * a kotlin function directly into a [TypedCommand], with optional
 * and mandatory parameters abilities.
 *
 * @see TypedCommand
 * @see CType
 */
open class FunctionCommand(
    private val function: KFunction<*>,
    types: List<CType<*>>,
    api: JDA,
    name: (PossibleCommandCallEvent?) -> String,
    help: (PossibleCommandCallEvent?) -> String,
    prefix: (PossibleCommandCallEvent?) -> String,
    aliases: (PossibleCommandCallEvent?) -> MutableList<String>,
    botRunnable: Boolean,
    private val runExample: String,
    private val seeHelp: String,
    usableGlobalFlags: List<GlobalFlag>,
    subCommands: MutableList<Command> = mutableListOf(),
    overloads: MutableList<Command> = mutableListOf(),
    private val functionMap: Map<String, Any>? = null,
    private val exception: KFunction<*>? = null,
    private val argumentsPredicate: KFunction<*>? = null
): TypedCommand(api, name, help, prefix, aliases, function.asCommandSignature(types, usableGlobalFlags, functionMap),
    botRunnable, subCommands, overloads) {

    private val mandatoryString: String
    private val optionalString: String
    private val namedString: String

    init {

        val f = {list: List<CType<*>> -> list.map {
            val regex = Regex("([A-Z](?:[a-z]|[A-Z])*)")
            val classes = regex.findAll(it.jType.toString()).map { it2 -> it2.value }.toList()
            return@map classes.joinToString("<") + if (classes.size != 1) ">".repeat(classes.size) else "" }
        }

        mandatoryString = f(commandSignature.mandatory).toString()
        optionalString = f(commandSignature.optional.filter { !it.isNamed() }).toString()
        namedString = commandSignature.named.map {
            val flag = it.key
            return@map "(${flag.flags.joinToString { it2 -> "`$it2`" }}): ${flag.help}"
        }.joinToString("\n")
    }

    override fun invoke(event: CommandCallEvent, invokeHelp: Boolean,
                        overloadCall: Boolean, errors: MutableList<CommandException>): Boolean{
        val typed = event as TypedCommandCallEvent
        val sub = getSubCommand(event)

        try {

            if(sub != null){
                val ne = typed.asSubCommandCall(sub)
                return sub.invoke(ne, invokeHelp=invokeHelp, errors=errors)
            }

            if(invokeHelp){

                if(!commandSignature.usableGlobalFlag.contains(GlobalFlag.HELP)){
                    event.message.reply("La commande ne supporte pas l'appel global --help").queue()
                    return false
                }

                event.message.replyEmbeds(embedHelpMessage(event)).queue()
                return true
            }

            val parsed = createTypedArguments(event, typed.typedArgs)

            if(parsed.exception == null){

                typed.globalsActivated.addAll(parsed.globalFlags)

                val args = mutableListOf<Pair<KParameter, Any>>()
                parsed.args.forEachIndexed{i, it ->
                    if(it != null)
                        args.add(Pair(commandSignature.raw[i + 1], it))
                }

                args.add(0, Pair(commandSignature.raw[0], typed))

                if(argumentsPredicate != null){
                    val pArgs = args.mapIndexed { _, it -> Pair(argumentsPredicate.parameters[it.first.index], it.second)}

                    if(!(argumentsPredicate.callBy(mapOf(pairs = pArgs.toTypedArray())) as Boolean)){
                        return false
                    }
                }

                function.callBy(mapOf(pairs = args.toTypedArray()))

                if(typed.globalsActivated.contains(GlobalFlag.AUTO_DELETE)){
                    event.message.delete().queue()
                }


                return true

            } else {

                if(overloads.isNotEmpty() && !overloadCall){

                    val errorToInvoke = mutableListOf<CommandException>()
                    var success = false

                    for(overload in overloads){
                        success = overload.invoke(event, false, overloadCall = true, errors=errorToInvoke)
                        if(success)
                            break
                    }

                    if(!success){
                        invokeException(event, OverloadException(event, errorToInvoke))
                    }

                    return success
                }

                errors.add(parsed.exception)

                if(overloads.isEmpty() && !overloadCall)
                    invokeException(event, errors[0])

                return false
            }

        } catch (e: Exception){
            e.printStackTrace()
            return false
        }

    }

    override fun invokeException(event: TypedCommandCallEvent, exe: CommandException) {
        exception?.call(event, exe)
    }
    override fun embedHelpMessage(event: PossibleCommandCallEvent): MessageEmbed {
        val embed = super.embedHelpMessage(event)
        val builder = EmbedBuilder(embed)

        if(mandatoryString != "[]")
            builder.addField("Mandatory:", mandatoryString, false)
        if(optionalString != "[]")
            builder.addField("Optional:", optionalString, false)
        if(namedString != "")
            builder.addField("Named:", namedString, false)
        if(overloads.isNotEmpty())
            builder.addField("Overloads:", "${overloads.size + 1} total " +
                    "overloads (see all them with help command)", false)
        if(runExample != "")
            builder.addField("Example:", runExample, false)
        if(seeHelp != "")
            builder.addField("See:", seeHelp, false)
        if(subCommands.size != 0)
            builder.addField("SubCommands:", subCommands.joinToString(separator = "\n") { it.name(null) }, false)
        return builder.build()
    }
}

/**
 * Transform a KFunction into a [FunctionCommand].
 */
@Throws(MissingAnnotationOnFunctionException::class)
fun <R> KFunction<R>.asFunctionCommand(
    api: JDA,
    types: List<CType<*>>,
    exception: KFunction<*>?,
    predicate: KFunction<*>?,
    id: String? = null,
    map: Map<String, Any>? = null
): FunctionCommand {

    val flower =
        findAnnotation<FlowerCommand>() ?: throw MissingAnnotationOnFunctionException(FlowerCommand::class, this)

    val name: (PossibleCommandCallEvent?) -> String
    val prefix: (PossibleCommandCallEvent?) -> String
    val aliases: (PossibleCommandCallEvent?) -> MutableList<String>
    val help: (PossibleCommandCallEvent?) -> String
    val botRunnable: Boolean
    val example: String
    val usableFlags: List<GlobalFlag>
    val seeHelp: String
    @Suppress("UNCHECKED_CAST")
    if(map != null){
        val param = map[id] as Map<*, *>? ?: throw NullPointerException(id)

        name = { _: PossibleCommandCallEvent? -> param["name"] as String }
        prefix = { _: PossibleCommandCallEvent? -> param["prefix"] as String? ?: map["prefix"] as String }
        aliases = { _: PossibleCommandCallEvent? -> param["aliases"] as MutableList<String> }
        help = { _: PossibleCommandCallEvent? -> param["help"] as String }
        botRunnable = param["botRunnable"] as Boolean
        example = param["example"] as String
        usableFlags = (param["usableGlobalFlags"] as List<String>).map { GlobalFlag.valueOf(it) }
        seeHelp = param["see"] as String
    } else {
        name = { _: PossibleCommandCallEvent? -> flower.name }
        prefix = { _: PossibleCommandCallEvent? -> flower.prefix }
        val c = flower.aliases.toMutableList()
        aliases = { _: PossibleCommandCallEvent? -> c }
        help = { _: PossibleCommandCallEvent? -> flower.help }
        botRunnable = flower.botRunnable
        example = flower.example
        usableFlags = flower.usableGlobalFlags.toList()
        seeHelp = flower.see

    }

    if(exception != null){
        if(exception.parameters.size != 2
            && exception.parameters[0].type != TypedCommandCallEvent.TYPE
            && exception.parameters[1].type != CommandException.TYPE)
            throw MissingFunctionParameter(exception, "The exception function signature must look like: " +
                    "{${TypedCommandCallEvent.TYPE}, ${CommandException.TYPE}}")
    }

    if(predicate != null){

        var eq = parameters.size == predicate.parameters.size

        if(eq) {
            for (i in parameters.indices) {
                if (parameters[i].type != predicate.parameters[i].type
                    || parameters[i].isOptional != parameters[i].isOptional
                ) {
                    eq = false
                }
            }
        }

        if(!eq){
            throw MalformedFunctionParameters(predicate, "The predicate must have the same signature as the function command.")
        }

        if(predicate.returnType != Boolean::class.createType()){
            throw MalformedReturnTypePredicateFunction("Return type must be Boolean in $predicate.")
        }
    }

    return FunctionCommand(
        this,
        types,
        api,
        name,
        help,
        prefix,
        aliases,
        exception = exception,
        argumentsPredicate = predicate,
        botRunnable = botRunnable,
        runExample = example,
        usableGlobalFlags = usableFlags,
        seeHelp = seeHelp,
        functionMap = map?.get(id) as Map<String, Any>?
    )

}

fun getAllFunctionCommandWithinPackage(
    pkg: String,
    api: JDA,
    cTypes: List<CType<*>>,
    defaultException: KFunction<*>? = null,
    map: Map<String, Map<String, Any>>? = null
): List<FunctionCommand>{

    val flowerCommandAnnot = FlowerCommand::class
    val flowerExceptionAnnot = FlowerException::class
    val flowerArgumentsPredicateAnnot = FlowerArgumentsPredicate::class

    val fcaName = flowerCommandAnnot.java.canonicalName
    val feName = flowerExceptionAnnot.java.canonicalName
    val fapName = flowerArgumentsPredicateAnnot.java.canonicalName

    val clsG = ClassGraph().enableAllInfo().acceptPackages(pkg).scan()
    val rawFunctions = clsG.getClassesWithMethodAnnotation(fcaName).flatMap { routeClassInfo ->
                routeClassInfo.methodInfo.filter{ function ->
                    function.hasAnnotation(flowerCommandAnnot.java) }.mapNotNull { method ->
                    method.loadClassAndGetMethod().kotlinFunction
                }
            }

    val exceptionFunctions = clsG.getClassesWithMethodAnnotation(feName).flatMap { routeClassInfo ->
            routeClassInfo.methodInfo.filter{ function ->
                function.hasAnnotation(flowerExceptionAnnot.java) }.mapNotNull { method ->
                method.loadClassAndGetMethod().kotlinFunction
            }
        }


    val predicateFunctions = clsG.getClassesWithMethodAnnotation(fapName).flatMap { routeClassInfo ->
        routeClassInfo.methodInfo.filter{ function ->
            function.hasAnnotation(flowerArgumentsPredicateAnnot.java) }.mapNotNull { method ->
            method.loadClassAndGetMethod().kotlinFunction
        }
    }

    val functions = rawFunctions.map {

        val annot = it.findAnnotation<FlowerCommand>() ?: throw Exception("")
        var exception: KFunction<*>? = defaultException
        var pred: KFunction<*>? = null

        for(e in exceptionFunctions){

            val annotException = e.findAnnotation<FlowerException>() ?: throw Exception("")

            if(annotException.id == annot.id){
                exception = e
                break
            }
        }

        for(ap in predicateFunctions){

            val annotPred = ap.findAnnotation<FlowerArgumentsPredicate>() ?: throw Exception("")

            if(annotPred.id == annot.id){
                pred = ap
                break
            }
        }

        return@map Pair(annot , it.asFunctionCommand(api, cTypes, exception, pred, annot.id, map))
    }

    val toReturn = mutableListOf<FunctionCommand>()

    for(it in functions) {
        val annot = it.first
        val command = it.second

        var find = annot.parentId == ""
        for(it2 in functions){

            val annot2 = it2.first
            val command2 = it2.second

            if(annot !== annot2 && annot.id == annot2.id){
                command.overloads.add(command2)
            }

            if(annot !== annot2 && annot.parentId == annot2.id && annot2.id != ""){
                find = true
                command.parent = command2
                command2.subCommands.add(command)
            }

        }

        if(!find)
            throw MalformedFlowerCommandAnnotation("Parent id not link does not exist on name=" + annot.name)

        if(annot.parentId == "")
            toReturn.add(command)

    }

    return toReturn.toList()
}