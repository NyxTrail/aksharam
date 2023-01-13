package `in`.digistorm.aksharam.activities.main

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel

class AksharamViewModel(
    val tabs: List<String>,
): ViewModel() {
    // Select first tab by default
    var tabState by mutableStateOf(0)
}