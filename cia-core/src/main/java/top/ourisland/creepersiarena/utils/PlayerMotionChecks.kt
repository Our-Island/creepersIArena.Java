package top.ourisland.creepersiarena.utils

import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector

object PlayerMotionChecks {

    private val DOWN = Vector(0, -1, 0)

    @JvmStatic
    fun isServerSideGrounded(entity: Entity): Boolean {
        val box: BoundingBox = entity.boundingBox
        val world: World = entity.world

        val y = box.minY + 0.01
        val maxDistance = 0.08

        val minX = box.minX + 0.03
        val maxX = box.maxX - 0.03
        val minZ = box.minZ + 0.03
        val maxZ = box.maxZ - 0.03
        val midX = (minX + maxX) / 2.0
        val midZ = (minZ + maxZ) / 2.0

        fun hitsGround(
            world: World,
            x: Double,
            y: Double,
            z: Double,
            distance: Double
        ): Boolean = world.rayTraceBlocks(
            Location(world, x, y, z),
            DOWN,
            distance,
            FluidCollisionMode.NEVER,
            true
        ) != null

        return hitsGround(world, minX, y, minZ, maxDistance) ||
                hitsGround(world, minX, y, maxZ, maxDistance) ||
                hitsGround(world, maxX, y, minZ, maxDistance) ||
                hitsGround(world, maxX, y, maxZ, maxDistance) ||
                hitsGround(world, midX, y, midZ, maxDistance)
    }

}
