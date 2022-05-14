package org.flower.types

import net.dv8tion.jda.api.JDABuilder
import kotlin.system.exitProcess

class Index(var index: Int) {

    fun inc(): Index {
        return Index(index++)
    }

}