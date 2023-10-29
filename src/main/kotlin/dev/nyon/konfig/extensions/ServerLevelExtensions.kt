@file:Suppress("unused")
package dev.nyon.konfig.extensions

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.ExperienceOrb
import net.minecraft.world.level.GameRules
import net.minecraft.world.phys.Vec3

fun ServerLevel.dropExperience(pos: BlockPos, amount: Int) {
    if (this.gameRules.getBoolean(GameRules.RULE_DOBLOCKDROPS))
        ExperienceOrb.award(this, Vec3.atCenterOf(pos), amount)
}