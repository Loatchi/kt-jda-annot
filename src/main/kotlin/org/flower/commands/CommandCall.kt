package org.flower.commands

class CommandCall(
    val args: List<Any?>,
    val exception: TypedCommandException?,
    val globalFlags: List<GlobalFlag> = emptyList()
    ) {

}
