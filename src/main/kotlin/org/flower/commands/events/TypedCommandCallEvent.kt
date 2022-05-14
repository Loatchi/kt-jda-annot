package org.flower.commands.events

import org.flower.commands.Command
import org.flower.commands.GlobalFlag
import org.flower.types.*
import kotlin.reflect.full.createType
import kotlin.reflect.typeOf

/**
 * The class representing a typed [CommandCallEvent].
 *
 * @author T.Francois
 */
class TypedCommandCallEvent(private val event: CommandCallEvent, val types: List<CType<*>>, autoInit: Boolean = true): CommandCallEvent(event) {

    val globalsActivated = mutableListOf<GlobalFlag>()

    companion object{
        val TYPE = typeOf<TypedCommandCallEvent>()
    }

    inner class TypedArgument<T : Any>(val obj: T,
                                       val clazz: CType<*>,
                                       val raw: String,
                                       val start: Int,
                                       val end: Int
    ){
        override fun toString(): String {
            return "($obj of $clazz)"
        }
    }

    fun asSubCommandCall(newInitiator: Command?): CommandCallEvent {
        val t = TypedCommandCallEvent(super.asSubCommandCall(newInitiator, 1, args.size), types, false)
        t.typedArgs = typedArgs.subList(1, typedArgs.size)
        return t
    }

    private fun createTypedArgs(): MutableList<TypedArgument<*>> {

        val index = Index(0)
        val typedArgs = mutableListOf<TypedArgument<*>>()

        while(index.index < args.size){

            for(type in types){
                val start = index.index
                val o = type.interpret(this, index)
                val end = index.index
                if(o != null){
                    typedArgs.add(TypedArgument(o, type, this.args.subList(start, end).joinToString(separator), start, end))
                    break
                }
            }
        }

        return typedArgs
    }

    override fun toString(): String {
        return "TypedCommandCall(rawArgs=$args; typedArgs=$typedArgs)"
    }

    fun removeEmptyString() {
        val iter = typedArgs.iterator()
        while(iter.hasNext()){
            val t = iter.next()
            if(t.raw == "")
                iter.remove()
        }
    }

    var typedArgs: MutableList<TypedArgument<*>> = if (autoInit) createTypedArgs() else mutableListOf()
}