package com.fansan.dex2oat.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.TimeUtils
import com.fansan.dex2oat.MainViewModel
import com.fansan.dex2oat.SpacerH
import com.fansan.dex2oat.SpacerW
import com.fansan.dex2oat.entity.PackageEntity
import com.fansan.dex2oat.entity.SourceType
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainPage(doAction: () -> Unit) {

    val tabLabels = listOf("用户", "系统")
    var tabIndex by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val pageState = rememberPagerState(initialPage = 0) { 2 }
    val vm = viewModel<MainViewModel>()

    LaunchedEffect(key1 = pageState, block = {
        snapshotFlow { pageState.currentPage }
            .collect {
                tabIndex = it
            }
    })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        MainTitle() {
            doAction()
        }
        if (vm.stateHolder.loadingPackage.value) {
            LoadingPage()
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TabRow(
                        selectedTabIndex = tabIndex,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.secondary,
                        indicator = { tabPositions ->
                            Box(
                                modifier = Modifier
                                    .tabIndicatorOffset(tabPositions[tabIndex])
                                    .height(4.dp)
                                    .padding(horizontal = 12.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(color = MaterialTheme.colorScheme.tertiary)
                            )
                        },
                        divider = {}) {
                        tabLabels.forEachIndexed { index, s ->
                            Tab(
                                selected = index == tabIndex,
                                onClick = {
                                    scope.launch {
                                        tabIndex = index
                                        pageState.animateScrollToPage(index)
                                    }
                                },
                                modifier = Modifier.height(40.dp),
                                selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                                unselectedContentColor = MaterialTheme.colorScheme.onSecondary
                            ) {
                                Text(
                                    text = s,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    HorizontalPager(state = pageState) {
                        MainList(type = it)
                    }
                }

                if (vm.stateHolder.running) {
                    ProcessLayout()
                }
            }
        }
    }
}

@Composable
fun MainTitle(doAction: () -> Unit) {
    val vm = viewModel<MainViewModel>()
    val selectMode = vm.stateHolder.selectMode.value
    Box(
        modifier = Modifier
            .height(60.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Text(
            text = if (vm.stateHolder.running) "编译中..." else if (selectMode) "已选择 ${vm.stateHolder.selecteNum}" else "Dex2oat",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.align(alignment = Alignment.Center)
        )

        if (selectMode && !vm.stateHolder.running) {
            IconButton(
                onClick = { vm.cancelAll() }, modifier = Modifier
                    .padding(start = 12.dp)
                    .size(20.dp)
                    .align(alignment = Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "cancel",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            IconButton(
                onClick = { doAction() }, modifier = Modifier
                    .padding(end = 12.dp)
                    .size(20.dp)
                    .align(alignment = Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = "action",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainList(type: Int) {
    val vm = viewModel<MainViewModel>()
    LazyColumn(
        content = {
            when (type) {
                0 -> {
                    if (vm.userPackageList.isNotEmpty()) {
                        stickyHeader {
                            StickHeaderItem(title = "未编译", selectedAll = vm.userPackageList.count { it.isSelected } == vm.userPackageList.size){
                                vm.scopeSelectAll(type = SourceType.UP)
                            }
                        }


                        itemsIndexed(vm.userPackageList) { index, item ->
                            AppItem(info = item) {
                                vm.selectItem(vm.userPackageList, index = index, !item.isSelected)
                            }

                        }
                    }

                    if (vm.userPackageListCompiled.isNotEmpty()) {
                        stickyHeader {
                            StickHeaderItem(title = "已编译",selectedAll = vm.userPackageListCompiled.count { it.isSelected } == vm.userPackageListCompiled.size){
                                vm.scopeSelectAll(type = SourceType.UPCompiled)
                            }
                        }

                        itemsIndexed(vm.userPackageListCompiled) { index, item ->
                            AppItem(info = item) {
                                vm.selectItem(
                                    vm.userPackageListCompiled,
                                    index = index,
                                    !item.isSelected
                                )
                            }

                        }
                    }
                }

                1 -> {
                    if (vm.systemPackageList.isNotEmpty()) {
                        stickyHeader {
                            StickHeaderItem(title = "未编译",selectedAll = vm.systemPackageList.count { it.isSelected } == vm.systemPackageList.size){
                                vm.scopeSelectAll(type = SourceType.SP)
                            }
                        }

                        itemsIndexed(vm.systemPackageList) { index, item ->
                            AppItem(info = item) {
                                vm.selectItem(vm.systemPackageList, index = index, !item.isSelected)
                            }

                        }
                    }

                    if (vm.systemPackageListCompiled.isNotEmpty()) {
                        stickyHeader {
                            StickHeaderItem(title = "已编译",selectedAll = vm.systemPackageListCompiled.count { it.isSelected } == vm.systemPackageListCompiled.size){
                                vm.scopeSelectAll(type = SourceType.SPCompiled)
                            }
                        }

                        itemsIndexed(vm.systemPackageListCompiled) { index, item ->
                            AppItem(info = item) {
                                vm.selectItem(
                                    vm.systemPackageListCompiled,
                                    index = index,
                                    !item.isSelected
                                )
                            }

                        }
                    }
                }
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.primary),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    )
}

@Composable
fun AppItem(info: PackageEntity, click: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .height(110.dp)
        .background(
            color = if (info.isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondary,
            shape = RoundedCornerShape(15.dp)
        )
        .padding(horizontal = 12.dp, vertical = 8.dp)
        .clickable {
            click()
        }) {
        Row(Modifier.height(intrinsicSize = IntrinsicSize.Min)) {
            Image(
                painter = rememberDrawablePainter(drawable = AppUtils.getAppIcon(info.info.packageName)),
                contentDescription = "icon",
                modifier = Modifier.size(50.dp),
                contentScale = ContentScale.Fit
            )

            SpacerW(width = 10.dp)

            Column(
                modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = AppUtils.getAppName(info.info.packageName),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = info.info.packageName,
                    fontSize = 11.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Row {
                    Text(
                        text = info.info.versionName,
                        fontSize = 11.sp,
                        modifier = Modifier.alignByBaseline(),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    SpacerW(width = 4.dp)
                    Text(
                        text = info.info.longVersionCode.toString(),
                        fontSize = 9.sp,
                        modifier = Modifier.alignByBaseline(),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        SpacerH(height = 8.dp)
        Column {
            Text(
                text = "首次安装日期:${TimeUtils.millis2String(info.info.firstInstallTime)}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSecondary
            )
            SpacerH(height = 4.dp)
            Text(
                text = "更新时间:${TimeUtils.millis2String(info.info.lastUpdateTime)}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSecondary
            )
        }
    }
}

@Composable
private fun StickHeaderItem(title: String,selectedAll:Boolean,click: () -> Unit) {
    Box(
        modifier = Modifier
            .height(50.dp)
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.primary)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(start = 20.dp)
                .align(alignment = Alignment.Center)
        )

        Text(
            text = if (selectedAll) "取消" else "全选",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .align(alignment = Alignment.CenterEnd)
                .padding(end = 12.dp)
                .clickable {
                    click()
                }
        )
    }
}

@Composable
private fun LoadingPage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(50.dp)
        )
    }
}

@Composable
private fun BoxScope.ProcessLayout() {
    val vm = viewModel<MainViewModel>()
    Row(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth()
            .height(50.dp)
            .background(
                color = MaterialTheme.colorScheme.tertiary,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp)
            .align(alignment = Alignment.BottomCenter),
        verticalAlignment = Alignment.CenterVertically
    ) {


        CircularProgressIndicator(
            modifier = Modifier.size(40.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        )

        SpacerW(width = 16.dp)

        Text(
            text = vm.stateHolder.currentProcessName,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 12.sp,
            modifier = Modifier
                .padding(end = 6.dp)
                .weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "${vm.stateHolder.currentProcessIndex + 1}/${vm.stateHolder.processCount}",
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 12.sp
        )
    }
}