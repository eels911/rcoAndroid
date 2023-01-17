package com.sabina.project.base.external.mapper

import kotlin.reflect.*
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.withNullability

class SimpleEssentialMapperImpl<T : Any, R : Any>(
    private val sourceClass: KClass<T>,
    private val targetClass: KClass<R>,
//    Converters should be only static KFunction<> or another instance of SimpleEssentialMapper
    vararg val converters: Any
) : EssentialMapper<T, R>() {
    private val mappedProperties = mutableMapOf<String, Pair<Any?, KType>>()

    @Suppress("UNCHECKED_CAST")
    override fun transform(raw: T): R {
        for (prop in raw::class.declaredMemberProperties) {
            var value = (prop as KProperty1<Any?, Any?>).get(raw)
            var propName = prop.name
            for (annotation in prop.annotations) {
                when (annotation) {
                    is TargetName -> {
                        propName = annotation.name
                    }
                    is UseDefaultInt -> {
                        value = value ?: annotation.default
                    }
                    is UseDefaultLong -> {
                        value = value ?: annotation.default
                    }
                    is UseDefaultDouble -> {
                        value = value ?: annotation.default
                    }
                    is UseDefaultString -> {
                        value = value ?: annotation.default
                    }
                    is UseDefaultBoolean -> {
                        value = value ?: annotation.default
                    }
                    is UseDefault -> {
                        value = value ?: getDefault(prop)
                    }
                }
            }
            mappedProperties[propName] = Pair(value, prop.returnType)
        }

        val instance = createInstance()

//       filling up the rest instance fields
        fillUpProperties(instance)

        return instance
    }

    private fun createInstance(): R {
        val constructor = targetClass.constructors.asSequence()
            .sortedByDescending { it.parameters.size }
            .firstOrNull {
                mappedProperties.keys.containsAll(it.parameters.asSequence()
                    .filter { !it.type.isMarkedNullable && !it.isOptional }
                    .map { it.name }
                    .toHashSet()
                )
            }

        constructor ?: throw AppropriateConstructorNotFoundException()

        val constructedParams = mutableListOf<Any?>()

        constructor.parameters.forEach { parameter ->
            val pair = mappedProperties[parameter.name]
            val mappedType = pair!!.second
            val value = pair.first

            if (mappedType == parameter.type || mappedType == parameter.type.withNullability(false)) {
                constructedParams.add(value)
            } else if (mappedType.withNullability(false) == parameter.type) {
                if (value == null) {
                    val function = getAppropriateFunction(mappedType, parameter.type)

                    function ?: throw AppropriateConverterNotFoundException(
                        mappedType.toString(),
                        parameter.type.toString()
                    )
                    constructedParams.add(function.call(value))
                } else {
                    constructedParams.add(value)
                }
            } else {
                val function = getAppropriateFunction(mappedType, parameter.type)

                if (function != null) {
                    constructedParams.add(function.call(value))
                } else {
                    // Check that property is collection which elements we can parse
                    val parameterClass = parameter.type.classifier as KClass<*>
                    val mappedTypeClass = mappedType.classifier as KClass<*>

                    if (parameterClass.isSubclassOf(Iterable::class) && mappedTypeClass.isSubclassOf(
                            Iterable::class
                        )
                    ) {
                        val func = getAppropriateFunction(
                            mappedType.arguments[0].type!!,
                            parameter.type.arguments[0].type!!
                        )

                        func ?: throw AppropriateConverterNotFoundException(
                            mappedType.arguments[0].type.toString(),
                            parameter.type.arguments[0].type.toString()
                        )

                        when (parameterClass) {
                            MutableList::class -> {
                                val list = (value as Iterable<*>).map { func }.toMutableList()
                                constructedParams.add(list)
                            }
                            List::class -> {
                                val list = (value as Iterable<*>).map { func }.toList()
                                constructedParams.add(list)
                            }
                            MutableSet::class -> {
                                val set = (value as Iterable<*>).map { func }.toMutableSet()
                                constructedParams.add(set)
                            }
                            Set::class -> {
                                val set = (value as Iterable<*>).map { func }.toSet()
                                constructedParams.add(set)
                            }
                            else -> throw UnknownCollectionTypeException()
                        }
                    } else {
                        throw AppropriateConverterNotFoundException(
                            mappedType.toString(),
                            parameter.type.toString()
                        )
                    }
                }
            }
        }

        return constructor.call(*constructedParams.toTypedArray())
    }

    private fun fillUpProperties(instance: R) {
        for (property in instance::class.declaredMemberProperties) {
            if (property !is KMutableProperty<*>) continue

            val pair = mappedProperties[property.name]

            pair ?: continue

            val mappedType = pair.second
            val value = pair.first

            if (mappedType == property.returnType || mappedType == property.returnType.withNullability(
                    false
                )
            ) {
                property.setter.call(instance, value)
            } else if (mappedType.withNullability(false) == property.returnType) {
                if (value == null) {
                    val function = getAppropriateFunction(mappedType, property.returnType)

                    function ?: throw AppropriateConverterNotFoundException(
                        mappedType.toString(),
                        property.returnType.toString()
                    )
                    property.setter.call(instance, function.call(value))
                } else {
                    property.setter.call(instance, value)
                }
            } else {
                val function = getAppropriateFunction(mappedType, property.returnType)

                if (function != null) {
                    property.setter.call(instance, function.call(value))
                } else {
                    // Check that property is collection which elements we can parse
                    val parameterClass = property.returnType.classifier as KClass<*>
                    val mappedTypeClass = mappedType.classifier as KClass<*>

                    if (parameterClass.isSubclassOf(Iterable::class) && mappedTypeClass.isSubclassOf(
                            Iterable::class
                        )
                    ) {
                        val func = getAppropriateFunction(
                            mappedType.arguments[0].type!!,
                            property.returnType.arguments[0].type!!
                        )

                        func ?: throw AppropriateConverterNotFoundException(
                            mappedType.arguments[0].type.toString(),
                            property.returnType.arguments[0].type.toString()
                        )

                        when (parameterClass) {
                            MutableList::class -> {
                                val list = (value as Iterable<*>).map { func }.toMutableList()
                                property.setter.call(instance, list)
                            }
                            List::class -> {
                                val list = (value as Iterable<*>).map { func }.toList()
                                property.setter.call(instance, list)
                            }
                            MutableSet::class -> {
                                val set = (value as Iterable<*>).map { func }.toMutableSet()
                                property.setter.call(instance, set)
                            }
                            Set::class -> {
                                val set = (value as Iterable<*>).map { func }.toSet()
                                property.setter.call(instance, set)
                            }
                            else -> throw UnknownCollectionTypeException()
                        }
                    } else {
                        throw AppropriateConverterNotFoundException(
                            mappedType.toString(),
                            property.returnType.toString()
                        )
                    }
                }
            }
        }
    }

    /**
     * Find function with the same parameter and return types in converters
     */
    private fun getAppropriateFunction(parameterType: KType, returnType: KType): KFunction<*>? {
        var converter: KFunction<*>? = null

        converters.forEach {
            when (it) {
                is KFunction<*> -> if ((it.returnType == returnType || it.returnType == returnType.withNullability(
                        false
                    )) &&
                    (it.parameters.first().type == parameterType || it.parameters.first().type.withNullability(
                        false
                    ) == parameterType
                            )
                ) {
                    converter = it
                    return@forEach
                }

                is SimpleEssentialMapperImpl<*, *> -> {
                    if (parameterType.classifier == it.sourceClass && returnType.classifier == it.targetClass) {
                        converter = it::transform
                        return@forEach
                    }
                }

                else -> throw UnknownConverterTypeException()
            }
        }

        return converter
    }
}