package com.ismartcoding.plain.ui.page

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ismartcoding.plain.R
import com.ismartcoding.plain.helpers.ShareHelper
import com.ismartcoding.plain.ui.base.ActionButtons
import com.ismartcoding.plain.ui.base.BottomSpace
import com.ismartcoding.plain.ui.base.IconTextShareButton
import com.ismartcoding.plain.ui.base.IconTextToTopButton
import com.ismartcoding.plain.ui.base.PCard
import com.ismartcoding.plain.ui.base.IconTextToBottomButton
import com.ismartcoding.plain.ui.base.PListItem
import com.ismartcoding.plain.ui.base.PModalBottomSheet
import com.ismartcoding.plain.ui.base.PSwitch
import com.ismartcoding.plain.ui.base.VerticalSpace
import com.ismartcoding.plain.ui.models.TextFileViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ViewTextContentBottomSheet(
    viewModel: TextFileViewModel,
    content: String,
) {
    val context = LocalContext.current
    val onDismiss = {
        viewModel.showMoreActions.value = false
    }

    PModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
    ) {
        LazyColumn {
            item {
                VerticalSpace(32.dp)
            }
            item {
                ActionButtons {
                    IconTextShareButton {
                        ShareHelper.shareText(context, content)
                        onDismiss()
                    }
                    IconTextToTopButton {
                        viewModel.gotoTop()
                        onDismiss()
                    }
                    IconTextToBottomButton {
                        viewModel.gotoEnd()
                        onDismiss()
                    }
                }
            }
            item {
                VerticalSpace(dp = 24.dp)
                PCard {
                    PListItem(title = stringResource(id = R.string.wrap_content), action = {
                        PSwitch(
                            activated = viewModel.wrapContent.value,
                        ) {
                            viewModel.toggleWrapContent(context)
                        }
                    })
                }
            }

            item {
                BottomSpace()
            }
        }
    }
}


