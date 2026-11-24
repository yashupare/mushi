package com.mushi.app.modules.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mushi.app.core.ui.misc.SkeletonBar
import com.mushi.app.core.ui.misc.SkeletonTree
import com.mushi.app.core.ui.misc.shimmerApply

/** The whole Home page in outline: chips, quick picks, the hero card and two shelves. */
@Composable
internal fun HomeSkeleton(modifier: Modifier = Modifier) {
    SkeletonTree(isLoading = true, modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            ChipsSkeleton()
            QuickPicksSkeleton()
            Box(
                Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .shimmerApply())
            ShelfSkeletonContent()
        }
    }
}

/** Below the chips, while another genre loads: the quick picks and a shelf. */
@Composable
internal fun HomeBodySkeleton(modifier: Modifier = Modifier) {
    SkeletonTree(isLoading = true, modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            QuickPicksSkeleton()
            ShelfSkeletonContent()
        }
    }
}

/** One shelf: a title and a row of cards. Used while the next page of shelves loads. */
@Composable
internal fun ShelfSkeleton(modifier: Modifier = Modifier) {
    SkeletonTree(isLoading = true, modifier = modifier.fillMaxWidth()) {
        ShelfSkeletonContent()
    }
}

@Composable
private fun ChipsSkeleton() {
    Row(
        Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        listOf(48, 72, 90, 96, 80).forEach { width ->
            SkeletonBar(Modifier.width(width.dp), height = 36.dp, shape = RoundedCornerShape(18.dp))
        }
    }
}

@Composable
private fun QuickPicksSkeleton() {
    Column(
        Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(4) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(2) {
                    Box(
                        Modifier
                            .weight(1f)
                            .height(60.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .shimmerApply())
                }
            }
        }
    }
}

@Composable
private fun ShelfSkeletonContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SkeletonBar(
            Modifier.padding(horizontal = 16.dp).width(180.dp),
            height = 20.dp,
            shape = RoundedCornerShape(8.dp))
        Row(
            Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(3) {
                Column(Modifier.width(140.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .shimmerApply())
                    SkeletonBar(Modifier.fillMaxWidth(0.75f), height = 13.dp)
                    SkeletonBar(Modifier.fillMaxWidth(0.45f), height = 11.dp)
                }
            }
        }
    }
}
