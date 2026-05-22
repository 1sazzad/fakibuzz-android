package com.qarena.android.presentation.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.qarena.android.R

@Composable
fun BrandLogo(
    modifier: Modifier = Modifier,
    width: Dp = 160.dp
) {
    Image(
        painter = painterResource(id = R.drawable.q_arena_logo),
        contentDescription = "Q Arena logo",
        modifier = modifier.width(width),
        contentScale = ContentScale.Fit
    )
}
