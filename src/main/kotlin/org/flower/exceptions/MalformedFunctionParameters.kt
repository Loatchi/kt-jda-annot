package org.flower.exceptions

import kotlin.reflect.KFunction

class MalformedFunctionParameters(function: KFunction<*>, message: String):
    Exception("$function is malformed: $message") {
}