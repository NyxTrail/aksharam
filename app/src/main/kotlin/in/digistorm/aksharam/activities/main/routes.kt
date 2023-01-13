package `in`.digistorm.aksharam.activities.main

interface Route {
    val route: String
}

object LettersRoute: Route {
    override val route = "letters"
}

object TransliterateRoute: Route {
    override val route = "transliterate"
}

object PracticeRoute: Route {
    override val route = "practice"
}