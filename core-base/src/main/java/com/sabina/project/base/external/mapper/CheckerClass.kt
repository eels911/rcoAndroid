package com.sabina.project.base.external.mapper

interface CheckerClass {
    /**
     * If check passed this function must return empty string, otherwise string explanation what is
     * wrong
     */
    operator fun invoke(raw: Any): String
}