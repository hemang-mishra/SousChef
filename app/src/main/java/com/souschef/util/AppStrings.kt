package com.souschef.util

import com.souschef.model.recipe.SupportedLanguages

/**
 * Single source of truth for all static UI strings that must react to the
 * app's in-app language switch (managed by [LanguageManager]).
 *
 * **Why not `strings.xml` / `values-hi/`?**
 * The app uses a custom in-app language switcher that does NOT change the
 * Android system locale. `Context.getString(R.string.*)` always reads the
 * device locale, so `values-hi/strings.xml` would have no effect here.
 * Each function takes the active [language] code and returns the correct copy,
 * mirroring the pattern used in `RecipeStep.instructionIn(language)`.
 *
 * To add a new language: add a new `else if` branch (or a `when` arm) in
 * each function and update [SupportedLanguages].
 */
object AppStrings {

    // ── Cooking Mode — Navigation & Header ─────────────────────────────────

    fun step(language: String, current: Int, total: Int): String =
        if (language == SupportedLanguages.HINDI) "चरण $current / $total"
        else "Step $current of $total"

    fun previousStep(language: String): String =
        if (language == SupportedLanguages.HINDI) "← पिछला" else "← Previous"

    fun nextStep(language: String): String =
        if (language == SupportedLanguages.HINDI) "अगला चरण →" else "Next Step →"

    fun finishCooking(language: String): String =
        if (language == SupportedLanguages.HINDI) "पकाना पूरा 🎉" else "Finish Cooking 🎉"

    // ── Cooking Mode — Ingredient Card ─────────────────────────────────────

    fun ingredient(language: String): String =
        if (language == SupportedLanguages.HINDI) "सामग्री" else "Ingredient"

    fun notLoaded(language: String): String =
        if (language == SupportedLanguages.HINDI) "लोड नहीं" else "Not Loaded"

    fun dispense(language: String): String =
        if (language == SupportedLanguages.HINDI) "डिस्पेंस" else "Dispense"

    // ── Cooking Mode — Visual Cue ──────────────────────────────────────────

    fun lookFor(language: String): String =
        if (language == SupportedLanguages.HINDI) "देखें: " else "Look for: "

    // ── Cooking Mode — Timer ───────────────────────────────────────────────

    fun timer(language: String): String =
        if (language == SupportedLanguages.HINDI) "टाइमर" else "Timer"

    fun timesUp(language: String): String =
        if (language == SupportedLanguages.HINDI) "⏰ समय पूरा!" else "⏰ Time's up!"

    fun timerStart(language: String): String =
        if (language == SupportedLanguages.HINDI) "शुरू" else "Start"

    fun timerPause(language: String): String =
        if (language == SupportedLanguages.HINDI) "रुकें" else "Pause"

    fun timerReset(language: String): String =
        if (language == SupportedLanguages.HINDI) "रीसेट" else "Reset"

    // ── Cooking Complete Screen ────────────────────────────────────────────

    fun greatWork(language: String): String =
        if (language == SupportedLanguages.HINDI) "शानदार! 🎉" else "Great work! 🎉"

    fun dishReady(language: String): String =
        if (language == SupportedLanguages.HINDI)
            "आपका व्यंजन तैयार है। बोन अपेती!"
        else
            "Your dish is ready to be enjoyed. Bon appétit!"

    fun backToRecipe(language: String): String =
        if (language == SupportedLanguages.HINDI) "रेसिपी पर वापस" else "Back to Recipe"

    fun shareRecipe(language: String): String =
        if (language == SupportedLanguages.HINDI) "रेसिपी शेयर करें" else "Share Recipe"

    // ── Recipe Overview — General ──────────────────────────────────────────

    fun recipeOverview(language: String): String =
        if (language == SupportedLanguages.HINDI) "रेसिपी विवरण" else "Recipe Overview"

    fun startCooking(language: String): String =
        if (language == SupportedLanguages.HINDI) "पकाना शुरू करें" else "Start Cooking"

    fun byCreator(language: String, name: String): String =
        if (language == SupportedLanguages.HINDI) "द्वारा $name" else "by $name"

    // ── Recipe Overview — Ingredients Sections ─────────────────────────────

