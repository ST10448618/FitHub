package com.example.fithub.ui.screens.plans

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.R
import com.example.fithub.core.WorkoutImageMapper
import com.example.fithub.ui.components.*
import com.example.fithub.ui.theme.*
import com.example.fithub.ui.util.localized

@Composable
fun WorkoutListScreen(
    onBack: () -> Unit,
    onPlanClick: (String) -> Unit,
    viewModel: WorkoutListViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        AppHeader(
            title = stringResource(R.string.plans_workout_plans_title),
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        stringResource(
                            R.string.workout_list_search_format,
                            state.category.localized()
                        )
                    )
                },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = { Icon(Icons.Filled.FilterList, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                stringResource(R.string.workout_list_found_format, state.items.size),
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )

            Spacer(Modifier.height(8.dp))

            when {
                state.isLoading -> LoadingState()
                state.items.isEmpty() -> EmptyState(
                    title = stringResource(R.string.workout_list_empty_title),
                    message = stringResource(R.string.workout_list_empty_body),
                    emoji = "🔍"
                )
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(state.items, key = { it.id }) { plan ->
                        WorkoutCardRow(
                            plan = plan,
                            onClick = { onPlanClick(plan.id) },
                            localImageRes = WorkoutImageMapper.drawableRes(context, plan.id)
                        )
                    }
                }
            }
        }
    }
}