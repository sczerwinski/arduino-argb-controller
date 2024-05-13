package dev.slav.argb.model.messages

/**
 * Parser for the messages sent by microcontrollers.
 */
class MessageParser {

    /**
     * Returns a message parsed from the [messageString].
     *
     * @param messageString A string representation of the message.
     *
     * @return Parsed message.
     */
    fun parseMessage(messageString: String?): Message {
        val items = messageString?.split("\\s+".toRegex())
        val args = items?.drop(n = 1).orEmpty()
        return when (val type = items?.firstOrNull()) {
            null -> NullMessage
            TYPE_INIT -> InitMessage
            TYPE_DONE -> DoneMessage
            TYPE_DATA -> parseDataMessage(args)
            TYPE_SET_DATA -> parseSetDataMessage(args)
            TYPE_ERROR -> ErrorMessage(args.joinToString(separator = " "))
            else -> UnsupportedMessage(type, args)
        }
    }

    private fun parseDataMessage(args: List<String>): Message =
        parseNameValuesMessage(TYPE_DATA, args) { name, values -> DataMessage(name, values) }

    private fun parseNameValuesMessage(
        type: String,
        args: List<String>,
        factory: (String, List<UInt>) -> Message
    ): Message {
        val name = args.firstOrNull()
        val values = args.asSequence().drop(n = 1).parseNumbers()
        return if (name != null) {
            factory(name, values)
        } else {
            UnsupportedMessage(type, args)
        }
    }

    private fun Sequence<String>.parseNumbers(): List<UInt> =
        mapNotNull { arg ->
            try { arg.toUInt() }
            catch (e: NumberFormatException) { null }
        }.toList()

    private fun parseSetDataMessage(args: List<String>): Message =
        parseNameValuesMessage(TYPE_SET_DATA, args) { name, values -> SetDataMessage(name, values) }

    companion object {
        private const val TYPE_INIT = "INIT"
        private const val TYPE_DONE = "DONE"
        private const val TYPE_DATA = "DATA"
        private const val TYPE_SET_DATA = "SET"
        private const val TYPE_ERROR = "ERR"
    }
}
