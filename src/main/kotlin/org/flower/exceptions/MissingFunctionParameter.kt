package org.flower.exceptions

import kotlin.reflect.KFunction

class MissingFunctionParameter(function: KFunction<*>, message: String):
    Exception("Missing parameter in function $function: $message") {
}