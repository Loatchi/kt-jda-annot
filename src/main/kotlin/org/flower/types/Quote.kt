package org.flower.types

data class Quote(var message: String, val opener: String, val closer: String){
    fun toStringForDiscord(): String {
        val builder = StringBuilder()
        builder.append("{content=$message, opener=")

        for(c in opener)
            builder.append("\\$c")

        builder.append(", closer=")

        for(c in closer)
            builder.append("\\$c")

        return builder.append("}").toString()
    }
}

val EMPTY_QUOTE = Quote("", "", "")
