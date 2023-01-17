package com.sabina.project.base.external.mapper

class EssentialParamMissingException(missingParams: Set<String>, rawObject: Any) : RuntimeException(
    "Params are missing in received object.\n" +
            "\tObject -> $rawObject\n" +
            "\tParams -> ${missingParams.joinToString(",\n\t          ")}"
)