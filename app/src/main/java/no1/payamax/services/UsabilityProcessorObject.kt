package no1.payamax.services

val UsabilityProcessorObject = UsabilityProcessorEngine(
    listOf(
        LandlineOriginUsabilityProcessor(),
        ThousandPrefixedOriginUsabilityProcessor(),
        PhonebookOriginUsabilityProcessor(),
        TitledOriginUsabilityProcessor(),
        PersonalOriginUsabilityProcessor(),
        OperatorOriginUsabilityProcessor(),
        EmojiUsabilityProcessor(),
        Cancel11UsabilityProcessor(),
        CodeUsabilityProcessor(),
    )
)