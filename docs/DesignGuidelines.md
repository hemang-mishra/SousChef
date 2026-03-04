# Premium Food App - UI Design Guidelines

## 🎯 Design Philosophy

**Core Principle**: Luxury Minimalism with Glassmorphism  
**Brand Values**: Sophisticated, Refined, Curated, Premium, Trustworthy

### Design Pillars
1. **Breathing Room** - Generous white space, never cramped
2. **Visual Hierarchy** - Clear, intentional content prioritization
3. **Restrained Elegance** - Subtle effects over flashy animations
4. **Content First** - Food photography takes center stage
5. **Intentional Interaction** - Every tap feels purposeful and refined

---

## 📐 Layout System

### Spacing Scale
Use **multiples of 4dp** for all spacing to maintain rhythm and consistency.

```
SPACING TOKENS:
xxxs = 2dp   (micro spacing - icon padding, tight elements)
xxs  = 4dp   (minimal spacing - compact lists)
xs   = 8dp   (tight spacing - related elements)
sm   = 12dp  (small spacing - form fields, chips)
md   = 16dp  (standard spacing - DEFAULT for most UI)
lg   = 24dp  (generous spacing - section separation)
xl   = 32dp  (large spacing - major sections)
xxl  = 48dp  (extra large - hero sections)
xxxl = 64dp  (maximum spacing - page headers)
```

### Standard Margins & Padding

**Screen Edge Margins**
- Mobile: `16dp` horizontal, `24dp` vertical
- Tablet: `24dp` horizontal, `32dp` vertical
- Desktop: `32dp` minimum (use max-width containers)

**Content Padding**
- Cards: `16dp` all sides
- Glass Cards: `20dp` all sides (more breathing room)
- Buttons: `16dp` horizontal, `12dp` vertical
- Input Fields: `16dp` horizontal, `14dp` vertical
- List Items: `16dp` horizontal, `12dp` vertical

**Section Spacing**
- Between components: `24dp`
- Between major sections: `48dp`
- Between screen sections: `64dp`

### Grid System

**Mobile** (< 600dp)
- Columns: 4
- Gutter: 16dp
- Margin: 16dp

**Tablet** (600dp - 1024dp)
- Columns: 8
- Gutter: 24dp
- Margin: 24dp

**Desktop** (> 1024dp)
- Columns: 12
- Gutter: 24dp
- Max Width: 1440dp (center-aligned)

---

## 🎨 Color Usage Rules

### Primary Color (Gold)
**GoldMuted `#D4AF6A`**

**USAGE:**
✅ Primary CTAs (Reserve, Book, Purchase)
✅ Premium badges and tags
✅ Active states and selections
✅ Premium tier indicators
✅ Accent on important icons

**AVOID:**
❌ Large background areas (use sparingly)
❌ Body text (readability issues)
❌ More than 2-3 gold elements per screen
❌ Combining with other bright colors

### Surface Colors

**Light Mode**
- Background: `IvoryWhite #FAF9F7`
- Cards: `PearlWhite #FFFFFF`
- Elevated Cards: `CreamLight #F5F3F0`

**Dark Mode**
- Background: `CharcoalDeep #1A1A1A`
- Cards: `CharcoalMedium #2D2D2D`
- Elevated Cards: `CharcoalLight #3A3A3A`

### Text Hierarchy Colors

**Light Mode**
```
Primary Text (Headings, Important): TextPrimaryLight #1A1A1A
Secondary Text (Body, Descriptions): TextSecondaryLight #666666
Tertiary Text (Metadata, Captions): TextTertiaryLight #999999
Disabled Text: TextDisabledLight #CCCCCC
```

**Dark Mode**
```
Primary Text (Headings, Important): TextPrimaryDark #FAFAFA
Secondary Text (Body, Descriptions): TextSecondaryDark #B3B3B3
Tertiary Text (Metadata, Captions): TextTertiaryDark #808080
Disabled Text: TextDisabledDark #4D4D4D
```

### Accent Colors Usage

**SageGreen** - Success states, vegetarian/vegan tags, availability  
**TerracottaMuted** - Special offers, limited items, featured tags  
**DeepBurgundy** - Wine-related content, premium selections  
**DeepOlive** - Organic, farm-to-table indicators

**RULE:** Use only ONE accent color per component. Never mix multiple accents.

---

## ✍️ Typography Rules

### Font Pairing
- **Headings**: Serif (Playfair Display / Cormorant) - Elegant, authoritative
- **Body**: Sans-serif (Inter / SF Pro) - Clean, readable
- **Metadata**: Sans-serif - Consistent with body

### Typography Scale

```
DISPLAY (Hero sections, splash screens)
displayLarge   = 57sp, Bold, Serif, -0.25sp letter spacing
displayMedium  = 45sp, SemiBold, Serif
displaySmall   = 36sp, SemiBold, Serif

HEADLINE (Section titles)
headlineLarge  = 32sp, Bold, Serif
headlineMedium = 28sp, SemiBold, Serif  ← Most common for screen titles
headlineSmall  = 24sp, SemiBold, Serif

TITLE (Card headers, subsections)
titleLarge     = 22sp, Medium, Serif  ← Card titles, dish names
titleMedium    = 16sp, SemiBold, Sans ← Subsection headers
titleSmall     = 14sp, Medium, Sans

BODY (Content, descriptions)
bodyLarge      = 16sp, Normal, Sans  ← Default body text
bodyMedium     = 14sp, Normal, Sans  ← Secondary descriptions
bodySmall      = 12sp, Normal, Sans

LABEL (UI elements, buttons, tags)
labelLarge     = 14sp, Medium, Sans  ← Buttons
labelMedium    = 12sp, Medium, Sans  ← Tags, chips
labelSmall     = 11sp, Medium, Sans  ← Metadata
```

