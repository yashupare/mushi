# Mushi design spec (research notes)

Working reference for the Mushi UI revamp. Principles taken from Apple's Human Interface
Guidelines (HIG) and reporting on the current Apple Music app. We follow the principles, not
Apple's exact look or assets.

**Confidence labels:** `HIG` = read from Apple's published guidelines. `PRESS` = reported by a
news or review source, not confirmed by Apple. `UNKNOWN` = not found in any source I could reach.

Apple does not publish Apple Music's internal animation timings, curves or artwork scale
factors. Those are `UNKNOWN`; where this spec gives a number for motion it is our own choice.

## 1. Structure and navigation

| Item | What Apple does | Confidence |
|---|---|---|
| Tab bar | Navigation only, never actions. Always visible while moving between sections. Single-word labels. Filled symbols preferred. Don't hide or disable tabs; explain empty content instead. Red badges only for critical info. | HIG |
| Tab bar + mini player | The mini player is an accessory above the tab bar. On scroll down the bar minimises and the accessory moves inline beside it with fewer controls; scrolling up expands it again. | HIG, PRESS |
| Search | A dedicated trailing tab on iPhone, reachable from every view. Placeholder states scope. Searches as you type, shows recents and suggestions, results categorised, broad scope by default. Lyric search exists. | HIG, PRESS |
| Large titles | Large title on iPhone that shrinks into the bar on scroll and returns at the top. | HIG |
| Library | Library and Collections are separate; Collections has three layouts and can be collapsed and reordered; items can be pinned to the top. | PRESS |
| Home ("Listen Now") | Recently played, personalised mixes, recommended stations, favourite-artist picks. | PRESS |

## 2. Now Playing

| Item | Behaviour | Confidence |
|---|---|---|
| Artwork | Large and dominant; its colours tint the whole screen's background. | PRESS |
| Track change gestures | Swipe on the mini player, on the tab bar, or on the artwork to go next or previous (since iOS 26.1). Transport buttons stay under the artwork. | PRESS |
| Quick lyrics | Swiping down reveals quick lyrics access. | PRESS |
| Long press | Shows song credits, audio format and contributors. | PRESS |
| Artwork scale on pause | Artwork shrinks when paused and grows when playing. Common observed behaviour, factor unknown. | UNKNOWN |
| Volume | Use a system-style volume view, not a plain slider; don't change overall volume yourself. | HIG |
| Audio behaviour | Resume or not after interruptions depending on type; let other apps know when audio ends; allow rerouting. | HIG |
| Animated artwork | Some albums have artist-made motion artwork. Not for every album, so a static cover is normal. | PRESS |

## 3. Lyrics

- Time-synced: current line emphasised, other lines softer. Line-by-line on iPhone; word-by-word on Apple TV. `PRESS`
- Translation and pronunciation (phonetic) exist on iPhone and iPad, under or beside the lyrics. `PRESS`
- "Sing" lets people lower the vocals with a slider. `PRESS`
- Offline lyrics for downloaded songs arrived in iOS 26.2. `PRESS`
- Not every song is synced; some have static text or none. The UI must handle all three. `PRESS`

## 4. Components

| Component | Rule | Confidence |
|---|---|---|
| Buttons | Hit region at least 44x44 pt. A press state on every custom button. One or two prominent buttons per view. Labels start with a verb. Delayed actions show an in-button activity indicator. | HIG |
| Sheets | For a scoped task. One sheet at a time. Two detents: medium (about half height) and large. Resizable sheets show a grabber. Swipe to dismiss; confirm if there are unsaved changes. Done pairs with Cancel. | HIG |
| Context menus | Few items, about three groups at most. Hide unavailable items rather than dimming. Most-used item nearest the finger. Destructive items last and marked destructive. One submenu level. The preview's clip shape must match the image so corners don't jump during the animation. | HIG |
| Menus | Verb labels. Icons for all items in a group or none. About five items per submenu. | HIG |
| Lists | Let people reorder. Highlight the selected row persistently in navigation lists. Keep row text short; middle ellipsis when both ends matter. | HIG |
| Search field | Placeholder reinforces scope. Search as you type. Tokens only with suggestions. | HIG |
| Sliders | Minimum on the leading side. Don't use a slider for volume. | HIG |
| Toolbars | Symbols over text where the symbol is clear. Group by function, at most three groups. One prominent action, trailing. Standard Back; never a text button reading "Back". | HIG |
| Loading | Show something immediately. Placeholders while loading. Determinate progress when the duration is known. Let people do other things meanwhile. | HIG |
| Swipe, drag, long press | Standard gestures only for standard actions. Always offer a second, visible way. Give immediate feedback during the gesture. Don't conflict with system gestures. | HIG |
| Queue actions (swipe, reorder, Play Next / Later) | Exists, but the exact gestures and visuals were not in any source I reached. | UNKNOWN |
| Add-to-library and download states | Exists, but the exact visuals were not found. | UNKNOWN |

