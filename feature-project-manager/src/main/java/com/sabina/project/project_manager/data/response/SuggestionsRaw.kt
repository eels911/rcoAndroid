package com.sabina.project.project_manager.data.response

import com.google.gson.annotations.SerializedName
import com.sabina.project.base.external.mapper.EssentialMapper
import com.sabina.project.base.external.mapper.essentialMap
import com.sabina.project.project_manager.domain.model.AddressSuggestions
import javax.inject.Inject

internal data class SuggestionsRaw(
    @SerializedName("suggestions") val suggestions: List<AddressSuggestionRaw> = listOf(),
) {
    class MapperToSuggestions @Inject constructor(
        private val mapperToAddressSuggestion: AddressSuggestionRaw.MapperToAddressSuggestion,
    ) : EssentialMapper<SuggestionsRaw, AddressSuggestions>() {
        override fun transform(raw: SuggestionsRaw): AddressSuggestions {
            return AddressSuggestions(
                raw.suggestions.essentialMap(mapperToAddressSuggestion),
            )
        }
    }
}