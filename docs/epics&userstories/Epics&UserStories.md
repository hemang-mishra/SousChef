# **Epic 1: Authentication & Role Management**

## **Epic Description**

The system will provide user authentication through registration and login functionality.

There will be two major roles in the system:

* **User**
* **Admin**

Any registered user can:

* Browse recipes
* Act as a chef (create recipes)

There will be **no restriction during registration** regarding being a chef or a normal user.

However:

* A user can receive a **“Verified Chef” tag**
* The **Admin** controls verification from the admin side

---

Now I’ll break this Epic into User Stories strictly based on what you described.

---

# **User Stories – Epic 1**

### **US 1.1 – User Registration**

As a new user,
I want to register an account,
So that I can access the application.

---

### **US 1.2 – User Login**

As a registered user,
I want to log into my account,
So that I can access my profile and use the app features.

---

### **US 1.3 – Default User Role Assignment**

As a system,
I want every registered account to be assigned the role "User" by default,
So that role-based access can be managed properly.

---

### **US 1.4 – Chef Functionality Access**

As a registered user,
I want to be able to create recipes without selecting a special “chef” role during registration,
So that anyone can act as a chef.

---

### **US 1.5 – Admin Role**

As an admin,
I want to have administrative privileges,
So that I can manage user verification.

---

### **US 1.6 – Chef Verification**

As an admin,
I want to assign a "Verified Chef" tag to a user,
So that verified chefs can be distinguished from other users.



# **Epic 2: Advanced Recipe Modeling & Intelligent Customization**

---

## **Epic Description**

The system shall provide a structured and complete recipe model where:

* Recipes are divided into the smallest possible executable steps.
* No step assumes prior knowledge.
* Each step contains sufficient detail for complete execution.
* Before cooking begins, users must see an overview screen.
* Users can customize:

    * Number of servings
    * Spice level
    * Salt preference
    * Sweetness preference
* Ingredient quantities must automatically recalculate based on selected preferences.
* Recipes are created for a base serving size defined by the creator.
* The system stores ingredient quantities in a normalized format.
* Recipe creators can restrict allowable serving size ranges.

---

# **Core System Design (Internal Logic)**

---

## 1️⃣ Recipe Structure Model

Each recipe shall contain:

* Title
* Description
* Base serving size (e.g., 4 people)
* Ingredient list
* Step list
* Serving size restriction (optional min-max range)
* Flavor profile attributes derived from ingredients

---

## 2️⃣ Ingredient Model

Each ingredient shall store:

* Ingredient name
* Quantity (for base serving size)
* Unit (grams, tsp, etc.)
* Derived per-person quantity (system calculated)
* Flavor attributes:

    * `spice_intensity_value`
    * `sweetness_value`
    * `saltiness_value`

These values are numerical weights used for recalculation logic.

---

## 3️⃣ Serving Size Logic

### Base Rule

When a creator defines:

* Base serving size = 4
* Ingredient quantity = 4 tomatoes

System internally stores:

```
per_person_quantity = ingredient_quantity / base_serving_size
```

When user selects:

```
selected_servings = 3
```

System calculates:

```
final_quantity = per_person_quantity × selected_servings
```

---

### Serving Size Restriction

Recipe creator may define:

* Minimum serving size
* Maximum serving size
* Recommended range (e.g., 2–6 servings)

System shall:

* Restrict user selection outside allowed range.
* Display recommended serving range in overview screen.

---

## 4️⃣ Flavor Preference Customization Logic

User can adjust:

* Spice level
* Saltiness level
* Sweetness level

Each ingredient contributes to one or more flavor dimensions using stored weight values.

### Example Ingredient Flavor Mapping

| Ingredient | Spicy | Sweet | Salty |
| ---------- | ----- | ----- | ----- |
| Salt       | 0     | 0     | 10    |
| Sugar      | 0     | 10    | 0     |
| Red Chili  | 8     | 0     | 0     |
| Onion      | 1     | 2     | 0     |

---

### Adjustment Logic

When a user increases a specific preference:

* System identifies ingredients contributing to that flavor dimension.
* Ingredient quantities are proportionally scaled based on:

    * Their flavor intensity value.
    * The degree of user adjustment.

This ensures:

* Only relevant ingredients are modified.
* Flavor balance remains controlled.
* Multi-dimensional ingredients are handled mathematically.

---

## 5️⃣ Overview Screen (Pre-Cooking Configuration Layer)

Before cooking begins, user sees:

* Recipe summary
* Base serving size
* Customizable serving selector
* Spice preference selector
* Saltiness preference selector
* Sweetness preference selector
* Dynamically updated ingredient quantities