## 5. Type, colour, material

- Default body 17 pt, minimum 11 pt. Avoid light weights. Few typefaces. `HIG`
- iOS text styles (default size): Body 17/22, Headline 17/22 semibold, Title 1 28/34, Title 2 22/28, Caption 2 11/13. `HIG`
- Glass or blur effects: not in the content layer; use sparingly on the most important controls; keep text legible, with extra dimming over bright content. `HIG` The Nielsen Norman Group reports legibility problems with the glass effect in iOS 26. `PRESS`
- Prefer a monochrome or single distinct accent in the tab bar; avoid tab colours close to the content background. `HIG`
- Don't change artwork's aspect ratio. `HIG`

## 6. Motion and haptics

- Motion is purposeful, short, tied to the gesture, and never the only signal. People must not wait for it to finish. `HIG`
- Feedback motion follows the gesture: something revealed by sliding down is dismissed by sliding back. `HIG`
- Avoid adding animation to frequent interactions. `HIG`
- Haptics: selection (a single pulse for changing values), impact (light, medium, heavy, rigid, soft), notification (success, warning, error). Use sparingly and consistently, and let people turn them off. `HIG`

## 7. How this maps onto the Mushi code

Status as of this note. "Done" means built and installed on the test phone; it has not been
reviewed on the phone for every item.

| Area | Mushi file(s) | Status |
|---|---|---|
| Palette, type scale | `core/ui/theming/AppTheme.kt` | Done |
| Tab bar (flat) | `modules/shell/AppBottombar.kt` | Done; minimise-on-scroll not built |
| Mini player (flat) | `modules/shell/AppFloatingPlayer.kt` | Done; swipe to change track not built |
| Now Playing: tint, artwork scale, left-aligned title | `modules/shell/AppExpandedPlayer.kt`, `core/ui/theming/ArtworkTint.kt` | Done; swipe on artwork, long-press credits not built |
| Lyrics view | `modules/lyrics/*` | Not started |
| Home carousel and chips | `core/ui/component/FeaturedCarousel.kt`, `modules/home/HomeScreen.kt` | Done |
| Album / playlist header and track rows | `core/ui/component/CollectionDetails.kt`, `TrackList.kt` | Done |
| Cards | `core/ui/component/cards/PlayableCard.kt` | Done (touch shows play only) |
| Loading skeletons | `core/ui/misc/Shimmer.kt` | Done (flat tint) |
| Artist page | `modules/artist/ArtistScreen.kt` | Not started |
| Search | `modules/search/*` | Not started |
| Library | `modules/library/*` | Not started |
| List rows | `core/ui/component/cards/PlayableListTile.kt` | Not started |
| Track filter and sort bar | `core/ui/component/TrackList.kt` | Not started |
| Sheets, menus, dialogs | `core/ui/component/*Sheet*`, `TrackOptions.kt` | Not started |
| Settings | `modules/settings/*` | Not started |
| Haptics | none yet | Not started |

## 8. Open questions

- Exact motion timings and curves: choose our own (short, spring-based) and tune on the phone.
- Queue and download visuals: design our own consistent with the rules above.
- A custom typeface: still the phone's default font.
