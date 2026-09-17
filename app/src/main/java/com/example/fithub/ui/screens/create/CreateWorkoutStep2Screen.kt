package com.example.fithub.ui.screens.plans.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.domain.model.Exercise
import com.example.fithub.domain.model.ExerciseCategory
import com.example.fithub.ui.components.*
import com.example.fithub.ui.theme.*

@Composable
fun CreateWorkoutStep2Screen(
    onBack: () -> Unit,
    onNext: () -> Unit,
    viewModel: CreateWorkoutViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CreateWorkoutScaffold(
        stepNumber = 2,
        title = "Exercise Selection",
        subtitle = "Use the Session Builder to find the exercises you desire and add them to your plan!",
        onCancel = {
            viewModel.cancelAndReset()
            onBack()
        },
        onNext = onNext,
        nextEnabled = state.draft.exercises.isNotEmpty()
    ) {
        Text(
            "Session Builder",
            style = MaterialTheme.typography.titleMedium,
            color = FitHubPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(10.dp))

        // Search
        OutlinedTextField(
            value = state.exerciseSearchQuery,
            onValueChange = viewModel::onExerciseSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search by Name") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(12.dp))

        // Category chips
        Text(
            "Categories",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))

        CategoryGrid(
            selected = state.selectedCategory,
            onSelect = viewModel::selectCategory
        )

        Spacer(Modifier.height(14.dp))

        Text(
            "Exercises",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))

        if (state.filteredExercises.isEmpty()) {
            Text(
                "No exercises match.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            ExerciseSelectionGrid(
                exercises = state.filteredExercises,
                isAdded = viewModel::isExerciseAdded,
                onAdd = viewModel::addExercise
            )
        }

        Spacer(Modifier.height(20.dp))

        // Your Workout
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Your Workout",
                style = MaterialTheme.typography.titleMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                "${state.draft.exercises.size} exercises currently added",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        Spacer(Modifier.height(10.dp))

        if (state.draft.exercises.isEmpty()) {
            Text(
                "Add exercises to build your workout.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        } else {
            state.draft.exercises.forEachIndexed { idx, ex ->
                YourWorkoutRow(
                    index = idx + 1,
                    exerciseName = ex.exerciseName,
                    category = ex.category.name.lowercase()
                        .replaceFirstChar { it.uppercase() },
                    onRemove = { viewModel.removeExercise(ex.exerciseId) }
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CategoryGrid(
    selected: ExerciseCategory,
    onSelect: (ExerciseCategory) -> Unit
) {
    val categories = ExerciseCategory.entries

    // Simple wrap-style row of chips (2 rows)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        categories.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { cat ->
                    ChipToggle(
                        label = cat.name.lowercase().replaceFirstChar { it.uppercase() },
                        selected = cat == selected,
                        onClick = { onSelect(cat) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // fill remaining slots with spacer for consistent widths
                repeat(4 - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ChipToggle(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) FitHubPrimary else CardWhite)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) TextOnPrimary else FitHubPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ExerciseSelectionGrid(
    exercises: List<Exercise>,
    isAdded: (String) -> Boolean,
    onAdd: (Exercise) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        exercises.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { ex ->
                    ExerciseCard(
                        exercise = ex,
                        added = isAdded(ex.id),
                        onAdd = { onAdd(ex) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ExerciseCard(
    exercise: Exercise,
    added: Boolean,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardWhite)
            .border(
                width = if (added) 1.5.dp else 1.dp,
                color = if (added) FitHubPrimary else SurfaceGray,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = !added, onClick = onAdd)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                exercise.name,
                style = MaterialTheme.typography.titleSmall,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                "Category : ${exercise.category.name.lowercase()
                    .replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (added) FitHubPrimary else FitHubLightBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (added) Icons.Filled.Check else Icons.Filled.Add,
                contentDescription = null,
                tint = if (added) Color.White else FitHubPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun YourWorkoutRow(
    index: Int,
    exerciseName: String,
    category: String,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardWhite)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(FitHubLightBlue),
            contentAlignment = Alignment.Center
        ) {
            Text(
                index.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                exerciseName,
                style = MaterialTheme.typography.titleSmall,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Category : $category",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
        IconButton(onClick = onRemove) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Remove",
                tint = ErrorRed
            )
        }
    }
}