All ingredient recalculations occur in real time before cooking starts.

---

## 6️⃣ Step Model (Micro-Step Structure)

Each recipe step shall include:

* Instruction text (fully explicit, no assumed knowledge)
* Optional timer
* Flame level indicator (low / medium / high)
* Expected visual cue (e.g., “until golden brown”)
* Optional media reference (image or video)

Steps must:

* Be atomic.
* Represent the smallest executable cooking action.
* Avoid assumptions about prior preparation.

---

# User Stories – Epic 2

---

### **US 2.1 – Create Structured Recipe**

As a recipe creator,
I want to create a recipe with a defined base serving size,
So that ingredient scaling can be calculated properly.

---

### **US 2.2 – Add Ingredient with Flavor Attributes**

As a recipe creator,
I want to add ingredients with quantity and associated flavor attributes,
So that customization can be calculated automatically.

---

### **US 2.3 – Define Serving Size Restriction**

As a recipe creator,
I want to restrict minimum and maximum serving sizes,
So that recipe integrity is maintained.

---

### **US 2.4 – Add Micro-Level Steps**

As a recipe creator,
I want to add detailed, atomic steps including timers, flame levels, and visual cues,
So that users can follow instructions without assumptions.

---

### **US 2.5 – View Recipe Overview**

As a user,
I want to view an overview screen before cooking,
So that I can understand and customize the recipe.

---

### **US 2.6 – Customize Serving Size**

As a user,
I want to adjust the number of servings,
So that ingredient quantities update automatically.

---

### **US 2.7 – Customize Flavor Preferences**

As a user,
I want to adjust spice, saltiness, and sweetness preferences,
So that the ingredient quantities adapt accordingly.

---

### **US 2.8 – Dynamic Ingredient Recalculation**

As a system,
I want to recalculate ingredient quantities dynamically based on serving size and flavor preferences,
So that the recipe adapts intelligently.




# **Epic 3: Hardware Integration & Smart Spice Dispensing**

## **Epic Description**

This Epic focuses on integrating the application with a spice dispensing hardware device that assists users during cooking by dispensing the correct amount of spices at the appropriate step in a recipe.

The device contains **six compartments**, numbered **1 through 6**, where users manually store spices. Each compartment contains a specific ingredient selected by the user.

Since the hardware device supports only **powder-format ingredients**, the system must store whether an ingredient is **dispensable** or not.

During cooking:

* If a recipe step requires a dispensable ingredient,
* The application checks whether that ingredient is available in any device compartment.
* If available, the application sends a command to the device to dispense the required quantity automatically.

If the ingredient is **not available in the device**, the step will simply display the instruction to **manually add the ingredient**, without mentioning device availability.

The system also maintains a **dispense log** and tracks the quantity of spices remaining in each compartment. Users can view the current spice levels, update compartment contents, and receive **refill alerts** when quantities become low.

---

# **Core System Design (Internal Logic)**

## 1️⃣ Ingredient Dispensability

Each ingredient shall store whether it can be dispensed by the hardware device.

Example attribute:

```
is_dispensable = true / false
```

This ensures that only compatible ingredients are considered for automated dispensing.

---

## 2️⃣ Device Compartment Model

The device contains **six compartments**.

Each compartment shall store:

* Compartment ID (1–6)
* Ingredient stored in the compartment
* Current spice quantity
* Maximum capacity
* Last refill timestamp

Users manually assign spices to compartments.

---

## 3️⃣ Ingredient Mapping to Device Compartments

Before dispensing, the system checks:

1. Whether the ingredient in the step is dispensable.
2. Whether the ingredient exists in any of the device compartments.
3. Whether sufficient quantity is available.

If all conditions are satisfied, the system triggers the device to dispense the ingredient.

---

## 4️⃣ Automatic Dispense During Recipe Steps

When a cooking step requires a dispensable ingredient:

1. The system identifies the ingredient and required quantity.
2. It checks whether the ingredient exists in any device compartment.
3. If available, the application sends a dispense command to the device.
4. The device dispenses the specified quantity.
5. The system updates the compartment inventory and logs the event.

If the ingredient is not present in the device:

* The step simply displays the instruction for the user to add the ingredient manually.

---

## 5️⃣ Dispense Log

The system maintains a record of every dispensing event.

Each log entry stores:

* Timestamp
* Recipe ID
* Step ID
* Ingredient dispensed
* Quantity dispensed
* Compartment ID
* User ID

This log helps track spice usage and maintain accurate inventory levels.

---

## 6️⃣ Spice Quantity Tracking

