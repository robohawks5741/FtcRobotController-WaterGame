package org.firstinspires.ftc.teamcode

import com.acmerobotics.roadrunner.Vector2d
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sign

/**
 * **I Don't Care** if it's `null`, but I don't want an exception, so I use the `idc` function
 */
inline infix fun <T, I> I.idc(f: () -> T): T? = try { f() } catch (_: Exception) { null }

// n_{i}=1.5
// n_{o}=0.5
// x\left\{0\le x\le1\right\}
// \left(x^{n_{i}}\cdot\left(1-x\right)\right)\ +\ \left(x^{n_{o}}\cdot x\right)\left\{0\le x\le1\right\}
@JvmOverloads
    fun Double.stickCurve(nI: Double = 1.5, nO: Double = 0.5): Double {
    val x = this.absoluteValue
    val s = this.sign
    return (x.pow(nI) * (1 - x)) + (x.pow(nO) * x) * sign
}

/**
 * Naive multiplication (x1 * x2, y1 * y2)
 */
operator fun Vector2d.times(other: Vector2d) = Vector2d(this.x * other.x, this.y * other.y)

fun Boolean.toInt(): Int = if (this) 1 else 0
fun Boolean.toUInt(): UInt = if (this) 1u else 0u
fun Boolean.toLong(): Long = if (this) 1 else 0
fun Boolean.toDouble(): Double = if (this) 1.0 else 0.0
fun Boolean.toFloat(): Float = if (this) 1f else 0f

fun Double.clamp(min: Double, max: Double) = if (this < min) min; else if (this > max) max; else this