package org.flower.exceptions

import kotlin.reflect.KFunction
import kotlin.reflect.KClass

class MissingAnnotationOnFunctionException(annot: KClass<out Annotation>, function: KFunction<*>) :
    Exception("Missing $annot on function $function")
