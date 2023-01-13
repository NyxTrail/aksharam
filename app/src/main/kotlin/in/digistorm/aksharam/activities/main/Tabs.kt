package `in`.digistorm.aksharam.activities.main

import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.themeadapter.material3.Mdc3Theme

@Composable
fun AksharamTabRow(
    tabs: List<String>,
    setPagerIndicator: @Composable (tabPositions: List<TabPosition>) -> Unit,
    modifier: Modifier = Modifier,
    setState: (id: Int) -> Unit = {},
    selectedTabIndex: Int = 0,
) {
    Mdc3Theme {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            indicator = setPagerIndicator,
            tabs = {
                tabs.forEachIndexed { index, tabName ->
                    AksharamTab(
                        selected = index == selectedTabIndex,
                        onClick = { setState(index) },
                        tabName = tabName,
                    )
                }
            },
            modifier = modifier
        )
    }
}

@Composable
fun AksharamTab(
    tabName: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    selected: Boolean = false
) {
    Tab(
        onClick = onClick,
        modifier = modifier,
        selected = selected
    ) {
        Text(text = tabName)
    }
}

@Preview
@Composable
fun TabsPreview() {
    val previewList = listOf("Letters", "Transliterate", "Practice")
    AksharamTabRow(
        tabs = previewList,
        setState = {},
        setPagerIndicator = {}
    )
}