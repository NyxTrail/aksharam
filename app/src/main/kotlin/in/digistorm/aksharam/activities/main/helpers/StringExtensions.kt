package `in`.digistorm.aksharam.activities.main.helpers

fun String.upperCaseFirstLetter(): String {
    return this.replaceFirstChar {
        if(it.isLowerCase())
            it.titlecase()
        else
            it.toString()
    }
}