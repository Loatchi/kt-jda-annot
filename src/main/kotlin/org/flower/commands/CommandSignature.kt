package org.flower.commands

import org.flower.commands.events.TypedCommandCallEvent
import org.flower.exceptions.CTypeParameterDoesNotExist
import org.flower.exceptions.MalformedFunctionParameters
import org.flower.exceptions.MissingFunctionParameter
import org.flower.types.CType
import org.flower.types.Named
import org.flower.types.asCTypeOrNull
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation

/**
 * A class representing a command signature. It is used
 * by a TypedCommand to represent its signature.
 *
 * @see TypedCommand
 */
open class CommandSignature(val raw: List<KParameter>,
                       val mandatory: List<CType<*>>,
                       val optional: List<CType<*>>,
                       val named: Map<Flag, Int>,
                       val usableGlobalFlag: List<GlobalFlag>
) {

    val all = mutableListOf<CType<*>>()

    init {
        all.addAll(mandatory)
        all.addAll(optional)
    }

    open fun getCallArguments(e: TypedCommandCallEvent, typed: List<TypedCommandCallEvent.TypedArgument<*>>): CommandCall {

        val usedTyped = typed.toMutableList()

        val args = mutableListOf<Any?>()
        args.addAll(List(all.size) { null })

        val globals = mutableListOf<Pair<GlobalFlag, Int>>()

        val ms = mandatory.size
        val os = optional.size

        var i = 0
        var j = 0

        val offset = mutableListOf<Int>()
        val toRemove = mutableListOf<TypedCommandCallEvent.TypedArgument<*>>()

        for(k in typed.indices){

            if(!typed[k].clazz.isNamed())
                continue

            val triedNamed = typed[k].obj as Named<*>

            try{
                val g = GlobalFlag.values().first { it.flagName == triedNamed.name}
                globals.add(Pair(g, k))
                args.remove(k)
                toRemove.add(typed[k])

            } catch (_: NoSuchElementException){}
        }

        usedTyped.removeAll(toRemove)

        val objects = usedTyped.map { it.obj }.toMutableList()
        val types = usedTyped.map { it.clazz }.toMutableList()

        val cs = types.size

        while(i < ms + os && j < cs){

            if(i in offset){
                offset.remove(i)
                i++

                //to loop back in this statement to avoid successive i matches
                continue
            }

            if(types[j].isNamed()){

                var match = false
                val triedNamed = (objects[j] as Named<*>)

                for((flag, int) in named.entries){

                    if(match)
                        break

                    if(flag.flags.contains(triedNamed.name)){

                        if(types[j] != all[int]){
                            return CommandCall(emptyList(), WrongTypeCommandException(e, "wrong type for flag", j, all[int]))
                        }

                        if(e.isFromGuild && !e.member?.hasPermission(flag.permission)!!)
                            return CommandCall(emptyList(), MemberDoesntHavePermissionForFlag(e, "permission denied", j, flag))

                        args[int] = objects[j]
                        offset.add(int)
                        i--
                        match = true
                    }

                }

                if(!match){
                    return CommandCall(emptyList(), FlagDoesNotExistException(e, "flag does not exist", j, triedNamed))
                }

                j++
            } else if(types[j] == all[i]) {
                args[i] = objects[j]
                j++
            } else{
                if(i < ms)
                    return CommandCall(emptyList(), WrongTypeCommandException(e, "argument mismatched", j, all[i]))
            }

            i++

        }

        for(g in globals){
            if(g.first !in usableGlobalFlag){
                return CommandCall(emptyList(), CommandDoestUseGlobalFlag(e, "global flag error", g.second, g.first))
            }
        }

        if(i < ms)
            return CommandCall(emptyList(), IncompleteCommandCallCommandException(e, "not enough arguments", i))

        return  if(j == cs)
                CommandCall(args.toList(), null, globals.map { it.first })
                else CommandCall(emptyList(), TypedCommandException(e, "too much arguments ; do not match signature", -1))
    }

}

/**
 * Transforms a KFunction signature into a [CommandSignature].,
 */
fun <R> KFunction<R>.asCommandSignature(
    types: List<CType<*>>,
    usableGlobalFlag: List<GlobalFlag>,
    functionMap: Map<String, Any>?,
): CommandSignature {

    val mandatory = mutableListOf<CType<*>>()
    val optional = mutableListOf<CType<*>>()
    var optionalMode = false

    if(parameters.isEmpty() || parameters[0].type != TypedCommandCallEvent.TYPE)
        throw MissingFunctionParameter(this,
            "The first parameter must be from type ${TypedCommandCallEvent::class}")

    //By convention, we always put a TypedCommandCallEvent as the first
    //argument of a function

    val named = mutableMapOf<Flag, Int>()

    parameters.subList(1, parameters.size).forEachIndexed { i, p ->

        val parameter = p.type.asCTypeOrNull(types) ?: throw CTypeParameterDoesNotExist(p.type)

        if(p.isOptional)
            optionalMode = true

        if(optionalMode && !p.isOptional){
            throw MalformedFunctionParameters(this, "optional parameter before a mandatory one")
        }

        if(parameter.isNamed()){
            val a = p.findAnnotation<FlowerNamed>()
                ?: throw MalformedFunctionParameters(this, "$p needs to be annotated with FlowerNamed")
            val help = if(a.help == "" && functionMap != null){
                val map = functionMap["flags"] as Map<String, String>?
                if(map != null) map[map.keys.first { it in a.flags}] ?: throw Exception("") else "TODO"
            } else a.help
            named[Flag(a.flags.toList(), a.permission, help)] = i
        }

        if(optionalMode)
            optional.add(parameter)
        else
            mandatory.add(parameter)

    }

    return CommandSignature(parameters, mandatory, optional, named, usableGlobalFlag)
}

