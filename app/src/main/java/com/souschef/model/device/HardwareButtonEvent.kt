package com.souschef.model.device

/**
 * Physical button presses forwarded by the SousChef dispenser hardware while
 * the user is in cooking mode. Each press maps to a high-level intent that
 * the cooking screen can react to without the user touching the phone.
 */
enum class HardwareButtonEvent {
    /** Move to the previous step. */
    PREVIOUS,
    /** Move to the next step (or finish on the last step). */
    NEXT,
    /** Dispense the current step's ingredient — no-op if not dispensable. */
    DISPENSE
}
