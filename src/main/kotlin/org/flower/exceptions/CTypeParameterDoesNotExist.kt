package org.flower.exceptions

import kotlin.reflect.KType

class CTypeParameterDoesNotExist(type: KType): Exception("type: $type does not have a CType representation")