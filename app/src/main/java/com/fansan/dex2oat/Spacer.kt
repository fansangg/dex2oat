package com.fansan.dex2oat

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 *@author  fansan
 *@version 2022/11/27
 */

@Composable
fun SpacerH(height: Dp, modifier: Modifier = Modifier) =
	Spacer(modifier = modifier
		.height(height))

@Composable
fun SpacerW(width: Dp, modifier: Modifier = Modifier) =
	Spacer(modifier = modifier
		.width(width))