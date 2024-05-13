package dev.slav.argb.model.messages

/**
 * A message containing information about the value set in a microcontroller.
 */
data class SetDataMessage(
    /**
     * Name of the data.
     */
    val name: String,

    /**
     * Data values.
     */
    val values: List<UInt>
) : Message
