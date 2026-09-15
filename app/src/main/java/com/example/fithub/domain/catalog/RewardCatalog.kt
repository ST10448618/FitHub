package com.example.fithub.domain.catalog

/**
 * Static definition of rewards users can redeem with Particles.
 */
data class RewardOffer(
    val id: String,
    val title: String,
    val subtitle: String,
    val costParticles: Int,
    val brandDrawableName: String
)

object RewardCatalog {

    val OFFERS: List<RewardOffer> = listOf(
        RewardOffer(
            id = "bun_n_bite",
            title = "Bun n Bite",
            subtitle = "Meal voucher",
            costParticles = 100,
            brandDrawableName = "brand_bun_n_bite"
        ),
        RewardOffer(
            id = "noosh",
            title = "NOOSH",
            subtitle = "Snack voucher",
            costParticles = 150,
            brandDrawableName = "brand_noosh"
        ),
        RewardOffer(
            id = "planet_fitness",
            title = "Planet Fitness",
            subtitle = "1 month membership",
            costParticles = 100,
            brandDrawableName = "brand_planet_fitness"
        ),
        RewardOffer(
            id = "steam",
            title = "Steam Voucher",
            subtitle = "R100 wallet top-up",
            costParticles = 1000,
            brandDrawableName = "brand_steam"
        )
    )

    /** Reward state for the UI. */
    enum class RewardStatus { AVAILABLE, CLAIMED, INSUFFICIENT }

    /**
     * For the prototype, "claimed" is tracked locally.
     * Add a real claim store when the reward flow goes live.
     */
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