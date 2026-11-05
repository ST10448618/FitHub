package com.example.fithub.ui.screens.nutrition.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource          // ← ADDED
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.R                            // ← ADDED
import com.example.fithub.domain.model.MealType
import com.example.fithub.ui.components.*
import com.example.fithub.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun NutritionGoalsScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: NutritionGoalsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        AppHeader(title = stringResource(R.string.nutrition_goals_title), onBack = onBack)

        if (state.isLoading) {
            LoadingState()
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ---- Your Details ----
            RoundedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.nutrition_goals_your_details),
                    style = MaterialTheme.typography.titleMedium,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(10.dp))
                DetailLine(stringResource(R.string.nutrition_goals_age_label), state.age.toString())
                DetailLine(
                    stringResource(R.string.nutrition_goals_current_weight_label),
                    stringResource(R.string.nutrition_goals_kg_format, state.currentWeightKg.toInt()),
                    inline2 = stringResource(R.string.nutrition_goals_height_label),
                    value2 = stringResource(R.string.nutrition_goals_cm_format, state.heightCm.toInt())
                )
                DetailLine(stringResource(R.string.nutrition_goals_lifestyle_label), state.activityLabel)
                DetailLine(
                    stringResource(R.string.nutrition_goals_target_weight_label),
                    state.targetWeightKg?.let {
                        stringResource(R.string.nutrition_goals_kg_format, it.toInt())
                    } ?: stringResource(R.string.common_dash)
                )
            }

            Spacer(Modifier.height(16.dp))

            // ---- Nutrition Management ----
            Text(
                stringResource(R.string.nutrition_goals_management_title),
                style = MaterialTheme.typography.titleLarge,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(12.dp))

            // Recommendation banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            stringResource(R.string.nutrition_goals_recommendation_engine),
                            style = MaterialTheme.typography.labelMedium,
                            color = ErrorRed,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            stringResource(
                                R.string.nutrition_goals_recommendation_value,
                                state.recommendedDailyCalories
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Daily Calorie Intake Goal ----
            RoundedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.nutrition_goals_daily_calorie_goal),
                    style = MaterialTheme.typography.titleMedium,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(12.dp))

                IntStepper(
                    value = state.userDailyCalories,
                    onChange = viewModel::setUserDailyCalories,
                    step = 50,
                    range = 1000..6000,
                    suffix = " " + stringResource(R.string.nutrition_goals_kcal_suffix)
                )

                Spacer(Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        stringResource(
                            R.string.nutrition_goals_recommended_value,
                            state.recommendedDailyCalories
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        stringResource(
                            R.string.nutrition_goals_current_value,
                            state.userDailyCalories
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = FitHubPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(12.dp))

                SmallActionButton(
                    text = stringResource(R.string.nutrition_goals_use_recommendation),
                    onClick = viewModel::acceptRecommendation
                )
            }

            Spacer(Modifier.height(16.dp))

            // ---- Meal Targets ----
            RoundedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.nutrition_goals_meal_targets_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.nutrition_goals_meal_targets_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(12.dp))

                MealTargetRow(
                    emoji = "🍳",
                    label = stringResource(R.string.enum_meal_breakfast),
                    color = MealBreakfast,
                    value = state.breakfastKcal,
                    onChange = { viewModel.setMealCalories(MealType.BREAKFAST, it) }
                )
                MealTargetRow(
                    emoji = "🥗",
                    label = stringResource(R.string.enum_meal_lunch),
                    color = MealLunch,
                    value = state.lunchKcal,
                    onChange = { viewModel.setMealCalories(MealType.LUNCH, it) }
                )
                MealTargetRow(
                    emoji = "🍽",
                    label = stringResource(R.string.enum_meal_dinner),
                    color = MealDinner,
                    value = state.dinnerKcal,
                    onChange = { viewModel.setMealCalories(MealType.DINNER, it) }
                )
                MealTargetRow(
                    emoji = "🍓",
                    label = stringResource(R.string.enum_meal_snack),
                    color = MealSnack,
                    value = state.snackKcal,
                    onChange = { viewModel.setMealCalories(MealType.SNACK, it) }
                )

                Spacer(Modifier.height(12.dp))
                Divider(color = SurfaceGray)
                Spacer(Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        stringResource(R.string.nutrition_goals_total_label),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(
                            R.string.nutrition_goals_total_format,
                            state.mealAllocated,
                            state.userDailyCalories
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        color = if (state.mealValid) SuccessGreen else ErrorRed,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (!state.mealValid) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.nutrition_goals_meal_invalid_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = ErrorRed
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Macro Targets ----
            RoundedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.nutrition_goals_macro_targets_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.nutrition_goals_macro_targets_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(12.dp))

                MacroSliderRow(
                    label = stringResource(R.string.food_details_macro_protein),
                    color = MacroProtein,
                    value = state.proteinG, range = 0..400,
                    onChange = viewModel::setProtein
                )
                MacroSliderRow(
                    label = stringResource(R.string.food_details_macro_carbs),
                    color = MacroCarbs,
                    value = state.carbsG, range = 0..700,
                    onChange = viewModel::setCarbs
                )
                MacroSliderRow(
                    label = stringResource(R.string.food_details_macro_fats),
                    color = MacroFats,
                    value = state.fatG, range = 0..250,
                    onChange = viewModel::setFat
                )

                Spacer(Modifier.height(8.dp))
                SmallActionButton(
                    text = stringResource(R.string.nutrition_goals_reset_macros),
                    onClick = viewModel::recalcMacrosFromCalories
                )
            }

            if (state.errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(state.errorMessage!!, color = ErrorRed)
            }

            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton(
                    text = stringResource(R.string.generic_cancel),
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    text = if (state.isSaving) stringResource(R.string.nutrition_goals_saving)
                    else stringResource(R.string.nutrition_goals_confirm),
                    onClick = viewModel::save,
                    enabled = !state.isSaving && state.mealValid,
                    modifier = Modifier.weight(1.3f)
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ---------- Helper composables ----------

@Composable
private fun DetailLine(label: String, value: String, inline2: String? = null, value2: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        if (inline2 != null && value2 != null) {
            Spacer(Modifier.weight(1f))
            Text(inline2, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Text(
                value2,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun IntStepper(
    value: Int,
    onChange: (Int) -> Unit,
    step: Int,
    range: IntRange,
    suffix: String = ""
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledIconButton(
            onClick = { onChange((value - step).coerceIn(range.first, range.last)) },
            modifier = Modifier.size(38.dp)
        ) { Text("−", fontWeight = FontWeight.Bold) }
        Spacer(Modifier.width(12.dp))
        Text(
            "$value$suffix",
            style = MaterialTheme.typography.headlineMedium,
            color = FitHubPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.width(12.dp))
        FilledIconButton(
            onClick = { onChange((value + step).coerceIn(range.first, range.last)) },
            modifier = Modifier.size(38.dp)
        ) { Text("+", fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun MealTargetRow(
    emoji: String,
    label: String,
    color: Color,
    value: Int,
    onChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.width(10.dp))
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        FilledIconButton(
            onClick = { onChange((value - 50).coerceAtLeast(0)) },
            modifier = Modifier.size(30.dp)
        ) { Text("−") }
        Text(
            stringResource(R.string.nutrition_goals_kcal_format, value),
            style = MaterialTheme.typography.titleSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        FilledIconButton(
            onClick = { onChange(value + 50) },
            modifier = Modifier.size(30.dp)
        ) { Text("+") }
    }
}

@Composable
private fun MacroSliderRow(
    label: String,
    color: Color,
    value: Int,
    range: IntRange,
    onChange: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                stringResource(R.string.food_details_grams_format, value),
                style = MaterialTheme.typography.titleSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.roundToInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = color.copy(alpha = 0.25f)
            )
        )
    }
}