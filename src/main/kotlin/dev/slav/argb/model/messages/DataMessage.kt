package dev.slav.argb.model.messages

/**
 * A message containing microcontroller data.
 */
data class DataMessage(
    /**
     * Name of the data.
     */
    val name: String,

    /**
     * Data values.
     */
    val values: List<UInt>
) : Message
