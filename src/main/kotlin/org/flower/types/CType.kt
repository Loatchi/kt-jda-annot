package org.flower.types

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.*
import net.dv8tion.jda.api.requests.restaction.pagination.ThreadChannelPaginationAction
import org.flower.commands.GlobalFlag
import org.flower.commands.TypedCommand
import org.flower.commands.events.CommandCallEvent
import java.awt.Color
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.time.OffsetDateTime
import java.util.*
import java.util.regex.Pattern
import kotlin.reflect.*
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.javaType

/**
 * CType stands for command type. It is used as a
 * way to transform a [String] received from Discord
 * into an object.
 *
 * @constructor [cls] must represent a class with up to one nested generic otherwise,
 *     when creating the kotlin type, matching the class it will "start" the nested generics
 *     providing a low support for an implementation.
 *
 * @author T.Francois
 */
abstract class CType<T: Any>(cls: KClass<T>){

    val cls: KClass<T>
    val jType: KType
    private val isList: Boolean
    private val isNamed: Boolean
    private val innerString: String

    init {
        this.cls = cls
        val p = this::class.java.genericSuperclass as ParameterizedType
        val pp = p.actualTypeArguments

        val innerBuilder = StringBuilder(cls.simpleName)

        var t: Array<Type> = emptyArray()

        if(pp.isNotEmpty()){
            if((pp[0] is ParameterizedType)){
                t = (pp[0] as ParameterizedType).actualTypeArguments
            }
        }
        if(t.isNotEmpty()){
            innerBuilder
                .append("<")
                .append(t.joinToString(separator = ", ") {
                    return@joinToString it.typeName.substringAfterLast(" ").substringAfterLast(".")
                })
                .append(">")
        }

        val innerTypes = t.map {
            KTypeProjection(KVariance.INVARIANT, Class.forName(it.typeName.substringAfterLast(" ")).kotlin.createType())
        }


        // There is only up to one nested generic meaning List<String> => CType<List<String>> but
        // List<List<String>> => CType<List<List<*>>>
        this.jType = cls.createType(arguments = innerTypes)

        isList = cls == List::class
        isNamed = cls == Named::class
        innerString = innerBuilder.toString()
    }


    fun isNamed() = isNamed

    /**
     * Transform one or multiple [String] into a
     * [T]. The [index] is provided as a way to use multiple
     * elements of [CommandCallEvent.args]. You just need to shift
     * the index by more than one element. By default, the command handler
     * doesn't shift the index at all. [interpret] should shift it.
     * If your implementation of [CType] doesn't find a match it shall
     * return null and let the [index] intact.
     *
     * @author T.Francois
     * */
    abstract fun interpret(event: CommandCallEvent, index: Index): T?

    override fun equals(other: Any?) = super.equals(other) || (other is CType<*> && other.jType == jType)
    override fun hashCode() = cls.hashCode()

    override fun toString(): String {
        return "CType<${jType}>"
    }
    fun innerToString(): String{
        return innerString
    }
}

class Named<T : Any>(val name: String, val value: T, val type: CType<T>? = null){

    override fun toString(): String {
        return "Named<$name>=$value"
    }
}

