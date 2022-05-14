package org.flower.commands

import net.dv8tion.jda.api.Permission

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class FlowerNamed(val flags: Array<String>, val help: String = "", val permission: Permission = Permission.MESSAGE_SEND) {}
