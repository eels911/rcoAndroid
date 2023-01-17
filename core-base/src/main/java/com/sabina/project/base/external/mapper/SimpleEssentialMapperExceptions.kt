package com.sabina.project.base.external.mapper

import java.lang.RuntimeException

sealed class SimpleEssentialMapperExceptions(message: String) : RuntimeException(message)
class AppropriateConstructorNotFoundException :
    SimpleEssentialMapperExceptions("Appropriate constructor not found")

class AppropriateConverterNotFoundException(outType: String, inType: String) :
        SimpleEssentialMapperExceptions("Cant find function for converting $outType to $inType. Make sure that you define appropriate static function in converters")

class UnknownConverterTypeException :
        SimpleEssentialMapperExceptions("Converters should be only static KFunction<> or another instance of SimpleEssentialMapper")

class UnknownCollectionTypeException :
        SimpleEssentialMapperExceptions("SimpleEssentialMapperImpl can work only with List, MutableList, Set, MutableSet")

class UnsupportDefaultTypeException(type: String) :
        SimpleEssentialMapperExceptions("Can't find default value for $type")