### Typography Application Rules

**Screen Titles**
- Use: `headlineMedium` (28sp, SemiBold, Serif)
- Color: `TextPrimaryLight/Dark`
- Position: 24dp from top, 16dp bottom margin

**Section Headers**
- Use: `titleLarge` (22sp, Medium, Serif)
- Color: `TextPrimaryLight/Dark`
- Bottom margin: 16dp

**Card Titles (Dish Names)**
- Use: `titleLarge` (22sp, Medium, Serif)
- Color: `TextPrimaryLight/Dark`
- Single line preferred, 2 lines maximum

**Descriptions**
- Use: `bodyLarge` (16sp) for important descriptions
- Use: `bodyMedium` (14sp) for secondary descriptions
- Color: `TextSecondaryLight/Dark`
- Line height: 1.5x font size
- Max lines: 3-4 with ellipsis

**Metadata (Price, Rating, Time)**
- Use: `labelMedium` (12sp, Medium, Sans)
- Color: `TextTertiaryLight/Dark`
- All caps for labels: `NO` (use sentence case)

**Buttons**
- Use: `labelLarge` (14sp, Medium, Sans)
- All caps: `NO` (use sentence case)
- Letter spacing: 0.1sp

### Line Height Rules
- Headings: 1.2x font size (tighter, more elegant)
- Body text: 1.5x font size (readable, comfortable)
- Metadata: 1.3x font size (compact but clear)

### Text Alignment
- **Left-align**: Body text, lists, cards (default)
- **Center-align**: Hero sections, empty states, dialogs
- **Right-align**: Prices, metadata (when in columns)
- **Never justify**: Avoid justified text (creates awkward spacing)

---

## 🃏 Component Design Patterns

### Cards

#### Standard Card
```kotlin
Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(
        defaultElevation = 0.dp  // Flat by default
    )
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Content with 16dp padding
    }
}
```

**RULES:**
- No elevation by default (flat design)
- 12dp corner radius (standard)
- 16dp internal padding
- Use border if needed: `0.5dp`, `BorderLight/Dark` color
- White/Charcoal background (not gray)

#### Glass Card (Premium Effect)
```kotlin
Card(
    modifier = Modifier
        .fillMaxWidth()
        .blur(12.dp),  // Blur for glass effect
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
        containerColor = GlassWhite.copy(alpha = 0.8f)
    ),
    border = BorderStroke(0.5.dp, BorderLight)
) {
    Column(modifier = Modifier.padding(20.dp)) {
        // Content with extra padding (20dp)
    }
}
```