Each compartment maintains the current quantity of spice available.

Whenever a dispensing action occurs:

```
remaining_quantity = current_quantity - dispensed_amount
```

The system updates the quantity after each dispense event.

---

## 7️⃣ Refill Alerts

The application monitors spice levels within compartments.

When the quantity falls below a predefined threshold, the system notifies the user that the compartment requires refilling.

Users can manually refill compartments and update the stored quantity in the application.

---

## 8️⃣ Compartment Management

Users must be able to:

* Assign an ingredient to a compartment
* Replace the ingredient stored in a compartment
* Update the quantity after refilling
* View the current ingredient and quantity in each compartment

---

# **User Stories – Epic 3**

---

### **US 3.1 – Mark Ingredient as Dispensable**

As a system,
I want ingredients to store whether they are dispensable by the hardware device,
So that only compatible ingredients can be automatically dispensed.

---

### **US 3.2 – Configure Device Compartments**

As a user,
I want to assign ingredients to the six device compartments,
So that the system knows which spices are available for dispensing.

---

### **US 3.3 – Track Spice Quantity**

As a system,
I want to track the quantity of spice stored in each compartment,
So that dispensing actions can update inventory accurately.

---

### **US 3.4 – Automatic Spice Dispensing**

As a user,
I want the device to automatically dispense spices during recipe steps when available,
So that cooking becomes easier and more precise.

---

### **US 3.5 – Manual Addition When Spice Not Available**

As a user,
I want the step instructions to simply tell me to add the ingredient manually if it is not available in the device,
So that the cooking flow remains uninterrupted.

---

### **US 3.6 – Maintain Dispense Log**

As a system,
I want to record every spice dispensing action in a log,
So that spice usage and inventory can be tracked.

---

### **US 3.7 – Receive Refill Alerts**

As a user,
I want to receive alerts when a compartment’s spice level becomes low,
So that I can refill it in time.

---

### **US 3.8 – Manage Compartment Contents**

As a user,
I want to change the spice stored in any compartment and update quantities after refilling,
So that the device configuration stays accurate.



# **Epic 4: AI-Assisted Recipe Generation (Gemini Integration)**

## **Epic Description**

This Epic introduces AI-assisted support for recipe creation using Gemini integration.

The goal is to simplify the process of creating highly detailed, step-based recipes by allowing the recipe creator to provide high-level textual input or content sources (such as descriptions derived from videos). The AI system assists by generating a complete set of detailed cooking steps that fill in missing details and eliminate assumptions typically present in traditional recipes.

The generated steps should follow the application's philosophy of **atomic and fully explicit instructions**, ensuring that each step represents a clear and executable cooking action.

The AI-generated recipe content is **not automatically finalized**. Recipe creators must be given the opportunity to **review, edit, and approve** the generated steps before publishing the recipe.

This ensures:

* Authenticity of recipes
* Accuracy of instructions
* Creator control over final content

---

# **Core System Design (Internal Logic)**

## 1️⃣ AI-Assisted Recipe Input

Recipe creators can provide source material for recipe generation, such as:

* Textual description of a recipe
* Notes derived from cooking videos
* Basic ingredient list
* Rough cooking procedure

This input is sent to the Gemini AI system for processing.

---

## 2️⃣ AI Step Generation

The AI system processes the provided input and generates:

* A structured list of detailed cooking steps
* Explicit instructions without assumptions
* Intermediate preparation actions where needed
* Suggested timers or cues when applicable

The goal is to transform **high-level instructions into detailed atomic cooking steps**.

---

## 3️⃣ Gap Filling

AI should identify missing information in the provided recipe input and generate logical steps that fill these gaps.

Examples include:

* Preparation steps (washing, chopping, grinding, etc.)
* Heating instructions
* Order of ingredient addition
* Intermediate cooking actions

This ensures the final recipe follows the platform’s **complete step-based cooking philosophy**.

---

## 4️⃣ Editable AI Output

All AI-generated steps must be presented to the recipe creator for review.

The creator must be able to:

* Edit generated steps
* Delete unnecessary steps
* Add additional steps manually
* Rearrange step order

The recipe is only finalized after creator approval.

---

## 5️⃣ Integration with Recipe Step Structure

Generated steps should align with the system’s structured step model, including fields such as:

* Instruction text
* Optional timer
* Flame level
* Visual cues
* Ingredient references

This ensures AI-generated content remains compatible with the application's recipe architecture.

---

# **User Stories – Epic 4**

---

### **US 4.1 – Provide Recipe Input for AI Processing**

As a recipe creator,
I want to provide textual input or source material describing a recipe,
So that the AI can generate detailed cooking steps.

