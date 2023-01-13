package `in`.digistorm.aksharam.activities.main

import `in`.digistorm.aksharam.R
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AksharamTopBar(modifier: Modifier = Modifier) {
    TopAppBar(
        title = { AksharamTitle(modifier) },
        modifier = modifier
    )
}

@Composable
fun AksharamTitle(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(id = R.string.app_name),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}