package com.sabina.project.base.external.mapper

import io.reactivex.functions.Function
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

abstract class EssentialMapper<T : Any, R> : Function<T, R> {
    private val missedParams = HashSet<String>()

    @Throws(EssentialParamMissingException::class)
    operator fun invoke(raw: T): R = apply(raw)

    @Throws(EssentialParamMissingException::class)
    final override fun apply(raw: T): R {
        missedParams.clear()
        checkMisses(raw)

        if (missedParams.isNotEmpty()) {
            throw EssentialParamMissingException(missedParams, raw)
        }

        return transform(raw)
    }

    protected abstract fun transform(raw: T): R

    private fun checkMisses(raw: T) {
        raw::class.memberProperties.forEach { property ->
            val skip = property.findAnnotation<NotRequired>() != null

            if (!skip) {
                val value = property.getter.call(raw)

                val statuses = ArrayList<String>()

                val additionChecks = globalChecks.asSequence()
                    .filter { it.first.isInstance(value) }
                    .map { it.second }

                val excludedChecks = property.annotations.asSequence()
                    .mapNotNull { it as? ExcludeCheck }
                    .map { it.expressionClass }

                if (value != null) {
                    property.annotations.asSequence()
                        .mapNotNull { it as? Check }
                        .map { it.expressionClass }
                        .plus(additionChecks)
                        .minus(excludedChecks)
                        .map { it.objectInstance ?: it.createInstance() }
                        .map { it(value) }
                        .filter { it.isNotEmpty() }
                        .toCollection(statuses)
                } else {
                    statuses.add("null")
                }

                if (statuses.isNotEmpty()) {
                    missedParams.add("${property.name} $statuses")
                }
            }
        }
    }

    companion object {
        private val globalChecks = ArrayList<Pair<KClass<*>, KClass<out CheckerClass>>>()

        @JvmStatic fun addGlobalCheck(clsCheckPair: Pair<KClass<*>, KClass<out CheckerClass>>): Companion {
            globalChecks.add(clsCheckPair)
            return this
        }

        @JvmStatic fun addGlobalCheck(clsToCheck: KClass<*>, clsWhoCheck: KClass<out CheckerClass>): Companion {
            return addGlobalCheck(clsToCheck to clsWhoCheck)
        }
    }
}