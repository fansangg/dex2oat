package com.fansan.dex2oat.ui

import android.app.Activity
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.TimeUtils
import com.fansan.dex2oat.Dex2oatDatabase
import com.fansan.dex2oat.MainViewModel
import com.fansan.dex2oat.SpacerH
import com.fansan.dex2oat.SpacerW
import com.fansan.dex2oat.entity.PackageEntity
import com.fansan.dex2oat.ui.theme.Typography
import com.google.accompanist.drawablepainter.rememberDrawablePainter


@Composable
fun MainPage() {

	val vm = viewModel<MainViewModel>()
	val context = LocalContext.current

	Column(
		modifier = Modifier
			.fillMaxSize()
			.statusBarsPadding()
	) {
		MainTitle()
		OperationView()
		if (vm.stateHolder.loadingPackage.value) {
			LoadingPage()
		} else {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(color = MaterialTheme.colorScheme.primary)
			) {
				MainList(vm.currentShowList.toList())
				if (vm.stateHolder.running) {
					ProcessLayout()
				}

				if (vm.currentShowList.any { it.isSelected }) {
					FloatingActionButton(
						onClick = {
							vm.checkPower(context as Activity)
						},
						shape = RoundedCornerShape(18.dp),
						modifier = Modifier
							.align(alignment = Alignment.BottomEnd)
							.padding(end = 18.dp, bottom = 18.dp),
						containerColor = MaterialTheme.colorScheme.secondaryContainer
					) {
						Row(
							modifier = Modifier
								.wrapContentSize()
								.padding(8.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							Icon(imageVector = Icons.Default.Send, contentDescription = "action")
							SpacerW(width = 12.dp)
							Text(text = "编译${vm.currentShowList.count { it.isSelected }}个应用")
						}
					}
				}
			}
		}
	}

	LaunchedEffect(key1 = Unit, block = {
		vm.getAllPackageInfo()
	})
}

@Composable
fun MainTitle() {
	Box(
		modifier = Modifier
			.height(60.dp)
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.primary)
	) {
		Text(
			text = "Dex2oat",
			fontSize = 14.sp,
			color = MaterialTheme.colorScheme.onPrimary,
			modifier = Modifier.align(alignment = Alignment.Center)
		)
	}
}

@Composable
private fun OperationView() {
	val vm = viewModel(modelClass = MainViewModel::class.java)
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(50.dp)
			.background(color = MaterialTheme.colorScheme.tertiary)
			.padding(horizontal = 8.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.SpaceBetween
	) {
		Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
			OperationItem(label = "已编译",
			              isChecked = vm.stateHolder.checkCompiled,
			              checkChanged = {
				              vm.stateHolder.checkCompiled = it
				              vm.updateList()
			              })

			OperationItem(label = "系统应用",
			              isChecked = vm.stateHolder.checkSystemApp,
			              checkChanged = {
				              vm.stateHolder.checkSystemApp = it
				              vm.updateList()
			              })
		}

		OperationItem(label = "全选",
		              isChecked = vm.currentShowList.all { it.isSelected },
		              checkChanged = {
			              vm.toggleSelectAll()
		              })

	}
}

@Composable
private fun OperationItem(label: String, isChecked: Boolean, checkChanged: (Boolean) -> Unit) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(8.dp)
	) {
		Checkbox(
			checked = isChecked,
			onCheckedChange = checkChanged,
			colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primaryContainer),
			modifier = Modifier.size(20.dp)
		)
		Text(
			text = label,
			style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onPrimary)
		)
	}
}

@Composable
fun MainList(list: List<PackageEntity>) {
	val vm = viewModel<MainViewModel>()

	LazyColumn(
		content = {
			items(list) {
				AppItem(info = it) {
					vm.currentShowList = vm.currentShowList.map { entity ->
						if (it.info.packageName == entity.info.packageName) entity.copy(isSelected = !entity.isSelected) else entity
					}.toSet()
				}
			}
		},
		modifier = Modifier
			.fillMaxSize()
			.background(color = MaterialTheme.colorScheme.primary),
		verticalArrangement = Arrangement.spacedBy(8.dp),
		contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 4.dp)
	)
}

@Composable
fun AppItem(info: PackageEntity, click: () -> Unit) {
	Column(modifier = Modifier
		.fillMaxWidth()
		.wrapContentHeight()
		.background(
			color = if (info.isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary,
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
				text = "更新时间：${TimeUtils.millis2String(info.info.lastUpdateTime)}",
				style = Typography.labelMedium.copy(color = MaterialTheme.colorScheme.onPrimary)
			)
			SpacerH(height = 6.dp)
			val dbEntity = Dex2oatDatabase.getDb().packageInfoDao().getEntity(info.info.packageName)
			val isCompiled = dbEntity.isCompiled
			Text(
				text = if (isCompiled) "已编译" else "未编译",
				style = MaterialTheme.typography.labelMedium.copy(color = if (isCompiled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer)
			)
			if (isCompiled) {
				Text(text = "编译时间：${TimeUtils.millis2String(dbEntity.modifyTime)}")
			}
		}
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
			color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(50.dp)
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
				color = MaterialTheme.colorScheme.tertiary, shape = RoundedCornerShape(12.dp)
			)
			.padding(horizontal = 12.dp)
			.align(alignment = Alignment.BottomCenter),
		verticalAlignment = Alignment.CenterVertically
	) {


		CircularProgressIndicator(
			modifier = Modifier.size(40.dp), color = MaterialTheme.colorScheme.primaryContainer
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