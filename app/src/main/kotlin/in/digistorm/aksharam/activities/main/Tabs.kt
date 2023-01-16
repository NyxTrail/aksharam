package `in`.digistorm.aksharam.activities.main

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.themeadapter.material3.Mdc3Theme

@Composable
fun AksharamTabRow(
    tabs: List<String>,
    setPagerIndicator: @Composable (tabPositions: List<TabPosition>) -> Unit,
    modifier: Modifier = Modifier,
    tabOnClick : (id: Int) -> Unit = {},
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
                        onClick = { tabOnClick(index) },
                        modifier = Modifier
                            .paddingFromBaseline(bottom = 10.dp, top = 5.dp),
                        tabName = tabName,
                    )
                }
            },
            modifier = modifier.height(30.dp)
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
        tabOnClick = {},
        setPagerIndicator = {}
    )
}