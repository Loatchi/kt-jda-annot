# kt-jda-annot
An implementation of JDA that transform kotlin functions directly into typed discord commands.

It is still in progress altough already useable. The code is a bit messy ; it is meant to be refactored by anyone wanting
to use it. 

kt-jda-annot gives you the ability to transform a kotlin function into a typed command with various features.

###Usage

```kotlin

@FlowerCommand(id = "PING_ID") //supposing you created a json-file that you will give when creating discord commands
fun ping(event: TypedCommandCallEvent, @FlowerNamed(["nb_of_messages"]) number: Named<Int> = Named("", 1) ){

    // we know for sure that number.value is a valid candidate, not need to verify

    for(unused in 0 until number.value){
        event.message.reply("pong !").queue() 
    }

}

@FlowerArgumentsPredicate(id = "PING_ID")
fun predicate(event: TypedCommandCallEvent, number: Named<Int> = Named("", 1)){
  val success = number.value in 1..3
  
  if(!success){
    event.message.reply("number: `${number.value}` out of bounds [0; 3]").queue()
  }
  
  return success
}

```

The above create a command with signature \[Named\<Int\>\]: on discord it will mean that this will call it: (with f% as prefix)
  `f%ping` or `f%ping --nb_of_messages 2`
  
If using a default exception handler, you can send a message that `--nb_of_messages` has a wrong type:

```kotlin
@FlowerException(id = "NULL")
fun exception(event: TypedCommandCallEvent, error: CommandException){
    event.message.reply(error.toString()).queue()
}
```

For example: if `f%ping --nb_of_messages "quote_here"` is sent then error will be a WrongTypeCommandException which will create a message looking like:
```
ping
Error: wrong type for flag on parameter #0 - [/!\LAZY EVAL /!\]
=> f%ping --nb_of_messages "quote_here"
TYPE: Named<Quote>
EXPECTED: Named<Int>
```
As the bot tells us, it does a lazy evalutation meaning it will catch the first error encountered.
You can modify those behaviors, for example the messages by changing a few lines in CommandException file.

You can get the functions by calling `getAllFunctionCommandWithinPackage`:
```kotlin
val functions = getAllFunctionCommandWithinPackage("org.flower", jda, USED_C_TYPES, ::exception, COMMANDS_DATA)
// COMMANDS_DATA is a map containing each command id and their data
// ::exception is the default exception function called if none is provided with FlowerException and a corresponding id
// USED_C_TYPES is the used command types
// jda is the classic JDA instance
```
The library obviously let you create types that does not exist by default.
For example let's say you have a class `Potato` and you want that a command can use the type `Potato` in its signature.
You can change USED_C_TYPES by adding AT THE BEGGINING an instance of CType<Potato>. It could be like:
  
```kotlin
val C_POTATO = newCType{ event, index ->
    val isPotato = event.args[index] == "potato"
    if (isPotato){
        index.inc() // increments the indice of event by one, you can increase by more if you want (C_QUOTE for example)
        return@newCType Potato()
    } else {
       return@newCType null // null means that event.args[index] is not a potato and no increment is needed
    } 
}
```
  
If you also want to add a C_NAMED_POTATO you can use `parseCNamedT` to directly use C_POTATO. The named version is the version with 
a flag, if you want to be able to do `--random_name potato`.

C_POTATO does not need to increment the index of the arguments because, each argument is a String and C_STRING will always add one to the index.
Therefore if you put C_POTATO at the end of USED_C_TYPES, C_POTATO will never be parsed.
    
Although it is hard to understand at first, if time is taken to understand the project deeper, it is easily maintainable. It can be used to create huge bots, with no effort, for typing commands arguments. It can help you directly use a tool to do a bot.
    
See example:
    [huge](https://github.com/Loatchi/kt-jda-annot/blob/master/src/example/kotlin/org/flower/example/Huge.kt):

![alt text](https://github.com/Loatchi/kt-jda-annot/blob/master/example.png)
