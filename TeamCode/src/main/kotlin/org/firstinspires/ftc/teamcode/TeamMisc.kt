/**
 * This file contains a bunch of random functions that don't really have a better place to be.
 * Most of these are extension functions, used in other parts of the code.
 */
@file:JvmName("TeamMisc")
package org.firstinspires.ftc.teamcode

import android.util.Log
import com.acmerobotics.roadrunner.Vector2d
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.HardwareMap
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sign
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.full.hasAnnotation

import org.firstinspires.ftc.teamcode.component.Component

/**
 * **I Don't Care** if it's `null`, but I don't want to throw an exception.
 * @return null if calling [f] produces an exception, or whatever it returned otherwise.
 */
inline infix fun <T, I> I.idc(f: () -> T & Any): T? = try { f() } catch (_: Exception) { null }

/**
 * A nice stick curve for the Logitech controllers that our team primarily uses.
 */
// n_{i}=1.5
// n_{o}=0.5
// x\left\{0\le x\le1\right\}
// \left(x^{n_{i}}\cdot\left(1-x\right)\right)\ +\ \left(x^{n_{o}}\cdot x\right)\left\{0\le x\le1\right\}
@JvmOverloads
fun Double.stickCurve(nI: Double = 1.5, nO: Double = 0.5): Double {
    val x = this.absoluteValue
    return (x.pow(nI) * (1 - x)) + (x.pow(nO) * x) * this.sign
}

/**
 * Naive multiplication (x1 * x2, y1 * y2).
 * This is NOT the cross product or dot product.
 */
operator fun Vector2d.times(other: Vector2d) = Vector2d(this.x * other.x, this.y * other.y)

/**
 * Equivalent to [HardwareMap.tryGet], but this function uses generic parameters
 * instead of requiring a class reference. This is usually much more convenient.
 *
 * This method is also recreated the same way in a few other places.
 * @see [UltraOpMode.getHardware]
 * @see [Component.getHardware]
 */
inline fun <reified T> HardwareMap.search(deviceName: String): T? = this.tryGet(T::class.java, deviceName)

val Boolean.asInt: Int get() = if (this) 1 else 0
val Boolean.asUInt: UInt get() = if (this) 1u else 0u
val Boolean.asLong: Long get() = if (this) 1 else 0
val Boolean.asDouble: Double get() = if (this) 1.0 else 0.0
val Boolean.asFloat: Float get() = if (this) 1.0f else 0.0f

/**
 * Clamps the value between [min] and [max] (inclusive).
 * @return the value, clamped in the range of [[min, max]]
 */
fun Double.clamp(min: Double, max: Double) = if (this < min) min; else if (this > max) max; else this

/**
 * Flips the value of a [Boolean] property.
 *
 * Kotlin allows you to reflect on properties,
 * so this can be helpful for flipping properties with long names.
 *
 * An example:
 * ```kotlin
 * class A {
 *     var superDuperUberUltraMegaLongVariableNameThatIsBooleanInNature = true
 * }
 * fun sample() {
 *     var a = A()
 *     a::superDuperUberUltraMegaLongVariableNameThatIsBooleanInNature.flip()
 * }
 * ```
 * This is a bit of shorthand overkill, but it's useful.
 */
fun KMutableProperty0<Boolean>.flip(): Unit = this.set(!this.get())

/**
 * Prints to the Android log.
 * This is just [Log.i] with a set tag.
 */
fun addieLog(message: String) { Log.i("ADDIE'S INTERNAL", message) }

/**
 * This uses Sloppy heuristics to determine whether or not a given OpMode is running as [TeleOp]
 * or [Autonomous].
 * @return true if [opMode] is [TeleOp], and false if it is [Autonomous].
 */
fun isOpModeTeleOp(opMode: OpMode): Boolean {
    val isTeleOp = when {
        // match annotations
        opMode::class.hasAnnotation<TeleOp>() -> true
        opMode::class.hasAnnotation<Autonomous>() -> false
        // guess based on the class name
        else -> {
            val hasAuto = opMode::class.qualifiedName?.contains("auto", true)
            if (hasAuto == null) true else !hasAuto
        }
    }
//    addieLog("OpMode \"${opMode::class.simpleName}\" is (probably) ${if (isTeleOp) "TeleOp" else "Autonomous"}!")
    return isTeleOp
}