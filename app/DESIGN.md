---
name: Heritage Hearth
colors:
  surface: '#faf9f6'
  surface-dim: '#dbdad7'
  surface-bright: '#faf9f6'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f4f3f1'
  surface-container: '#efeeeb'
  surface-container-high: '#e9e8e5'
  surface-container-highest: '#e3e2e0'
  on-surface: '#1a1c1a'
  on-surface-variant: '#3f4945'
  inverse-surface: '#2f312f'
  inverse-on-surface: '#f2f1ee'
  outline: '#6f7975'
  outline-variant: '#bec9c4'
  surface-tint: '#066b58'
  primary: '#006754'
  on-primary: '#ffffff'
  primary-container: '#2a806c'
  on-primary-container: '#e7fff5'
  inverse-primary: '#84d6be'
  secondary: '#b7102a'
  on-secondary: '#ffffff'
  secondary-container: '#db313f'
  on-secondary-container: '#fffbff'
  tertiary: '#705624'
  on-tertiary: '#ffffff'
  tertiary-container: '#8b6e3a'
  on-tertiary-container: '#fff8f2'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#a0f2da'
  primary-fixed-dim: '#84d6be'
  on-primary-fixed: '#002019'
  on-primary-fixed-variant: '#005142'
  secondary-fixed: '#ffdad8'
  secondary-fixed-dim: '#ffb3b1'
  on-secondary-fixed: '#410007'
  on-secondary-fixed-variant: '#92001c'
  tertiary-fixed: '#ffdeaa'
  tertiary-fixed-dim: '#e6c185'
  on-tertiary-fixed: '#271900'
  on-tertiary-fixed-variant: '#5b4313'
  background: '#faf9f6'
  on-background: '#1a1c1a'
  surface-variant: '#e3e2e0'
typography:
  display-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  title-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  body-reg:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  num-display:
    fontFamily: JetBrains Mono
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.05em
  num-data:
    fontFamily: JetBrains Mono
    fontSize: 16px
    fontWeight: '500'
    lineHeight: 20px
  label-caps:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '700'
    lineHeight: 16px
    letterSpacing: 0.05em
rounded:
  sm: 0.5rem
  DEFAULT: 1rem
  md: 1.5rem
  lg: 2rem
  xl: 3rem
  full: 9999px
spacing:
  container-padding: 20px
  stack-gap-lg: 24px
  stack-gap-md: 16px
  stack-gap-sm: 8px
  section-margin: 32px
---

## Brand & Style
The brand personality is rooted in the concept of "stewardship"—combining the warmth of a family home with the precision of high-end wealth management. The target audience consists of multi-generational families who value legacy and stability over speculative gains. 

The design style is **Warm Minimalism**. It leverages heavy white space and a soft, ivory-based palette to create a "breathable" interface that reduces financial anxiety. By eschewing harsh borders in favor of soft tonal transitions and subtle depth, the UI evokes an emotional response of security, clarity, and quiet luxury.

## Colors
This design system utilizes a specialized palette tailored for the Chinese financial market, where red signifies growth.

- **Primary (Action):** Deep Cyan Green (#2A806C). Used for primary buttons, active states, and brand signatures.
- **Financial Up (Profit):** Heritage Red (#E63946). A sophisticated, high-chroma red used exclusively for positive financial deltas and "buy" actions.
- **Financial Down (Loss):** While the market standard is green, we use a desaturated version of our primary green or a specific neutral-gray for negative values to ensure brand consistency without confusing the user.
- **Background:** Ivory White (#FAF9F6). This provides a softer, more organic feel than pure white, reducing eye strain.
- **Accent:** Champagne Gold (#C5A36A). Used sparingly for "Premium" or "Wealth" tiers and celebratory milestones.

## Typography
The typography strategy distinguishes between narrative/interactable elements and raw financial data.

- **Humanist Sans-Serif:** We use **Plus Jakarta Sans** for all prose, headings, and UI labels. Its soft curves complement the large radius of the components.
- **Monospaced Precision:** **JetBrains Mono** is strictly reserved for currency values, percentages, and account numbers. This ensures that columns of numbers align perfectly in lists, conveying a sense of mathematical order and reliability.
- **Hierarchy:** Use `display-lg` for total portfolio balances and `label-caps` for small metadata descriptors to maintain a clear visual path.

## Layout & Spacing
The layout follows a **Fluid Grid** model optimized for mobile-first mini-programs.

- **Safe Zones:** A standard 20px lateral margin is maintained across all screens to ensure content does not feel "crowded" against the glass.
- **Spacing Rhythm:** We use a base-8 scale, but favor larger gaps (24px+) between distinct functional sections to maintain the minimalist aesthetic.
- **Groupings:** Related financial metrics (e.g., P&L, Total Return) should be grouped in a single card using 8px internal spacing to indicate their relationship.

## Elevation & Depth
This design system rejects harsh borders in favor of **Ambient Tonal Layers**. 

Hierarchy is established through:
- **Level 0 (Base):** Ivory White (#FAF9F6) background.
- **Level 1 (Cards):** Pure White (#FFFFFF) surfaces with a very soft, high-diffusion shadow (`Y: 4, Blur: 20, Opacity: 4%` in a warm grey tone).
- **Level 2 (Active/Modals):** Pure White surfaces with a more pronounced shadow (`Y: 8, Blur: 32, Opacity: 8%`) to indicate they sit "above" the main content.

Interactive elements should feel "tucked" into the surface or softly floating, never stamped on.

## Shapes
The shape language is defined by **Extreme Radii**. 

In this design system, standard containers use a 24px corner radius (`rounded-xl`). This softness mimics high-end physical products and furniture, reinforcing the "family" and "comfort" aspects of the brand. Buttons use a full pill-shape to distinguish them as primary touch targets. Small UI elements like tags or chips utilize a 12px radius to maintain the organic theme at a smaller scale.

## Components
- **Primary Buttons:** Pill-shaped, Deep Cyan Green background with White text. No shadow; instead, use a subtle 10% dark tint on press.
- **Wealth Cards:** Large white containers with 24px radius. These contain the "Portfolio Hero" info. Use JetBrains Mono for the primary balance.
- **Data Lists:** Use "Ghost Lists"—no dividers. Instead, use vertical spacing (16px) and subtle background shifts to separate rows.
- **Input Fields:** Soft Ivory backgrounds (slightly darker than the page base) with no borders. Focus state is indicated by a 1px Primary color inner-glow.
- **Profit/Loss Badges:** Pill-shaped with 10% opacity backgrounds of the status color (Red or Green) and 100% opacity text for high legibility and a sophisticated "glass" look.
- **Family Switcher:** A unique top-navigation component using circular avatars with a gold (#C5A36A) ring for the head of household.