---

### **US 4.2 – Generate Detailed Cooking Steps**

As a system,
I want to use Gemini AI to generate structured and detailed recipe steps from the provided input,
So that incomplete instructions can be expanded into clear cooking actions.

---

### **US 4.3 – Fill Missing Cooking Steps**

As a system,
I want the AI to infer and generate missing preparation or intermediate steps,
So that the final recipe does not assume prior knowledge.

---

### **US 4.4 – Review AI Generated Steps**

As a recipe creator,
I want to review the steps generated by the AI,
So that I can verify their accuracy before publishing.

---

### **US 4.5 – Edit AI Generated Steps**

As a recipe creator,
I want to edit or modify the AI-generated steps,
So that I can ensure the instructions are correct and authentic.

---

### **US 4.6 – Remove or Add Steps**

As a recipe creator,
I want to remove unnecessary generated steps or add new ones manually,
So that the recipe accurately represents the cooking process.

---

### **US 4.7 – Finalize AI-Assisted Recipe**

As a recipe creator,
I want to finalize the recipe after reviewing the AI-generated steps,
So that the recipe can be published in the system.




# **Epic 5: Recipe Sharing, Forking, and Personal Collections**

## **Epic Description**

This Epic introduces a community-driven recipe sharing system inspired by collaborative platforms such as Lyfta.

The system provides a **shared recipe pool** where users can publish their recipes for others to discover and use. Users can browse this pool and interact with recipes created by other users.

Instead of modifying the original recipe, users can create their own **editable copy** of a recipe. This allows them to customize ingredient quantities, steps, or preferences according to their own needs while preserving the integrity of the original recipe.

Each derived recipe maintains a reference to the **original recipe it was created from**, enabling traceability of recipe evolution.

Users can also **save recipes** to their personal collection for quick access later.

---

# **Core System Design (Internal Logic)**

## 1️⃣ Public Recipe Pool

The system maintains a shared pool of recipes where users can publish their recipes.

Published recipes become discoverable by other users in the platform.

Each recipe contains:

* Recipe ID
* Creator ID
* Recipe content (ingredients, steps, etc.)
* Visibility status (published)

---

## 2️⃣ Recipe Forking / Copying

Users should not directly edit another user's recipe.

Instead, the system allows users to create a **personal copy** of an existing recipe.

When a recipe is copied:

* A new recipe entry is created.
* The copied recipe becomes editable by the user.
* The new recipe stores the **ID of the original recipe** it was derived from.

Example structure:

```
recipe_id
original_recipe_id
creator_id
```

If the recipe is the original version:

```
original_recipe_id = null
```

If the recipe is derived:

```
original_recipe_id = ID_of_original_recipe
```

---

## 3️⃣ Original Recipe Identification

The system should clearly mark whether a recipe is:

* **Original Recipe**
* **Derived Recipe**

Derived recipes should reference their source recipe to maintain lineage.

This allows the system to track the origin of recipes and preserve attribution.

---

## 4️⃣ Recipe Customization After Copying

Once a user creates a copy of a recipe, they should be able to:

* Modify ingredients
* Adjust quantities
* Edit cooking steps
* Customize preferences

These changes apply only to the copied version and do not affect the original recipe.

---

## 5️⃣ Saving Recipes

Users should be able to save recipes for quick access later.

Saved recipes are stored in a **personal recipe collection**.

Saving a recipe does not create a copy; it simply bookmarks the recipe for future reference.

---

# **User Stories – Epic 5**

---

### **US 5.1 – Publish Recipe**

As a recipe creator,
I want to publish my recipe to the shared recipe pool,
So that other users can discover and use it.

---

### **US 5.2 – Browse Public Recipes**

As a user,
I want to browse recipes published by other users,
So that I can discover new dishes to cook.

---

### **US 5.3 – Copy an Existing Recipe**

As a user,
I want to create my own copy of a recipe created by another user,
So that I can customize it according to my preferences.

---

### **US 5.4 – Maintain Recipe Lineage**

As a system,
I want to store the ID of the original recipe when a copy is created,
So that derived recipes can be traced back to their source.

---

### **US 5.5 – Identify Original and Derived Recipes**

As a user,
I want to see whether a recipe is original or derived from another recipe,
So that I can understand its origin.

---

### **US 5.6 – Edit Copied Recipe**

As a user,
I want to edit my copied recipe,
So that I can modify ingredients, quantities, or steps according to my needs.

---

### **US 5.7 – Save Recipes**

As a user,
I want to save recipes to my personal collection,
So that I can easily access them later.