val EMPTY_TEXT_CHANNEL = object : TextChannel{
    override fun getPermissionOverrides(): MutableList<PermissionOverride> {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun putPermissionOverride(p0: IPermissionHolder): PermissionOverrideAction {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun createThreadChannel(p0: String?, p1: Boolean): ThreadChannelAction {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun createThreadChannel(p0: String?, p1: Long): ThreadChannelAction {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun retrieveArchivedPublicThreadChannels(): ThreadChannelPaginationAction {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun retrieveArchivedPrivateThreadChannels(): ThreadChannelPaginationAction {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun retrieveArchivedPrivateJoinedThreadChannels(): ThreadChannelPaginationAction {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun getParentCategoryIdLong(): Long {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun getIdLong(): Long {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun getName(): String {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun getType(): ChannelType {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun getJDA(): JDA {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun delete(): AuditableRestAction<Void> {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun compareTo(other: GuildChannel?): Int {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun getGuild(): Guild {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun getManager(): TextChannelManager {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun getPermissionContainer(): IPermissionContainer {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun getLatestMessageIdLong(): Long {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun canTalk(p0: Member): Boolean {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun removeReactionById(p0: String, p1: String, p2: User): RestAction<Void> {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun deleteMessagesByIds(p0: MutableCollection<String>): RestAction<Void> {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun clearReactionsById(p0: String): RestAction<Void> {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun clearReactionsById(p0: String, p1: String): RestAction<Void> {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun getPermissionOverride(p0: IPermissionHolder): PermissionOverride? {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun isSynced(): Boolean {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun createCopy(p0: Guild): ChannelAction<TextChannel> {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun getMembers(): MutableList<Member> {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun createInvite(): InviteAction {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun retrieveInvites(): RestAction<MutableList<Invite>> {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun getPositionRaw(): Int {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun getTopic(): String? {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun isNSFW(): Boolean {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun retrieveWebhooks(): RestAction<MutableList<Webhook>> {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun createWebhook(p0: String): WebhookAction {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun deleteWebhookById(p0: String): AuditableRestAction<Void> {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }

    override fun getSlowmode(): Int {
        throw Exception("EMPTY_TEXT_CHANNEL is used for optional types in typed command call.")
    }
}
val EMPTY_MEMBER = object : Member{
    override fun getIdLong(): Long {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun getAsMention(): String {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun getGuild(): Guild {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun getPermissions(): EnumSet<Permission> {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun getPermissions(p0: GuildChannel): EnumSet<Permission> {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun getPermissionsExplicit(): EnumSet<Permission> {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun getPermissionsExplicit(p0: GuildChannel): EnumSet<Permission> {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun hasPermission(vararg p0: Permission?): Boolean {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun hasPermission(p0: MutableCollection<Permission>): Boolean {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun hasPermission(p0: GuildChannel, vararg p1: Permission?): Boolean {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun hasPermission(p0: GuildChannel, p1: MutableCollection<Permission>): Boolean {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun canSync(p0: IPermissionContainer, p1: IPermissionContainer): Boolean {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun canSync(p0: IPermissionContainer): Boolean {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun getUser(): User {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun getJDA(): JDA {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun getTimeJoined(): OffsetDateTime {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun hasTimeJoined(): Boolean {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun getTimeBoosted(): OffsetDateTime? {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun isBoosting(): Boolean {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun getTimeOutEnd(): OffsetDateTime? {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun getVoiceState(): GuildVoiceState? {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun getActivities(): MutableList<Activity> {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun getOnlineStatus(): OnlineStatus {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun getOnlineStatus(p0: ClientType): OnlineStatus {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun getActiveClients(): EnumSet<ClientType> {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun getNickname(): String? {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun getEffectiveName(): String {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun getAvatarId(): String? {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun getRoles(): MutableList<Role> {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun getColor(): Color? {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun getColorRaw(): Int {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun canInteract(p0: Member): Boolean {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun canInteract(p0: Role): Boolean {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun canInteract(p0: Emote): Boolean {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun isOwner(): Boolean {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun isPending(): Boolean {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }

    override fun getDefaultChannel(): BaseGuildMessageChannel? {
        throw Exception("EMPTY_MEMBER is used for optional types in typed command call.")
    }
}

/**
 * Provided as a faster way to create a [CType] that doesn't need to provide
 * the class object.
 */
inline fun <reified T: Any> newCType(
    crossinline interpret: (event: CommandCallEvent, index: Index) -> T?,
): CType<T> {

    return object: CType<T>(T::class){
        override fun interpret(event: CommandCallEvent, index: Index): T? {
            return interpret(event, index)
        }
    }
}

fun KType.asCTypeOrNull(types: List<CType<*>>): CType<*>? {
    return types.firstOrNull { it.jType == this}
}

private val textChannelRegex = Pattern.compile("<#(?<ID>\\d*)>")
private val memberRegex = Pattern.compile("<@!(?<ID>\\d*)>")
operator fun <E> List<E>.get(index: Index): E = this[index.index]

/**
 * The integer/long CType. There is no CType<Int> as it would lead
 * to some ambiguity in the interpretation as some String could be
 * both an Int and a Long.
 */
val C_LONG = newCType{ event, index ->
    val a = event.args[index].toLongOrNull()
    if (a != null) index.inc()
    return@newCType a
}

/**
 * The float/double CType. There is no CType<Float> as it would lead
 * to some ambiguity in the interpretation phase as some String could
 * be both Float and Double type.
 */
val C_DOUBLE = newCType { event, index ->
    val a = event.args[index].toDoubleOrNull()
    if (a != null) index.inc()
    return@newCType a
}

/**
 * The CType representing a [Member].
 */
val C_MEMBER = newCType{ event, index ->

    val id = memberRegex.matcher(event.args[index])

    return@newCType when (id.matches()) {
        false -> null
        else -> {
            if (event.isFromGuild) {
                val a = event.guild.getMemberById(id.group("ID"))
                if (a != null) index.inc()
                a
            } else
                null
        }
    }
}

/**
 * The CType representation of a [TextChannel].
 */
val C_TEXT_CHANNEL = newCType { event, index ->

    val id = textChannelRegex.matcher(event.args[index])

    return@newCType when (id.matches()) {
        false -> null
        else -> {
            val a = event.jda.getTextChannelById(id.group("ID"))
            if (a != null) index.inc()
            a
        }
    }
}

/**
 * An arbitrary map of entries used to represent a [Quote].
 */
val openerClosers = listOf(
    "`" to "`",
    "**" to "**",
    "{" to "}",
    "[" to "]",
    "*" to "*",
    "(" to ")",
    "_" to "_",
    "\"" to "\"",
    "'" to "'")

private fun String.opener(): Pair<String, String>? {
    for(opcl in openerClosers){
        if(startsWith(opcl.first))
            return opcl
    }
    return null
}

/**
 * The CType representation of [Quote]. It is provided as a way
 * to bypass the separator.
 */
val C_QUOTE = newCType { event, index ->
    val a = event.args[index]
    var i = index.index
    val opcl = a.opener() ?: return@newCType null

    while (i < event.args.size) {
        if (event.args[i].endsWith(opcl.second) && (event.args[i].length > opcl.second.length || index.index != i)) {
            val out = event.args.subList(index.index, i + 1).joinToString(event.separator)
            index.index = i + 1
            return@newCType Quote(
                out.substring(opcl.first.length, out.length - opcl.second.length),
                opcl.first,
                opcl.second
            )
        }
        i++
    }


    return@newCType null
}

/**
 * The CType representation of a Boolean.
 */
val C_BOOLEAN = newCType { event, index ->
    return@newCType when (event.args[index]) {
        "true" -> {
            index.inc()
            true
        }
        "false" -> {
            index.inc()
            false
        }
        else -> null
    }
}

/**
 * The CType representation of a String. It always returns
 * a non-null value.
 */
val C_STRING = newCType { event, index ->
    event.args[index.inc()]
}

val C_LIST_LONG = newCType<List<Long>> { _, _ -> null }
val C_LIST_DOUBLE = newCType<List<String>> { _, _ -> null }
val C_LIST_BOOLEAN = newCType<List<Boolean>> { _, _ -> null }
val C_LIST_MEMBER = newCType<List<Member>> { _, _ -> null }
val C_LIST_TEXT_CHANNEL = newCType<List<TextChannel>> { _, _ -> null }
val C_LIST_QUOTE = newCType<List<Quote>> { _, _ -> null }
val C_LIST_STRING = newCType<List<String>> { _, _ -> null }

/**
 * A List of all the [CType] builtin.
 * It is properly ordered by parsing priority.
 * ([C_STRING] always return non-null value so putting
 * it first in the list would be pointless)
 */
val C_TYPES = listOf(C_LONG, C_DOUBLE, C_BOOLEAN, C_MEMBER, C_TEXT_CHANNEL, C_QUOTE, C_STRING)

/**
 * A list containing all the variants of the builtin [C_TYPES].
 */
val C_LIST_TYPES = listOf(C_LIST_LONG, C_LIST_DOUBLE, C_LIST_BOOLEAN, C_LIST_MEMBER,
    C_LIST_TEXT_CHANNEL, C_LIST_QUOTE, C_LIST_STRING)

/**
 * A list of all the [CType] builtin with their list variant.
 */
val C_TYPES_WITH_LIST = C_TYPES + C_LIST_TYPES

fun <T : Any> parseCNamedT(type: CType<T>, event: CommandCallEvent, index: Index): Named<T>?{

    if(event.args[index].startsWith("--")){
        if(event.args.size > index.index + 1){
            val start = index.index
            index.inc()
            val result = type.interpret(event, index)
            if(result != null) {
                return Named(event.args[start].substring(2), result, type)
            } else
                index.index--
        }
    }

    return null
}

val C_NAMED_LONG = newCType { event, index ->
    return@newCType parseCNamedT(C_LONG, event, index)
}

val C_NAMED_DOUBLE = newCType { event, index ->
    return@newCType parseCNamedT(C_DOUBLE, event, index)
}

//TODO refactor this spaghetti
val booleanType = Boolean::class.createType()
val C_NAMED_BOOLEAN = newCType { event, index ->

    val command = event.initiator

    if(event.args[index].startsWith("--")){

        for(flag in GlobalFlag.values()){
            if("--${flag.flagName}" == event.args[index]){
                index.inc()
                return@newCType Named(flag.flagName, true, C_BOOLEAN)
            }
        }

        if(event.args.size > index.index + 1){
            val start = index.index
            index.inc()
            val result = C_BOOLEAN.interpret(event, index)
            if(result != null) {
                return@newCType Named(event.args[start].substring(2), result, C_BOOLEAN)
            } else{

                var isBooleanFlag = true
                val name = event.args[start].substring(2)

                for((flag, int) in (command as TypedCommand).commandSignature.named.entries){
                    isBooleanFlag = flag.flags.contains(name)
                            && command.commandSignature.all[int].jType.arguments[0].type == booleanType
                }

                for(flag in GlobalFlag.values()){
                    isBooleanFlag = flag.flagName == name
                }

                if(event.args[start + 1].startsWith("--") || isBooleanFlag)
                    return@newCType Named(name, true, C_BOOLEAN)

                index.index--
            }

        } else{
            return@newCType Named(event.args[index.index++].substring(2), true, C_BOOLEAN)
        }
    }

    return@newCType null

}

val C_NAMED_MEMBER = newCType { event, index ->
    return@newCType parseCNamedT(C_MEMBER, event, index)
}

val C_NAMED_TEXT_CHANNEL = newCType { event, index ->
    return@newCType parseCNamedT(C_TEXT_CHANNEL, event, index)
}

val C_NAMED_QUOTE = newCType { event, index ->
    return@newCType parseCNamedT(C_QUOTE, event, index)
}

val C_NAMED_STRING = newCType { event, index ->
    return@newCType parseCNamedT(C_STRING, event, index)
}

//Boolean become first due to GlobalFlag priority
val C_NAMED_TYPES = listOf(C_NAMED_BOOLEAN, C_NAMED_LONG, C_NAMED_DOUBLE,
    C_NAMED_MEMBER, C_NAMED_TEXT_CHANNEL, C_NAMED_QUOTE, C_NAMED_STRING)

val C_ALL = C_NAMED_TYPES + C_TYPES