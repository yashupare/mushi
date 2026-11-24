# Mushi UI/UX revamp: complete plan

Companion to `MUSHI_DESIGN_SPEC.md` (research). This file is the build plan: what every part of
the app should look and behave like, and the order of work. The aim is one coherent change set,
built and checked once, not a series of one-off tweaks.

Principle source: Apple's Human Interface Guidelines and the current Apple Music app. We follow
the principles, not Apple's exact look or assets.

## 0. Status before this plan

Already built and on the test phone: palette and type scale, flat bottom bar with fold, flat
mini player, large collapsing titles (Home, Library, Search), tinted Now Playing, album and
playlist header and track rows, flat cards and skeletons, line tab icons, profile button,
Mushi name and logo.

What is still the old look: every library component's own style (buttons, text fields, toggles,
sliders, tabs, list rows, dialogs, menus, toasts, checkboxes, progress, sheets), the lyrics view,
the queue sheet, the artist page, Library lists, Settings, empty and error states, haptics.

## 1. Design tokens

| Token | Value |
|---|---|
| Palette | Four user-selectable palettes (Settings → Appearance → Colour palette): Rose Night (default), Aurora, Ember, Tide. Each has a near-black base, 4 surface levels, one accent and a brand gradient, in dark and light; defined in `core/ui/theming/MushiPalettes.kt`. Primary buttons use `MushiButton` so labels use the accent's label colour. |
| Dark surfaces | background `#000000`, surface `#111113`, raised `#1C1C1F` |
| Light surfaces | background `#FFFFFF`, surface `#F4F4F6`, raised `#ECECF0` |
| Text | primary full contrast; secondary `#9A9AA2` dark / `#6B6B73` light (both at least 4.5:1) |
| Hairline | 0.5 dp, `border` colour at 60% |
| Corner radius | 10 dp art, 12 dp buttons and fields, 14 dp menus and cards, 20 dp dialogs and sheets, full pill for chips and the mini player |
| Elevation | none by default. Shadow only on: floating pills, menus, dialogs, cover art. Never borders. |
| Type | page title 34 bold; section title 22 bold; row title 17 medium; row subtitle 13 muted; button label 15 semibold; caption 11. Digits that line up use tabular figures. |
| Hit target | 44 dp minimum for anything tappable |
| Motion | springs for movement (medium stiffness, no bounce except toggles), 150 to 250 ms fades. Never block input on an animation. Respect the system "remove animations" setting. |

## 2. Component spec (all done in one theme file)

Library components are restyled globally through the Millennium theme, so every screen inherits
them. Rules common to all: no border, no shadow, no glow, no bevel, flat fills.

| Component | Spec |
|---|---|
| Primary button | Accent fill, white label, 12 dp radius, 48 dp high, 20 dp side padding. Pressed 85% opacity. Disabled 40%. One or two per view. |
| Secondary button | 8% text-colour tint fill, label in text colour. |
| Quiet button | No fill; label or icon in text colour (accent when it is the main action in a bar). |
| Danger button | Danger fill, white label. |
| Icon button | 44 dp target. Primary: accent circle. Secondary: 8% tint circle. Quiet: no fill. 22 dp icon. |
| Text field | 8% tint fill, 12 dp radius, no border, 44 dp high, placeholder in muted colour; focus adds a 2 dp accent outline. |
| Toggle | Pill track 51 by 31 dp; on = accent, off = 16% tint; white 27 dp thumb with a soft shadow. |
| Slider | 4 dp track, track 20% tint, filled part in text colour; 12 dp thumb that grows to 18 dp while dragging. |
| Checkbox | Round, 22 dp; checked = accent fill with a white tick. |
| Tabs and segmented control | Rounded tint container; the selected tab is a raised pill with a soft shadow and semibold label. |
| List row | No background or border; 56 dp minimum; 16 dp side padding; selected = 6% tint. |
| Card | Surface fill, 14 dp radius, no border or shadow. |
| Menu | Raised fill, 14 dp radius, soft shadow, 44 dp rows at 17 pt, 12 dp gaps between groups, destructive last. |
| Dialog | Raised fill, 20 dp radius, soft shadow, 40% scrim, enters at 95% scale with a fade. |
| Bottom sheet | Raised fill, 20 dp top radius, 36 by 5 dp grabber, 40% scrim, swipe to dismiss. |
| Toast | Inverse pill (text colour fill, background colour label), 14 dp radius. |
| Progress | 3 dp pill track at 12% tint, fill in text colour. |
| Separator | 0.5 dp hairline. |
| Skeleton | Flat 9% tint with a shimmer sweep (done). |

## 3. Layout and navigation

- Tabs: Home, Library, Search. Search last. Settings through the profile button (done).
- The bottom bar folds into a pill with the mini player on scroll down (done).
- Large collapsing titles on Home, Library, Search (done).
- Home's top bar holds only the profile button. Devices and Group Jam move to the top of Settings.
- The mini player hides its heart and play button when nothing is queued.
- Sheets use medium and large detents with a grabber.

## 4. Screens

| Screen | Spec |
|---|---|
| Home | Large title, hero pager (done), flat chips (done), shelves with large art, section titles with a "See all" affordance. |
| Search | Large title, search field, recents list with a clear action, results grouped by type. |
| Library | Large title, segmented tabs, plain list rows (art, title, subtitle), grid option stays. |
| Album and playlist | Large cover header, accent artist link, Play and Shuffle, hairline track list (done). |
| Artist | Large circular or square portrait, name, Play and Shuffle, top tracks as a list, albums as a shelf. |
| Now Playing | Tinted background (done), left-aligned title (done), artwork scales on pause (done), swipe the artwork for next or previous, long press for track details. |
| Lyrics | Full-screen, tinted like Now Playing, 28 pt bold lines, the current line at full strength and others at 40%, tap a line to seek, smooth auto-scroll that keeps the current line a third of the way down. |
| Queue | Sheet with "Playing Next", drag handles to reorder, swipe to remove. |
| Settings | Grouped inset lists with rounded sections, a toggle per row where it applies, large title. |
| Empty and error states | Centred icon, one-line title, one-line explanation, a single primary button. |

## 5. Behaviour, motion and haptics

- Haptics: a light tick on tab change, a selection tick on toggles and sliders, an impact on
  play and pause, like, and add to queue, a success pattern on completed downloads, an error
  pattern on failures. Keep them sparing and consistent.
- Gestures: always paired with a visible control. Swipe the mini player or artwork for next and
  previous. Pull down to refresh on Home. Swipe down to close the player and sheets.
- Loading: show the skeleton immediately; never a blank screen.
- Reduced motion: when the system disables animations, springs become instant fades.

## 6. Order of work (one batch, one build)

1. Component theme file covering section 2, wired in for light and dark.
2. Home top bar and Settings shortcuts; the mini player's empty state.
3. Lyrics view, queue sheet, artist header, Library rows, Settings grouping.
4. Haptics helper and its call sites.
5. Fix: starting the playback service can crash when the app opens while the phone is asleep.
6. One build; fix compile errors; install; review on the phone.

## 7. Not in this batch

Search category grid, a custom typeface, animated transitions between the folded and unfolded
bar, long-press context menus with a preview, per-screen empty-state artwork.

## 8. How we will know it worked

- Every screen shows only the flat components above; no outlined, glowing or bevelled control.
- A scroll through Home, Library and Search shows large titles collapsing and the bar folding.
- A track plays, the player tints, lyrics scroll, the queue reorders.
- No crash opening the app with the screen off.
