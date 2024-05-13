package dev.slav.argb.model.commands

/**
 * Read command envelope.
 */
object ReadCommand : Command(TYPE_READ) {

    /**
     * Returns `null`.
     *
     * @return `null`.
     */
    override fun dataToString(): String? = null
}
