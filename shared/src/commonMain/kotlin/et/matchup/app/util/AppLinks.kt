package et.matchup.app.util

data class AuthLink(
    val resetToken: String? = null,
    val verificationToken: String? = null,
)

fun parseAuthLink(url: String?): AuthLink? {
    if (url.isNullOrBlank()) return null
    val lower = url.lowercase()
    val isReset = lower.contains("reset-password")
    val isVerify = lower.contains("verify-email") || lower.contains("email-verification")
    if (!isReset && !isVerify) return null
    val token = queryValue(url, "token") ?: fragmentValue(url, "token")
    return if (isReset) AuthLink(resetToken = token) else AuthLink(verificationToken = token)
}

private fun queryValue(url: String, key: String): String? {
    val query = url.substringAfter('?', "").substringBefore('#')
    return query.split('&')
        .mapNotNull { part ->
            val idx = part.indexOf('=')
            if (idx <= 0) null else percentDecode(part.substring(0, idx)) to percentDecode(part.substring(idx + 1))
        }
        .firstOrNull { it.first == key }
        ?.second
}

private fun fragmentValue(url: String, key: String): String? {
    val fragment = url.substringAfter('#', "")
    return fragment.split('&')
        .mapNotNull { part ->
            val idx = part.indexOf('=')
            if (idx <= 0) null else percentDecode(part.substring(0, idx)) to percentDecode(part.substring(idx + 1))
        }
        .firstOrNull { it.first == key }
        ?.second
}

private fun percentDecode(value: String): String {
    val bytes = ArrayList<Byte>(value.length)
    var i = 0
    while (i < value.length) {
        val ch = value[i]
        if (ch == '+') {
            bytes.add(' '.code.toByte())
            i++
        } else if (ch == '%' && i + 2 < value.length) {
            val hex = value.substring(i + 1, i + 3)
            val byte = hex.toIntOrNull(16)
            if (byte != null) {
                bytes.add(byte.toByte())
                i += 3
            } else {
                bytes.add(ch.code.toByte())
                i++
            }
        } else {
            val encoded = ch.toString().encodeToByteArray()
            encoded.forEach(bytes::add)
            i++
        }
    }
    return bytes.toByteArray().decodeToString()
}