    fun autoDispensable(language: String): String =
        if (language == SupportedLanguages.HINDI) "स्वतः-डिस्पेंस" else "Auto-Dispensable"

    fun autoDispensableDesc(language: String): String =
        if (language == SupportedLanguages.HINDI)
            "ये सामग्रियाँ आपके SousChef डिस्पेंसर द्वारा स्वतः संचालित होंगी।"
        else
            "These ingredients will be automatically handled by your SousChef dispenser."

    fun manualIngredients(language: String): String =
        if (language == SupportedLanguages.HINDI) "मैनुअल सामग्री" else "Manual Ingredients"

    fun manualIngredientsDesc(language: String): String =
        if (language == SupportedLanguages.HINDI)
            "सर्विंग साइज़ बदलते ही मात्राएँ अपडेट हो जाएँगी।"
        else
            "Quantities update instantly with serving size changes."

    // ── Recipe Overview — Serving Selector ────────────────────────────────

    fun servingSelectorTitle(language: String): String =
        if (language == SupportedLanguages.HINDI) "कितने लोगों के लिए बना रहे हैं?" else "How many are you cooking for?"

    fun servesRange(language: String, min: Int, max: Int): String =
        if (language == SupportedLanguages.HINDI) "$min–$max लोगों के लिए" else "Serves $min–$max"

    // ── Recipe Overview — Flavor Customization ────────────────────────────

    fun flavorCustomization(language: String): String =
        if (language == SupportedLanguages.HINDI) "स्वाद अनुकूलन" else "Flavor customization"

    fun spice(language: String): String =
        if (language == SupportedLanguages.HINDI) "मसाला" else "Spice"

    fun salt(language: String): String =
        if (language == SupportedLanguages.HINDI) "नमक" else "Salt"

    fun sweetness(language: String): String =
        if (language == SupportedLanguages.HINDI) "मिठास" else "Sweetness"

    fun less(language: String): String =
        if (language == SupportedLanguages.HINDI) "कम" else "Less"

    fun original(language: String): String =
        if (language == SupportedLanguages.HINDI) "मूल" else "Original"

    fun more(language: String): String =
        if (language == SupportedLanguages.HINDI) "अधिक" else "More"

    fun extraSpicy(language: String): String =
        if (language == SupportedLanguages.HINDI) "बहुत मसालेदार!" else "Extra Spicy!"

    fun veryMild(language: String): String =
        if (language == SupportedLanguages.HINDI) "बहुत हल्का!" else "Very Mild!"

    fun extraSalty(language: String): String =
        if (language == SupportedLanguages.HINDI) "बहुत नमकीन!" else "Extra Salty!"

    fun veryLowSalt(language: String): String =
        if (language == SupportedLanguages.HINDI) "बहुत कम नमक!" else "Very Low Salt!"

    fun extraSweet(language: String): String =
        if (language == SupportedLanguages.HINDI) "बहुत मीठा!" else "Extra Sweet!"

    fun barelySweet(language: String): String =
        if (language == SupportedLanguages.HINDI) "थोड़ा मीठा!" else "Barely Sweet!"

    // ── Recipe Overview — Menus & Dialogs ────────────────────────────────

    fun editRecipe(language: String): String =
        if (language == SupportedLanguages.HINDI) "रेसिपी संपादित करें" else "Edit Recipe"

    fun deleteRecipe(language: String): String =
        if (language == SupportedLanguages.HINDI) "रेसिपी हटाएँ" else "Delete Recipe"

    fun deleteRecipeConfirmTitle(language: String): String =
        if (language == SupportedLanguages.HINDI) "रेसिपी हटाएँ?" else "Delete Recipe"

    fun deleteRecipeConfirmMessage(language: String): String =
        if (language == SupportedLanguages.HINDI)
            "क्या आप सुनिश्चित हैं कि आप इस रेसिपी को हटाना चाहते हैं? यह क्रिया पूर्ववत नहीं की जा सकती।"
        else
            "Are you sure you want to delete this recipe? This action cannot be undone."

    fun delete(language: String): String =
        if (language == SupportedLanguages.HINDI) "हटाएँ" else "Delete"

    fun cancel(language: String): String =
        if (language == SupportedLanguages.HINDI) "रद्द करें" else "Cancel"
}
