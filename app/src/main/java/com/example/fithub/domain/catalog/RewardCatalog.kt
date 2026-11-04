package com.example.fithub.domain.catalog

import androidx.annotation.StringRes
import com.example.fithub.R

/**
 * Static definition of rewards users can redeem with Particles.
 *
 * `title`/`subtitle` remain the canonical English strings used by the firestore
 * sync path (which sends the raw reward title when a claim happens). The
 * `titleRes`/`subtitleRes` fields are the future-ready localisation hooks —
 * consumers that display to the user should read those, and any future log or
 * sync should send `title`. This keeps option 1 (full @StringRes migration)
 * available with a single-line change in `RewardsScreen.kt`.
 */
data class RewardOffer(
    val id: String,
    val title: String,
    val subtitle: String,
    val costParticles: Int,
    val brandDrawableName: String,
    @param:StringRes val titleRes: Int,
    @param:StringRes val subtitleRes: Int
)

object RewardCatalog {

    val OFFERS: List<RewardOffer> = listOf(
        RewardOffer(
            id = "bun_n_bite",
            title = "Bun n Bite",
            subtitle = "Meal voucher",
            costParticles = 100,
            brandDrawableName = "brand_bun_n_bite",
            titleRes = R.string.reward_bun_n_bite_title,
            subtitleRes = R.string.reward_bun_n_bite_subtitle
        ),
        RewardOffer(
            id = "noosh",
            title = "NOOSH",
            subtitle = "Snack voucher",
            costParticles = 150,
            brandDrawableName = "brand_noosh",
            titleRes = R.string.reward_noosh_title,
            subtitleRes = R.string.reward_noosh_subtitle
        ),
        RewardOffer(
            id = "planet_fitness",
            title = "Planet Fitness",
            subtitle = "1 month membership",
            costParticles = 100,
            brandDrawableName = "brand_planet_fitness",
            titleRes = R.string.reward_planet_fitness_title,
            subtitleRes = R.string.reward_planet_fitness_subtitle
        ),
        RewardOffer(
            id = "steam",
            title = "Steam Voucher",
            subtitle = "R100 wallet top-up",
            costParticles = 1000,
            brandDrawableName = "brand_steam",
            titleRes = R.string.reward_steam_title,
            subtitleRes = R.string.reward_steam_subtitle
        )
    )

    /** Reward state for the UI. */
    enum class RewardStatus { AVAILABLE, CLAIMED, INSUFFICIENT }

    fun statusFor(
        offer: RewardOffer,
        currentBalance: Int,
        claimedIds: Set<String>
    ): RewardStatus = when {
        offer.id in claimedIds -> RewardStatus.CLAIMED
        currentBalance >= offer.costParticles -> RewardStatus.AVAILABLE
        else -> RewardStatus.INSUFFICIENT
    }
}