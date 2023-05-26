# What is Aksharam?

Aksharam is an an android application for users who know the script for an Indic language and want 
to use that existing knowledge to learn the script for another Indic language.

Aksharam lays out the alphabets of each language in a format that is easy to understand and refer.
The app comes with a feature to practice the letters in a new language by entering the corresponding
transliterated characters. Finally, the app also has a small transliteration utility to quickly
enter some text and see how it transliterates to a language of your choice.

# What languages are supported?

Currently Kannada, Hindi and Malayalam are supported.

For the future, I would like to support as many languages as possible.
Tamil and Telugu are the immediate next ones I would like to add.

# Disclaimer

Note, I am not a student of languages and while I have tried my best to be as accurate as possible 
in the data presented in the app, it is quite possible that errors might have crept in.

Please feel free to let me know in case of any oversight/incorrectness and I will try my best to 
address them as soon as possible.

# Methodology for Transliteration

The app just matches the characters based on the Unicode blocks of a language. Note that in many cases,
how a glyphs for a character is transliterated may not directly correspond to the pronunciation of 
that character in the source language. In all such cases, the app gives precedence to what the character
matches to in the Unicode block than to the exact pronunciation.

In cases where such pronunciation discrepancies turn up, I have tried to add some helpful notes
under the details of that letter (long press a letter to see its details).

# How to use the app

## Download this app

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/in.digistorm.aksharam/)
[<img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png"
     alt="Get it on Google Play"
     height="80">](https://play.google.com/store/apps/details?id=in.digistorm.aksharam)

## Downloading Data Files

When the app is first launched, you are given the option to download the data file for a
language of your choice. Later, if you decide to check out other languages, you can manage the
data files in the Settings screen.

## Letters Tab

In the letters tab of the app, you can choose the language you wish to learn from the "
Language" drop down. The app will now display all the characters of the selected language.
From the "Convert To" drop down, you can choose a language you know. Now, you can tap lightly
on a character to see what it looks like in the second language you chose (the one you know).
Tap longer (touch and hold) on a character to see more detailed information about that
character.

## Transliteration Tab

Here, you can copy in text in one of the languages for which you have downloaded data files
for and transliterate it into a different language of your choice. Note, the app tries to preserve
and use the corresponding letter of the second language where possible (even if the a letter is
pronounced different in that language). The idea here is to help the user recognise the letter in
the source script.

## Practice Tab

To use the practice tab effectively, it is recommended to install a keyboard that supports
at least two languages - one that you wish to learn and another that you know. I recommend the
"Indic Keyboard" by "Indic Project" available in the [Play Store](https://play.google.com/store/apps/details?id=org.smc.inputmethod.indic)
and [F-Droid](https://f-droid.org/en/packages/org.smc.inputmethod.indic/).
In the practice tab, choose a script you wish to learn and a script that you know. Next you
can choose a suitable practice type and test your knowledge by entering characters in the script
that you already know.
