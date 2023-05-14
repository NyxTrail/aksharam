package `in`.digistorm.aksharam.util

fun String.containsOneOf(charList: List<String>): Boolean {
    for(char in charList)
        if(this.contains(char))
            return true
    return false
}