**RULES:**
- Use for featured/premium content only
- 16dp corner radius (slightly more rounded)
- 20dp internal padding (more breathing room)
- Subtle border: 0.5dp
- Blur: 12dp minimum
- Background: `GlassWhite/Dark` at 70-80% opacity
- **LIMIT: 1-2 glass cards per screen** (don't overuse)

#### Image Card (Food Photos)
```kotlin
Card(
    modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(4f / 3f),  // Consistent ratio
    shape = RoundedCornerShape(12.dp)
) {
    Box {
        AsyncImage(
            model = imageUrl,
            contentDescription = dishName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        
        // Gradient overlay for text
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
        )
        
        // Text on gradient
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = dishName,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }
    }
}
```

**RULES:**
- Always use aspect ratio: `4:3` or `16:9` (consistent across app)
- Image: `ContentScale.Crop` to fill card
- Gradient overlay for text readability
- Text: Always white on images with overlay
- Text position: Bottom-left with 16dp padding
- No elevation (images have natural depth)

### Buttons

#### Primary Button (CTAs)
```kotlin
Button(
    onClick = { },
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(8.dp),
    colors = ButtonDefaults.buttonColors(
        containerColor = GoldMuted,
        contentColor = Color.White
    ),
    elevation = ButtonDefaults.buttonElevation(
        defaultElevation = 0.dp,
        pressedElevation = 2.dp
    ),
    contentPadding = PaddingValues(
        horizontal = 24.dp,
        vertical = 12.dp
    )
) {
    Text(
        text = "Reserve Table",
        style = MaterialTheme.typography.labelLarge
    )
}
```

**RULES:**
- Gold background for primary actions
- White text
- 8dp corner radius
- No elevation at rest, 2dp when pressed
- Full width on mobile, auto-width on tablet/desktop
- Minimum height: 48dp (accessibility)
- Sentence case text, never ALL CAPS

#### Secondary Button (Alternative Actions)
```kotlin
OutlinedButton(
    onClick = { },
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(8.dp),
    border = BorderStroke(1.dp, GoldMuted),
    colors = ButtonDefaults.outlinedButtonColors(
        contentColor = GoldMuted
    )
) {
    Text(
        text = "View Menu",
        style = MaterialTheme.typography.labelLarge
    )
}
```

**RULES:**
- Gold border, transparent background
- Gold text
- Same size/padding as primary button
- Use for secondary/alternative actions

#### Text Button (Tertiary Actions)
```kotlin
TextButton(onClick = { }) {
    Text(
        text = "Cancel",
        style = MaterialTheme.typography.labelLarge,
        color = TextSecondaryLight
    )
}
```

**RULES:**
- No background, no border
- Secondary text color
- Use for cancel, dismiss, less important actions
- Left-align with other buttons when in button groups

### Input Fields

```kotlin
OutlinedTextField(
    value = text,
    onValueChange = { text = it },
    label = { Text("Email address") },
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(8.dp),
    colors = TextFieldDefaults.outlinedTextFieldColors(
        focusedBorderColor = GoldMuted,
        unfocusedBorderColor = BorderLight,
        focusedLabelColor = GoldMuted,
        cursorColor = GoldMuted
    ),
    textStyle = MaterialTheme.typography.bodyLarge
)
```

**RULES:**
- Always use outlined variant (not filled)
- 8dp corner radius
- Border: `BorderLight/Dark` unfocused, `GoldMuted` focused
- Label: Floating, `TextSecondaryLight/Dark` color
- Text: `bodyLarge` style
- Height: 56dp minimum
- Error states: Red border, error message below in `bodySmall`

### Lists & List Items

#### Standard List Item
```kotlin
ListItem(
    headlineContent = {
        Text(
            text = "Truffle Risotto",
            style = MaterialTheme.typography.titleMedium
        )
    },
    supportingContent = {
        Text(
            text = "Creamy arborio rice with black truffle",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondaryLight
        )
    },
    leadingContent = {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
    },
    trailingContent = {
        Text(
            text = "$42",
            style = MaterialTheme.typography.titleMedium,
            color = GoldMuted
        )
    },
    modifier = Modifier.clickable { }
)
```

**RULES:**
- Image: 56dp × 56dp, 8dp corner radius
- Headline: `titleMedium` style
- Supporting: `bodyMedium`, secondary color
- Trailing: Price in gold, or icon
- Dividers: Only between items if needed, use `DividerLight/Dark`
- Padding: 16dp horizontal, 12dp vertical
- Touch target: Minimum 48dp height

#### Grid Layout (Restaurants, Dishes)
```kotlin
LazyVerticalGrid(
    columns = GridCells.Adaptive(minSize = 160.dp),
    contentPadding = PaddingValues(16.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
) {
    items(dishes) { dish ->
        // Grid item card
    }
}
```

**RULES:**
- Adaptive columns: Minimum 160dp per cell
- Gap between items: 12dp
- Content padding: 16dp
- Each cell has same aspect ratio

### Chips & Tags

#### Filter Chip
```kotlin
FilterChip(
    selected = isSelected,
    onClick = { },
    label = {
        Text(
            text = "Italian",
            style = MaterialTheme.typography.labelMedium
        )
    },
    shape = RoundedCornerShape(50),  // Fully rounded
    colors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = GoldMuted,
        selectedLabelColor = Color.White,
        containerColor = Gray100,
        labelColor = TextPrimaryLight
    ),
    border = null
)
```

**RULES:**
- Fully rounded (pill shape)
- Selected: Gold background, white text
- Unselected: Light gray background, dark text
- No border
- Icon optional (leading)
- Use for filters, categories, tags

### Bottom Sheets

```kotlin
ModalBottomSheet(
    onDismissRequest = { },
    shape = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp
    ),
    containerColor = MaterialTheme.colorScheme.surface,
    dragHandle = {
        Box(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .width(32.dp)
                .height(4.dp)
                .background(
                    color = Gray300,
                    shape = RoundedCornerShape(2.dp)
                )
        )
    }
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        // Content
    }
}
```

**RULES:**
- Top corners: 24dp rounded
- Drag handle: 32dp × 4dp, gray, centered
- Content padding: 24dp all sides
- Title: `headlineSmall`, 16dp bottom margin
- Surface color background (white/charcoal)

### Dialogs

```kotlin
AlertDialog(
    onDismissRequest = { },
    shape = RoundedCornerShape(16.dp),
    title = {
        Text(
            text = "Confirm Reservation",
            style = MaterialTheme.typography.headlineSmall
        )
    },
    text = {
        Text(
            text = "Reserve table for 2 at 7:00 PM?",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondaryLight
        )
    },
    confirmButton = {
        Button(onClick = { }) {
            Text("Confirm")
        }
    },
    dismissButton = {
        TextButton(onClick = { }) {
            Text("Cancel")
        }
    }
)
```

**RULES:**
- 16dp corner radius
- Title: `headlineSmall`, primary color
- Body: `bodyLarge`, secondary color
- Buttons: Right-aligned, confirm on right
- Max width: 280dp on mobile
- Padding: 24dp all sides

---

## 🖼️ Image Guidelines

### Image Specifications

**Food Photography**
- Aspect Ratio: `4:3` (preferred) or `16:9`
- Minimum Resolution: 800px wide
- Format: JPEG (photos), PNG (graphics)
- Optimization: WebP when possible
- Quality: High (80-90% compression)

**Image Treatment**
- No filters or heavy editing
- Natural colors (food should look appetizing)
- Good lighting (bright, not dark)
- Clean backgrounds (blur if needed)
- Focus on the dish (close-up preferred)

### Image Overlays (for text readability)

**Gradient Overlay**
```kotlin
Brush.verticalGradient(
    colors = listOf(
        Color.Transparent,           // Top
        Color.Black.copy(alpha = 0.6f)  // Bottom (40-70% opacity)
    )
)
```

**Scrim Overlay** (when gradient isn't enough)
```kotlin
Color.Black.copy(alpha = 0.3f)  // 20-40% black overlay
```

**RULES:**
- Always add overlay when placing text on images
- Gradient preferred over flat scrim
- Text must have 4.5:1 contrast ratio minimum
- Test on various images to ensure readability

### Placeholder States

```kotlin
// Shimmer effect while loading
Box(
    modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(4f / 3f)
        .background(Gray100)
        .shimmerEffect()  // Custom shimmer modifier
)

// Error state
Box(
    modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(4f / 3f)
        .background(Gray100),
    contentAlignment = Alignment.Center
) {
    Icon(
        imageVector = Icons.Default.Restaurant,
        contentDescription = null,
        tint = Gray400,
        modifier = Modifier.size(48.dp)
    )
}
```

**RULES:**
- Loading: Shimmer effect on gray background
- Error: Icon on gray background, no harsh red
- Same aspect ratio as actual image
- Smooth transition when image loads

---

## ⚡ Interaction & Animation

### Animation Principles
1. **Subtle & Refined** - No jarring or overly playful animations
2. **Purposeful** - Every animation serves a function
3. **Fast & Responsive** - 200-300ms for most transitions
4. **Natural Easing** - Use ease-out for entrances, ease-in for exits

### Standard Animations

**Fade In/Out**
```kotlin
AnimatedVisibility(
    visible = isVisible,
    enter = fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)),
    exit = fadeOut(animationSpec = tween(200, easing = LinearOutSlowInEasing))
) {
    // Content
}
```
- Duration: 300ms in, 200ms out
- Use for: Dialogs, overlays, tooltips

**Scale**
```kotlin
animateFloatAsState(
    targetValue = if (isPressed) 0.95f else 1f,
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
)
```
- Scale down: 0.95x when pressed
- Use for: Buttons, cards, interactive elements
- **Avoid**: Aggressive scaling (> 1.05x)

**Slide**
```kotlin
AnimatedVisibility(
    visible = isVisible,
    enter = slideInVertically(
        initialOffsetY = { it },
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ),
    exit = slideOutVertically(
        targetOffsetY = { it },
        animationSpec = tween(200, easing = LinearOutSlowInEasing)
    )
) {
    // Content
}
```
- Duration: 300ms
- Use for: Bottom sheets, side panels
- Direction: Natural (bottom-up for sheets, left-right for panels)

### Interactive States

**Hover** (Desktop/Tablet)
```kotlin
.hoverable(
    enabled = true,
    interactionSource = remember { MutableInteractionSource() }
)
.indication(
    interactionSource = interactionSource,
    indication = rememberRipple(
        color = HoverLight
    )
)
```
- Overlay: 4% opacity
- No scale change on hover (too subtle on desktop)

**Press**
```kotlin
.clickable(
    interactionSource = interactionSource,
    indication = rememberRipple(
        bounded = true,
        color = PressedLight
    ),
    onClick = { }
)
```
- Ripple: 10% opacity
- Scale: 0.95x (for cards)
- Duration: 100ms

**Disabled**
```kotlin
.alpha(if (enabled) 1f else 0.38f)
.clickable(enabled = enabled) { }
```
- Opacity: 38% (Material standard)
- No pointer events
- Clear visual indication

### Loading States

**Pull to Refresh**
```kotlin
PullRefreshIndicator(
    refreshing = isRefreshing,
    state = pullRefreshState,
    modifier = Modifier.align(Alignment.TopCenter),
    backgroundColor = MaterialTheme.colorScheme.surface,
    contentColor = GoldMuted
)
```
- Color: Gold indicator
- Background: Surface color
- Smooth spring animation

**Shimmer Loading**
```kotlin
// Use for skeleton screens
.background(
    brush = Brush.linearGradient(
        colors = listOf(
            Gray100,
            Gray200,
            Gray100
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 0f)
    )
)
// Animate with infinite transition
```
- Colors: Light gray gradient
- Speed: 1.5s per cycle
- Direction: Left to right

### Haptic Feedback

**When to Use**
✅ Button presses (primary actions)
✅ Toggle switches
✅ Selection changes (important items)
✅ Success confirmations
✅ Pull to refresh

**When NOT to Use**
❌ Scrolling
❌ Every tap/click
❌ Hover states
❌ Typing

```kotlin
val haptics = LocalHapticFeedback.current

Button(
    onClick = {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        // Action
    }
) {
    Text("Reserve")
}
```

---

## 📱 Screen Patterns

### Top App Bar

```kotlin
TopAppBar(
    title = {
        Text(
            text = screenTitle,
            style = MaterialTheme.typography.headlineMedium
        )
    },
    navigationIcon = {
        IconButton(onClick = { }) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back"
            )
        }
    },
    actions = {
        IconButton(onClick = { }) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        }
    },
    colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.background,
        titleContentColor = TextPrimaryLight
    )
)
```

**RULES:**
- Background: Same as screen background (seamless)
- Title: `headlineMedium` (28sp, Serif)
- Icons: 24dp, secondary color
- Height: 64dp
- Elevation: 0dp (flat)
- Add elevation only when scrolled (2dp)

### Bottom Navigation Bar

```kotlin
NavigationBar(
    containerColor = MaterialTheme.colorScheme.surface,
    tonalElevation = 8.dp
) {
    NavigationBarItem(
        icon = {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null
            )
        },
        label = {
            Text(
                text = "Home",
                style = MaterialTheme.typography.labelMedium
            )
        },
        selected = selectedIndex == 0,
        onClick = { },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = GoldMuted,
            selectedTextColor = GoldMuted,
            indicatorColor = ChampagneGold,
            unselectedIconColor = Gray600,
            unselectedTextColor = Gray600
        )
    )
}
```

**RULES:**
- Background: Surface color (white/charcoal)
- Selected: Gold icon and text
- Unselected: Gray icon and text
- Indicator: Champagne gold background
- Labels: Always visible (not just on selected)
- Icons: 24dp
- Max items: 5 (ideal: 3-4)

### Empty States

```kotlin
Box(
    modifier = Modifier
        .fillMaxSize()
        .padding(48.dp),
    contentAlignment = Alignment.Center
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Restaurant,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = Gray300
        )
        Text(
            text = "No reservations yet",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimaryLight,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Your upcoming reservations will appear here",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondaryLight,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { }) {
            Text("Explore Restaurants")
        }
    }
}
```

**RULES:**
- Center-aligned content
- Large icon: 96-120dp, light gray
- Title: `headlineSmall`, primary color
- Description: `bodyMedium`, secondary color, 2 lines max
- CTA button: Optional, use when applicable
- Padding: 48dp from edges

### Error States

```kotlin
Column(
    modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Icon(
        imageVector = Icons.Default.ErrorOutline,
        contentDescription = null,
        modifier = Modifier.size(64.dp),
        tint = ErrorLight
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "Something went wrong",
        style = MaterialTheme.typography.titleLarge,
        color = TextPrimaryLight
    )
    Text(
        text = "Please try again",
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondaryLight
    )
    Spacer(modifier = Modifier.height(24.dp))
    Button(onClick = { }) {
        Text("Retry")
    }
}
```

**RULES:**
- Icon: Error color, 64dp
- Message: Clear and helpful (not technical jargon)
- CTA: "Retry" or "Go Back"
- Tone: Apologetic but not alarming

---

## 🎯 Accessibility Guidelines

### Color Contrast
- **Text on Background**: Minimum 4.5:1 ratio
- **Large Text (18sp+)**: Minimum 3:1 ratio
- **Icons**: Minimum 3:1 ratio
- **Interactive Elements**: Minimum 3:1 ratio

**Test all color combinations!**

### Touch Targets
- **Minimum**: 48dp × 48dp for all interactive elements
- **Recommended**: 56dp × 56dp for primary actions
- **Spacing**: 8dp minimum between touch targets

### Content Descriptions
```kotlin
Icon(
    imageVector = Icons.Default.Search,
    contentDescription = "Search restaurants"  // Always provide
)

Image(
    painter = painterResource(id = R.drawable.dish),
    contentDescription = "Truffle risotto"  // Describe the image
)

// Decorative images
Icon(
    imageVector = Icons.Default.Star,
    contentDescription = null  // null for decorative only
)
```

**RULES:**
- All icons: Descriptive text (action or meaning)
- All images: Describe what's shown
- Decorative only: `null` description
- Never leave blank: `""` (screen reader will read filename)

### Semantic Ordering
- Use `Modifier.semantics` for proper screen reader ordering
- Logical tab order for keyboard navigation
- Headings announced as headings

### Dark Mode Support
- All screens must support both light and dark mode
- No hard-coded colors (use theme colors)
- Test readability in both modes
- Respect system preference

---

## 📐 Responsive Design

### Breakpoints
```
COMPACT (Mobile): < 600dp width
MEDIUM (Tablet): 600dp - 840dp width  
EXPANDED (Desktop): > 840dp width
```

### Adaptive Layouts

**Mobile (Compact)**
- Single column layout
- Full-width cards
- Bottom navigation
- Hamburger menu for secondary nav

**Tablet (Medium)**
- Two column layout (60/40 split)
- Navigation rail (side navigation)
- Larger cards with more spacing
- Master-detail pattern

**Desktop (Expanded)**
- Three column layout (max)
- Persistent navigation drawer
- Max width containers (1440dp)
- Hover states enabled
- Keyboard shortcuts

### Window Size Classes

```kotlin
val windowSizeClass = calculateWindowSizeClass(activity = this)

when (windowSizeClass.widthSizeClass) {
    WindowWidthSizeClass.Compact -> {
        // Mobile layout
    }
    WindowWidthSizeClass.Medium -> {
        // Tablet layout
    }
    WindowWidthSizeClass.Expanded -> {
        // Desktop layout
    }
}
```

---

## 🚫 Common Mistakes to Avoid

### ❌ DON'Ts

1. **Don't use multiple accent colors in one component**
    - ❌ Gold button with green badge
    - ✅ Use one accent at a time

2. **Don't over-use glassmorphism**
    - ❌ Every card is glass
    - ✅ 1-2 glass elements per screen for emphasis

3. **Don't use harsh shadows**
    - ❌ `elevation = 16.dp`
    - ✅ Flat design with subtle borders or 2-4dp elevation max

4. **Don't cramped spacing**
    - ❌ 4dp between major sections
    - ✅ 24-48dp between major sections

5. **Don't use ALL CAPS everywhere**
    - ❌ Button text: "RESERVE NOW"
    - ✅ Button text: "Reserve Now"

6. **Don't mix font families within one text block**
    - ❌ "Truffle" (Serif) "Risotto" (Sans)
    - ✅ Entire title in one font

7. **Don't ignore aspect ratios for images**
    - ❌ Random image sizes
    - ✅ Consistent 4:3 or 16:9 ratio

8. **Don't use bright, saturated colors**
    - ❌ Bright red, electric blue
    - ✅ Muted, sophisticated palette

9. **Don't place text on images without overlay**
    - ❌ White text directly on photo
    - ✅ Text on gradient/scrim overlay

10. **Don't create inconsistent touch targets**
    - ❌ Some buttons 40dp, others 60dp
    - ✅ Consistent 48-56dp minimum

---

## ✅ Quick Checklist for Every Screen

Before finalizing any screen, verify:

**Layout & Spacing**
- [ ] 16dp margin from screen edges
- [ ] 24-48dp spacing between major sections
- [ ] Consistent card padding (16dp or 20dp)
- [ ] Breathing room around all elements

**Typography**
- [ ] Screen title uses `headlineMedium` (28sp, Serif)
- [ ] Body text uses `bodyLarge` or `bodyMedium` (Sans)
- [ ] Labels use `labelLarge` or `labelMedium` (Sans)
- [ ] Proper hierarchy (sizes decrease with importance)
- [ ] Text color matches importance (primary > secondary > tertiary)

**Colors**
- [ ] Follows color usage rules (gold for CTAs only)
- [ ] Text has 4.5:1 contrast ratio minimum
- [ ] Consistent light/dark mode support
- [ ] No hard-coded colors

**Components**
- [ ] Cards have 12dp corners (16dp for glass cards)
- [ ] Buttons have 8dp corners
- [ ] No excessive elevation (0-4dp max)
- [ ] All interactive elements are 48dp+ tall
- [ ] Images have consistent aspect ratios

**Interactions**
- [ ] Smooth animations (200-300ms)
- [ ] Proper ripple effects
- [ ] Disabled states clearly indicated (38% opacity)
- [ ] Loading states implemented
- [ ] Error states handled gracefully

**Accessibility**
- [ ] All images have content descriptions
- [ ] Touch targets are 48dp minimum
- [ ] Color contrast meets WCAG AA standards
- [ ] Works with screen readers
- [ ] Keyboard navigable (desktop)

**Responsive**
- [ ] Adapts to different screen sizes
- [ ] Max width on large screens (1440dp)
- [ ] Proper breakpoint handling
- [ ] No horizontal scrolling required

---

## 🎨 Example Screen Specifications

### Home Screen

**Layout:**
```
Top App Bar (64dp height)
├─ Logo/Title (left)
└─ Search Icon (right)

Hero Section (280dp height)
└─ Featured Restaurant Card (Glass)
    ├─ Image (16:9 ratio)
    ├─ Gradient Overlay
    └─ Title + CTA

Section: "Popular Restaurants" (headlineMedium)
└─ Horizontal Scroll List
    └─ Cards (160dp × 200dp each)

Section: "Nearby" (headlineMedium)  
└─ Vertical List
    └─ List Items (80dp height each)

Bottom Navigation Bar (80dp height)
```

**Spacing:**
- Screen edge margin: 16dp
- Between top bar and hero: 0dp (seamless)
- Between hero and section: 32dp
- Between section title and content: 16dp
- Between sections: 48dp
- Between list items: 12dp

**Colors:**
- Background: IvoryWhite (light) / CharcoalDeep (dark)
- Hero card: Glass effect (GlassWhite/Dark)
- CTAs: GoldMuted
- Text: Follows hierarchy

### Restaurant Detail Screen

**Layout:**
```
Hero Image (Screen width × 280dp)
├─ Back Button (top-left, 48dp from top)
├─ Favorite Button (top-right, 48dp from top)
└─ Gradient Overlay (bottom 120dp)
    └─ Restaurant Name (headlineLarge, white)

Content Card (overlapping hero by 32dp)
├─ Rating + Price + Category Row
├─ Description (bodyLarge)
├─ Tags (FilterChips in Row)
└─ Divider

Section: "Menu" (headlineMedium)
└─ List of Dishes
    └─ ListItem (image, name, price, description)

Sticky Bottom Bar (56dp height)
└─ Primary Button "Reserve Table"
```

**Spacing:**
- Hero image: Full width, no margin
- Content card: 16dp horizontal margin
- Card overlap: -32dp from hero
- Card padding: 20dp
- Between sections: 32dp
- Bottom bar: 16dp padding

**Colors:**
- Hero: Full-bleed image with gradient
- Content card: White (light) / CharcoalMedium (dark)
- Reserve button: GoldMuted

---

## 🧩 Reusable UI Components Library

All reusable components are located in `com.souschef.ui.components.PremiumComponents.kt`.

### Theme-Aware Colors (AppColors)

Use `AppColors` for all theme-aware color access. Located in `Theme.kt`.

```kotlin
// Instead of hardcoding:
Text(color = Color(0xFF1A1A1A))

// Use AppColors:
Text(color = AppColors.textPrimary())

// Common color accessors:
AppColors.gold()              // Primary gold accent
AppColors.onGold()            // Color for text/icons on gold
AppColors.textPrimary()       // Main text color
AppColors.textSecondary()     // Body/description text
AppColors.textTertiary()      // Labels/metadata
AppColors.cardBackground()    // Card container color
AppColors.heroBackground()    // Always dark (for drama)
AppColors.glassBackground()   // Glass effect background
AppColors.border()            // Border color
AppColors.divider()           // Divider line color
AppColors.success()           // Success semantic color
AppColors.error()             // Error semantic color
AppColors.warning()           // Warning semantic color
AppColors.info()              // Info semantic color
AppColors.accentGreen()       // Green accent
AppColors.accentTerracotta()  // Terracotta accent
AppColors.accentBurgundy()    // Burgundy accent
AppColors.goldBackground()    // Subtle gold-tinted background
```

### Premium Buttons

#### PremiumButton (Primary CTA)
```kotlin
import com.souschef.ui.components.PremiumButton

PremiumButton(
    text = "Reserve Table",
    onClick = { /* action */ },
    enabled = true,
    isLoading = false,
    leadingIcon = Icons.Default.Restaurant
)
```
**Use for:** Main actions - Book, Reserve, Purchase, Sign In

#### PremiumOutlinedButton (Secondary)
```kotlin
import com.souschef.ui.components.PremiumOutlinedButton

PremiumOutlinedButton(
    text = "View Menu",
    onClick = { /* action */ }
)
```
**Use for:** Alternative actions - View Details, Learn More

#### PremiumTextButton (Tertiary)
```kotlin
import com.souschef.ui.components.PremiumTextButton

PremiumTextButton(
    text = "Cancel",
    onClick = { /* action */ }
)
```
**Use for:** Dismiss, Cancel, Skip

#### PremiumSmallButton (Inline)
```kotlin
import com.souschef.ui.components.PremiumSmallButton

PremiumSmallButton(
    text = "Add",
    onClick = { /* action */ }
)
```
**Use for:** Inline actions, compact spaces

#### PremiumGradientButton (Hero CTAs)
```kotlin
import com.souschef.ui.components.PremiumGradientButton

PremiumGradientButton(
    text = "Book Now",
    onClick = { /* action */ },
    leadingIcon = Icons.Default.ArrowForward
)
```
**Use for:** Hero sections, featured content CTAs

### Premium Cards

#### PremiumCard (Standard)
```kotlin
import com.souschef.ui.components.PremiumCard

PremiumCard {
    Text("Card content")
}
```
**Use for:** Most content cards, lists

#### PremiumBorderedCard
```kotlin
import com.souschef.ui.components.PremiumBorderedCard

PremiumBorderedCard {
    Text("Bordered card content")
}
```
**Use for:** Secondary sections, visual separation needed

#### PremiumElevatedCard
```kotlin
import com.souschef.ui.components.PremiumElevatedCard

PremiumElevatedCard(elevation = 8.dp) {
    Text("Important content")
}
```
**Use for:** Featured/important content that needs emphasis

#### PremiumGlassCard
```kotlin
import com.souschef.ui.components.PremiumGlassCard

PremiumGlassCard {
    Text("Premium content")
}
```
**Use for:** Featured/premium sections. **LIMIT: 1-2 per screen**

#### PremiumHeroCard
```kotlin
import com.souschef.ui.components.PremiumHeroCard

PremiumHeroCard {
    Column {
        Text("Experience", style = MaterialTheme.typography.headlineLarge, color = Color.White)
        Text("Culinary Excellence", color = Color.White)
    }
}
```
**Use for:** Hero sections, dramatic dark backgrounds

#### PremiumGoldAccentCard
```kotlin
import com.souschef.ui.components.PremiumGoldAccentCard

PremiumGoldAccentCard {
    Text("Reservation Confirmed")
}
```
**Use for:** Confirmations, premium features, reservations

### Badges & Tags

#### PremiumBadge
```kotlin
import com.souschef.ui.components.PremiumBadge
import com.souschef.ui.components.BadgeType

PremiumBadge(text = "Michelin Star", type = BadgeType.GOLD)
PremiumBadge(text = "Top Rated", type = BadgeType.DARK)
PremiumBadge(text = "New", type = BadgeType.SUCCESS)
PremiumBadge(text = "Limited", type = BadgeType.ACCENT)
```
**Badge Types:**
- `GOLD` - Gold gradient background
- `DARK` - Dark background, gold text
- `SUCCESS` - Green gradient
- `ACCENT` - Terracotta/burgundy gradient

#### PremiumTag
```kotlin
import com.souschef.ui.components.PremiumTag

PremiumTag(text = "Vegetarian", color = AppColors.accentGreen())
PremiumTag(text = "French", color = AppColors.accentBurgundy())
```
**Use for:** Categories, dietary tags, cuisine types

#### PremiumStatusTag
```kotlin
import com.souschef.ui.components.PremiumStatusTag

PremiumStatusTag(text = "Available", color = AppColors.success())
PremiumStatusTag(text = "Sold Out", color = AppColors.error())
```
**Use for:** Availability, status indicators

### Section Components

#### PremiumSectionHeader
```kotlin
import com.souschef.ui.components.PremiumSectionHeader

PremiumSectionHeader(
    title = "Featured Restaurants",
    action = {
        TextButton(onClick = {}) { Text("See All") }
    }
)
```

#### PremiumSubsectionLabel
```kotlin
import com.souschef.ui.components.PremiumSubsectionLabel

PremiumSubsectionLabel(label = "Popular in your area")
```

### List Components

#### PremiumListItem
```kotlin
import com.souschef.ui.components.PremiumListItem

PremiumListItem(
    title = "Truffle Risotto",
    subtitle = "Creamy arborio rice with black truffle",
    price = "$42",
    leadingContent = {
        AsyncImage(model = imageUrl, ...)
    }
)
```

### Dividers

#### PremiumDivider
```kotlin
import com.souschef.ui.components.PremiumDivider

PremiumDivider()
```

#### PremiumDottedDivider
```kotlin
import com.souschef.ui.components.PremiumDottedDivider

PremiumDottedDivider(dotCount = 30)
```
**Use for:** Decorative separation, tickets, confirmations

---

## 🎨 Color System Reference

### Light vs Dark Theme Colors

| Purpose | Light Mode | Dark Mode |
|---------|------------|-----------|
| **Background** | IvoryWhite `#FAF9F7` | CharcoalDeep `#121212` |
| **Card Background** | White `#FFFFFF` | CharcoalMedium `#1E1E1E` |
| **Card Elevated** | White `#FFFFFF` | CharcoalLight `#2C2C2C` |
| **Primary Gold** | GoldVibrant `#FFB800` | GoldVibrant `#FFB800` |
| **Text Primary** | `#1A1A1A` | `#FAFAFA` |
| **Text Secondary** | `#5C5C5C` | `#B8B8B8` |
| **Text Tertiary** | `#8A8A8A` | `#787878` |
| **Border** | `#E0E0E0` | `#3A3A3A` |
| **Divider** | `#EEEEEE` | `#2A2A2A` |
| **Success** | `#4CAF50` | `#81C784` |
| **Error** | `#E53935` | `#EF5350` |
| **Warning** | `#FF9800` | `#FFB74D` |
| **Info** | `#2196F3` | `#64B5F6` |

### Accent Colors (Same in Both Themes)
| Color | Hex | Use Case |
|-------|-----|----------|
| SageGreen | `#66BB6A` | Vegetarian, organic, success |
| TerracottaVibrant | `#EF5350` | Limited, special offers |
| DeepBurgundy | `#8E2441` | Wine, premium |
| TealVibrant | `#26A69A` | Fresh, variety |
| DeepOlive | `#558B2F` | Farm-to-table |

---

## 🔍 LLM Prompt Template

When asking an LLM to generate UI code based on these guidelines:

```
Create a [SCREEN_NAME] screen following the Premium Food App Design Guidelines:

REQUIREMENTS:
- Use [COMPONENT_TYPE] with proper spacing (reference spacing tokens)
- Apply [COLOR_SCHEME] from the color system
- Typography: [SPECIFY_STYLES] for different text elements
- Follow [SPECIFIC_PATTERN] pattern from guidelines
- Implement [INTERACTION_TYPE] with proper animations
- Ensure accessibility (48dp touch targets, content descriptions)
- Support both light and dark modes

CONSTRAINTS:
- Spacing: Use multiples of 4dp
- Colors: Only use defined color tokens
- Typography: Serif for headings, Sans for body
- Components: [LIST_REQUIRED_COMPONENTS]
- No deviation from glassmorphism/minimalist style

OUTPUT:
Provide complete Jetpack Compose code with:
1. All necessary imports
2. Preview function with both light/dark modes  
3. Commented sections explaining design decisions
4. Proper theming integration (MaterialTheme usage)
```

---

## 📚 Summary

**Core Principles:**
1. Luxury through simplicity (not complexity)
2. Generous spacing (never cramped)
3. Refined typography pairing (Serif + Sans)
4. Muted, sophisticated colors (not bright)
5. Subtle interactions (not flashy)
6. Content-first approach (food photography shines)

**Key Rules:**
- **Spacing**: Multiples of 4dp, 16dp minimum margins
- **Typography**: `headlineMedium` for titles, `bodyLarge` for content
- **Colors**: Gold for CTAs only, generous use of neutrals
- **Components**: Flat design, subtle borders, minimal elevation
- **Images**: Consistent ratios (4:3), always with overlays for text
- **Accessibility**: 48dp touch targets, 4.5:1 contrast, content descriptions

**Remember:** Premium feels expensive through **restraint**, not excess.