# Data Show — overall handoff (versionCode 19)

This single file replaces all earlier handoffs (Final handoff, v1.4, v1.5).

## 0. How to work with the user
- Writes Bengali in English letters (Banglish). Reply the same way: short, simple, phone-friendly, at most one or two questions.
- Has a usage limit and cannot run the app while chatting: show a working HTML preview (publish as an Artifact) first, deliver ONE GitHub-ready zip and ONE handoff when asked.
- App name everywhere: **Data Show**. Do not reintroduce "Data Store", "Share Reader", "ShareReader" or "Data View" as visible names. Internal ids that must stay (changing them would break installs/updates/data): Java package/applicationId `com.sharepricereader.app`, IndexedDB name `ShareReader_OriginalFiles_v1`, localStorage keys `ds_projects_v1`, `ds_design_v1`, `ds_theme_v1`.

## 1. Project facts
- Android WebView app. All UI and logic: `app/src/main/assets/index.html` + `css/` + `js/` modules (see ARCHITECTURE.md; no build step, libs in `assets/libs`). Java: `MainActivity`, `Updater`, `PatchEngine`, `ShareProvider`. Build: `./gradlew assembleDebug` (JDK 17) or the GitHub workflow `.github/workflows/build.yml` (artifact `DataShow-apk`, also a Release). No Android SDK in the sandbox, so no APK has ever been built or run on a real phone.
- GitHub build: upload the zip contents to a repo root, then Actions > Build > download `app-debug.apk`. Update README (Bengali) when features change.
- In-app update replaces only `index.html` (patch text: `#SRUPDATE`, `@@VERSION`, `@@WRITE index.html` ... `@@END`, see `example-update.txt`, `PatchEngine.java`). Java features (share, web fetch, camera) need a new APK.

## 2. What exists (all done)
- **Bottom navigation** on every main page: Files | Cards | big + (Add) | Table | Compare; handle `#navTog` hides/shows it; old top tabs hidden. Cards = the old Show tab. Add (+) menu: QR code, ID card, PDF, Photo (preview box `#pvBox`, send ➤ puts data into the app), Import files, New folder, Paste data, Add card.
- **Website box** `#urlIn` (Scrap/Add page): `Android.fetchUrl` -> sanitized text/tables in `.pvweb`, select/copy, send.
- **Share** Excel/PDF/text via `ShareProvider` (Cards Export menu only, not Table/Compare).
- **Files page** (`#filesWrap`): new front page `#homeBox` (search, Import/Paste boxes, shortcut banner, Tools Cards/Table/Compare, Import grid, Export Excel/PDF/Clear data/Customize) above the old Documents section (`#tabDocuments`, folders + recent files). Home buttons use `data-hm` and one delegated handler (end of the script); errors show a toast (`gnToast`), no `alert()`; Clear data needs two taps within 4 s; export without data shows a toast.
- **8 card layouts** (`#dLay`): `classic`, `port` (old, untouched) + engine layouts `ds` (Data Show, default for new installs), `pp` Portfolio+, `cp` Classic+, `br` Broker, `cr` Compact row, `ti` Tile. Old saved keys stack/compact/tile/ledger/strip/broker migrate in `applyDesign()`; a user's saved layout/style is kept.
- **Card engine** (`engine.js` section in index.html: `dxCard()`, `DXTP` placement strings, `DXSZ` sizes, `DXTL` labels, `design.dx` state via `dxEnsure()`, UI `#dsOpts` = `renderDsOpts()` + `dxWire()`): Fields (show, label, slot, new-line, custom fields), Size and spacing sliders (Name, Label, Number, Price text, Price box height, Price box width, T.AMT/T.AVG box text, line gap, label gap, padding, radius), Colors (Buy/Sell price swatches, Name, Label, Number, Buy/Sell card, pill/box, Gain +/-). CSS classes `.dsx .t1-.t6 .dpr .dnm .dbuy .dsell` (renamed to avoid clashes with app CSS).
- **12 card styles** (`STYLES`, CSS `.st2`..`.st11` + font rules at the end of `<style>`), each with its own font and color treatment, driven by `--h`; user font/color overrides win. Tested: 8 layouts x 12 styles, no overflow at 390 px.
- **Calculation** (per name, oldest to newest over all rows, in `render()` -> `runD`): running T.AMT/T.AVG/TOTAL; gain per sell row; `design.gainM` = `low` (lowest earlier buy, default), `cheap` (cheapest buys matched), `avg`; `design.sellAt` = `avg` (previous T.AVG, default) | `price`. Sample check: gain +4.80 / +0.70 / -6.20; TOTAL 1,2,3,1,2,3,4; T.AMT 45, 81.70, 114.30, 38.10, 69.60, 101.60, 131.50. Card order `design.cOrd` old|new (same name; default oldest first, set in `sortRows0`).
- **Pinned top buttons** (v1.6): the Cards toolbar `.tbar` is `position:sticky` when `body.tbpin` (default on, `design.gPin`).
- **Gain on the Total/Average card** (v1.6, `gainBlock()` in `render()`, events from `gev`): lines grouped by month (default) or date; period `design.gPer` = `latest` (latest month, default) | `all` | `YYYY-MM`; tapping the card header "All ▸ / Latest ▸" toggles latest/all (capture-phase listener in `gnWire()`); bottom line "Total gain" (green) or "Total loss" (red, when the all-time sum is negative); can be hidden (`gOn`, `gTot`). Works in every layout (classic, port, engine) because it is added in `sumBody()`.
- **Customize > Design tab** (no extra Customize button anywhere): "Total card gain & top buttons" (`#gnOpts`: Pin top buttons, Gain show/hide, by Month/Date, period, total line, Gain method, Sell reduces T.AMT at, Card order), plus the per-layout `#dsOpts` panel. All auto-saved in `design`; new keys have no required defaults (code uses `||` / `!==`). "Reset design" restores defaults and re-sorts.

## 3. Test status
Headless Chrome (Playwright) at 390 px with seeded data: Files page and all its buttons (no console errors, no covered buttons), crawl of every button in Files/Cards/Scrap with a stubbed `Android` bridge (0 errors), 8 layouts x 12 styles, Customize options, old saved data migration, gain/month/date/toggle/pin tests, standalone preview. `node --check` OK.
Reported by the user but NOT reproducible in headless: "many Files buttons do nothing / console errors". Hardened (try/catch + toast, no alert, safe Clear). If it persists on a phone, ask which button and read the message shown.

## 4. Known gaps / ideas
- Never built or run as an APK; Photo/ID card are stored as images only (no OCR); QR needs `BarcodeDetector` (missing on some WebViews); web preview shows text/tables only; Share only on Cards Export menu.
- "Total gain" is all-time per name; period lines above it follow the selected month/date. Gain ignores the Buy/Sell/Net filter and uses all rows.
- Preview for the user: build a standalone copy (inline `libs/*.js` into the HTML, seed demo data under `ds_projects_v1`) and publish it as an Artifact.

## 5. Notes additions
- Notes: `#ntS` screen, script block "Notes", CSS block "Notes" in the main `<style>`; storage `ds_notes_v1`; note `fld` = folder `nid`. Note tool is FIRST in the Tools grid (`data-note="new"`), + menu has Note (`data-add="note"` -> `__ntNew`), folder "+ New note" creates inside the open folder, folder card counts include notes. Add card -> `__openAddCard` -> Cards page (`show`).
- Bug fixed: Notes CSS had been injected inside jsPDF (global `</style>` replace in the preview build script). Never inline libs into index.html for the APK; the preview build must escape `</script` and must not do a global `</style>` replace.
- No Android SDK in sandbox: APK still not built here; use GitHub Actions or in-app update zip.

## 6. QR / photo editor / preview (v+1)
- `libs/qrdec.js` = offline pure-JS QR decoder (`window.DSQR.decodeGray`), loaded in index.html. `offlineQr(src)` tries 1600/1100/700 px; order in QR scan: BarcodeDetector -> DSQR -> ZXing (needs internet).
- QR and Barcode now open in the photo editor (`pvImgEdit`): crop/rotate/Magic/B&W re-scan automatically; `🔍 Scan` button; result box `#pvQrOut` with Copy / Scan again.
- Photo editor bar has 💾 Save (JPG to Downloads via `Android.save`, browser download in web) and 📤 Export (JPG, PNG, PDF, Share). Helper `outFile()`.
- Preview fills the page: `pvFit()` sizes `#pvBody` down to the bottom menu; the Recent strip hides while a file is open; image canvas is centered (`.pvbody.pvimg`).

## 7. In-app update fix (versionCode 9)
- Bug: after "Update from file" the dialog showed the new label but the old page kept loading, because `shouldInterceptRequest` is not reliably called for the main `file:///android_asset/index.html` page.
- Fix: `MainActivity.loadHome()` reads `filesDir/www/index.html` and loads it with `loadDataWithBaseURL(HOME, html, ..., HOME)` (same base URL, so localStorage data stays). Used in onCreate and `reloadApp()`. `Updater.intercept` stays for sub-files (libs).
- This APK also has "update 2" (T.AVG fix, settings icons) already baked into `assets/index.html`.

## 8. Update 3 (filters, gain/loss, QR gallery)
- Filters: 5 per row by default (`design.fcols=5`, migration `fcV`), extra "Filter ▴/▾" button (`#ctrlA`, shares state with `#ctrlT`, `ds_ctrlhid`).
- Total/Average card gain (`gainBlock()`): collapsed by default (`design.gMin`, tap header `data-gmin`), expanded shows per-month gain and loss (red), Total gain / Total loss / Net. Latest/All toggle is its own `data-gtog` span.
- Price box width: removed `.dsx.t1>.R{min-width:84px!important}`; `.R` now `width:colW`.
- Net filter: net rows keep `__src` (original row) so Total/T.AMT/T.AVG show; running values are computed over the net rows (`netRun`).
- QR/Barcode camera: "Gallery / Files" opens `#pvQrIn` (image or PDF); PDF pages are rendered and scanned (`qrPickFile`). QR result shows links as buttons (`qrLinks`); tap = loads it in the website preview (`#urlIn` + `#urlGo`).

## 9. Native browser for website links (versionCode 10, needs new APK)
- Slow/blank websites and dead YouTube came from the in-page preview (HTML downloaded via `Android.fetchUrl`, scripts removed, iframe blocked by YouTube).
- New `BrowserActivity.java` (registered in AndroidManifest): full WebView with JS, cookies, normal Chrome user agent, progress bar, fullscreen video, top address/search bar (non-link text = Google search), bottom back/forward/reload and "Send text ➤" (selection or whole page text -> `BrowserActivity.resText/resUrl` -> `window.__browserSend` -> `pasteData`).
- `Bridge.openBrowser(url)` in MainActivity; `pvWeb()` in index.html calls it when `window.Android.openBrowser` exists, otherwise the old in-page preview still runs (browser/web preview). QR result links and the QR send button also go through `pvWeb()`.
- Not compiled or run in the sandbox (no Android SDK): check the GitHub build log first if it fails.

## Preview layout update
- Zoom (− % + Fit) and Full moved into the preview header row (`#pvZm` inside `.pvhd`), Full is now an icon only.
- Excel preview auto-fits all columns to the width on open / sheet change; Fit button re-fits (`xsAutoFit`). Zoom min is 35%.
- Edit panels (`#pvFmt` for PDF, `#pvShEd` for Excel) are smaller and minimizable (▾/▴, body class `edmin`).
- Side margins trimmed while a preview is open (body class `pvon`, set in `pvFit`).
- PDF reader feature list (single/continuous, search, thumbnails, markup, OCR...) is NOT built yet in this zip.

## PDF reader panels (v2, in index.html, script id `pvReader2Js`)
- Bottom bar `#pvRd` (shown when a PDF is open): Search, Pages (thumbnails), Marks (bookmarks), Markup, OCR. Panels open in overlay `#pvRdP`.
- `pvPdf` fires `document` event `pvpdf` when pages are ready; `window.__pv` exposes the `pv` state.
- Markup: SVG layer per page (`svg.pva`, page units = style width/height), saved per file in localStorage `dsPdfAn_<name|size|mtime>`. Tools pen/pencil/marker/eraser/box/circle/arrow/line/text/note + highlight/underline/strike on selected text, color, size, opacity, undo/redo, hide all.
- Bookmarks `dsPdfBm_<key>`; OCR text `dsPdfOcr_<key>`. OCR uses tesseract.js from CDN (needs internet once), page range, language, compare with scan, find, Copy, TXT, DOCX (JSZip).
- Not done: dark mode, single-page mode, last position, saving a PDF with markup baked in, callout.

## Excel zoom + recent strip fix
- Excel preview zoom no longer uses CSS `zoom` (it cut the right columns). It scales via CSS variable `--z` on `#pvShIn` (font, padding, row height, column min-width) so horizontal scroll reaches the last column. `#pvShIn` has extra right/bottom padding.
- Recent files strip `#pvRecent` is now always visible under the preview (compact size while a preview is open, `body.pvon`).

## Tight layout while previewing
- Recent strip: no scrollbar, smaller thumbnails, sits right above the bottom nav. `#msg` status text is hidden while a preview is open (`body.pvon`), so the preview area is taller.
- When there are no recent files and a preview is open, the recent strip is hidden so the preview fills that space (`pvFit`, `renderRecent`).

## 10. v3.1 cleanup / speed (versionCode 19)
- **Lazy libs**: no `<script src="libs/...">` in index.html any more. `need(k)` (in the main script, also `window.need`) loads on demand: `pdf` (pdf.min + pdf.worker, sets workerSrc), `xlsx`, `jspdf`, `zip` (JSZip), `qr` (qrdec). Every call site awaits it first (`(await need('pdf'),pdfjsLib).getDocument(`, `await need('xlsx')` at the top of XLSX handlers, etc.). New code that uses XLSX / jspdf / JSZip / pdfjsLib / DSQR MUST `await need('<name>')` first, never reference them at startup. Startup test: 0 files from `libs/` requested at boot.
- **Customize > Design** is now 6 `<details class="cgrp">` groups (Style open by default, Colors, Card content, Total card & gain, Lists & pages, Advanced) + Reset design outside. Only DOM moved: every element id and every handler is unchanged (verified by diffing the id list). Body class is `cgbd` (`.cgb` is used by another feature, do not reuse it). Add new options inside the matching group.
- **Speed**: `dxSave()` now saves after 300 ms and renders once per animation frame (30 slider events: ~1000 ms -> ~1 ms in the 3000-row test). Removed the dead duplicate `tablePdf` (the second definition always won).
- Java untouched (still never compiled here). `MANUAL.md` = detailed user manual (Bengali), keep it in sync when options change.
- Test recipe: serve `app/src/main/assets` with `python3 -m http.server` in the SAME shell command as the Playwright script (the server dies between tool calls); seed `ds_projects_v1` with `add_init_script`.

## 11. Photo editor v2 (versionCode 12)
- New full-screen editor (Light/Color/Filters/B&W/Detail/Retouch/Draw/Text/OCR/Resize/Export, undo/redo, hold-to-compare, multi-photo, customizable toolbar `ds_pe_v1`) lives in index.html as `<script type="text/plain" id="peSrc">` (a full HTML doc; `</script` escaped as `<\/script`) and opens in an iframe `#peFrame` via `window.__peOpen(file,{onUse,onOcr,onSave})`; `__peClose()` removes it. Own document = no CSS/id clashes with the app.
- Entry: first button "Edit" (`data-i="edt"`) in the Photo/ID/QR preview bar (`ibar()` in the "photo editor + OCR" block). ➤ = `peBack()` puts the edited image back into the preview (`pvImgEdit`, QR/Barcode re-scan); OCR tool -> `runOcr`; Save/Export -> `outFile()` (Downloads via `Android.save`).
- Old quick bar (Rotate/Crop/Magic/...) is unchanged. Headless test OK (open, slider, ➤ back, OCR button, close, 0 errors). Not run on a phone/APK.

## 12. Photo editor v3 (versionCode 13)
- `peSrc` replaced with the pro editor (same entry/API: `__peOpen`, `onUse/onOcr/onSave`, `DSPhotoEd.open`). 23 tools: Crop, Rotate, Light (+histogram, auto enhance), Curves, Color (+auto white balance, hue shift), HSL (8 colours), Split tone, Filters (34 presets with thumbnails, incl. Pixelate/Sketch/Cartoon/Halftone/Light leak), B&W, Detail (sharp/structure/noise/glow/vignette/grain), Lens blur, Color splash (tap photo), Retouch, Brush (dodge/burn/blur/mosaic/heal/clone/color+/color-/warm), Draw, Shapes, Text, Stickers, Frames, Expand, OCR, Resize, Export. Toolbar customizable (`ds_pe_v1`).
- Pixel pipeline `fx(src,p,dst,specialName,k)`; params default in `P0`; presets `FL` (param deltas), `SPF` (special filters). Brush edits are baked into `it.base`.
- Standalone preview/test copy: `datashow-photo-editor.html` (outside zip). Headless test: all tools, 34 filters, 9 brush modes, shapes, 0 errors. Not run on a phone/APK.
- UI (v3 layout, like the reference screenshot): header icons only (close, undo, redo, compare, add photo, send); bottom sheet with round tool buttons (label above icon) in 3 tabs Adjust / Edit / More (Customize button at the end of each strip); opening a tool shows its options + ✕ title ✓ bar (✕ reverts the tool's changes, ✓ confirms; crop/expand/straighten apply on ✓). Slider tools show one thin teal slider + round option icons (dot = changed). Colour `--ac` teal. Standalone copy: datashow-photo-editor.html.

## 13. In-app camera + gallery thumbnail (versionCode 14, needs new APK)
- Why the phone's own camera opened before: no CAMERA permission and no `onPermissionRequest`, so `getUserMedia` failed and the app fell back to `pvCamIn` (native camera). Fix in Java: `AndroidManifest.xml` has CAMERA (+ optional camera feature); `MainActivity` has `onPermissionRequest` (video only, asks the runtime permission, `REQ_WEBCAM`, `onRequestPermissionsResult`). Not compiled here (no Android SDK): check the GitHub build log first.
- In-app camera (`#scanCam`): bottom-left now shows a thumbnail of the latest photo from the Recent store (`#camTh`, `camThRefresh()`); tap = `camThOpen()` closes the camera and opens that photo in the preview page (`openV`); with no photo yet it opens the gallery picker. Gallery / Files stays on the right. Headless test OK (fake camera, thumbnail shown, tap opens editor, 0 errors).
- Editor icon style: rounded-square tiles (54px, `--chip`) with white/dark line SVG icons (`IC` map + `I(name)` in the editor script), label under the tile; same icons for slider option rows. Dark chip colour `#262d36`.
- Editor icon style v2 (matches reference 'Quick Edits' screenshot): 60px rounded-square tiles (radius 13px, `--chip` #262b34), 32px thin (1.5 stroke) white line icons from `IC` map, 12px full-opacity label under tile. Icons overridden in `Object.assign(IC,{...})` block.
- Editor v3 tools (Edit tab): Layers (non-destructive image/text layers: move by drag, size, rotate, opacity, order, hide, delete; stored in `it.layers`, included in undo snapshots, drawn by `drawL` in `doRender` and `renderFull`), Insert (add image layer / replace photo), Backgrounds (solid, gradient, blur, padding, corners), Shadows (drop shadow, outline, reflection, backdrop). Not done yet: AI cutout, perspective transform, face retouch.
- Editor v4: Transform (vertical/horizontal perspective, strip-based, `perspPanel`) and Reshape (push/warp brush for face reshaping, `warpOn`/`warpPanel`) added to the Edit tab. Still missing: AI cutout/relight, automatic face detection.
- Editor v5: Face tool (tap feature, pick Eyes/Nose/Lips/Chin/Cheeks/Face slim, amount + area, `facePanel`), crop rule-of-thirds grid + ring handles + 5:4/2:3 ratios, Lightroom-style gradient tracks (`GRD`) for Temperature/Tint/Saturation/Vibrance.
- Editor v6: crop ratio picker is now a scrollable row of ratio-shape icons (`.rt`, `RT` list) like the reference; Apply crop button hidden, the top-right check applies the crop.

## Photo editor: Face retouch (reference-style)
- Edit > Face now has group tabs (Nose, Face, Eyes, Eyebrows, Mouth) and option tiles (Face shape, Chin shape, Width, Cheekbone, ...). A white dashed oval marks the face: drag it to move, "Face size" slider resizes it (FaceDetector auto-places it where available). One slider per option gives a live preview (`facePrev`); ✓ or Apply commits (`applyFace` -> `wReg` pixel warp); ✕ or leaving the tool discards. Code: `FP`, `FG`, `FIC`, `wReg`, `applyFace`, `facePanel` in script `peSrc`. Tested in headless Chromium (no errors, preview changes, result kept after ✓).

## 14. Smart Selection for website visit (needs new APK for the native part)
- `assets/libs/smartsel.js` = offline selection engine `window.DSSel` (lazy: `need('sel')`). Floating "Select" button turns it on (links stop opening while ON). Tap = smart pick (Cell > Column > Row > Table, List item > List, Heading, Paragraph, Section, Full article); tap again or swipe left = bigger, swipe right = smaller, double-tap = full article, long-press = custom word/range. Toolbar: Copy, Send to Data Show, Translate (Google), Share, Search, Save, Speak (speechSynthesis), Dictionary (web), Highlight, History, Reader, Settings (toolbar items, row/column select, highlight colour; localStorage `dssel_cfg`). Keys: arrows / Esc / Ctrl+C.
- Native: `BrowserActivity` injects the file on `onPageFinished` (`injectSel()`), bridge `DSSelB` (`send/share/open/addHist/hist`, history in SharedPreferences `dssel`). Send -> same path as "Send text" (`resText` -> `__browserSend` -> `pasteData`).
- In-page preview (`pvWeb`, browser/web): `dsSelStart()` runs the engine on `#pvWebC`; Select ON swaps the iframe for the readable view, OFF restores it.
- Tested headless (cell/column/row/table, list, heading, double-tap, send, highlight, history, 0 errors). Not compiled or run on a phone: check the GitHub build log first. Not done: Translate/Dictionary are online (open Google), no embedded-iframe selection, no swipe-based range handles.

## 6. Photo flow v4
- Photo pick -> `dsCrop()` auto-detect crop screen (corner + edge handles, Rotate L/R, No Crop, Next; perspective warp) -> preview.
- Preview bar `#pvImgBar`: small line icons Edit | Crop | Rotate | OCR | Draw | Undo | Save | Export. Crop/Rotate/OCR/Draw open the editor straight on that tool (`__peOpen(..,{tool})`).
- Edit -> advanced panel `#pvAdv` slides in from the right (Auto crop, Crop, Straighten, Perspective, Light, Color, Filters, ... All tools).
- Bottom nav + (`#docBottom`) auto-hides while a photo/ID preview is open (`navAuto()`), returns on close; `navAutoOn` must stay `var`.

## 15. Daily data (live DSE table) on the Table page (v5 zip)
- Table page now has two buttons on top: **My data** (old table, untouched) and **Daily data** (live DSE price table). Mode saved in `ds_lt_mode`. Only on the Table tab (not Compare).
- Code = one block at the END of index.html (before `</body>`): `<style id="ltCss">` + `<script id="ltJs">`. Everything inside an IIFE, all ids/classes start with `lt` (the app already has `.wrap`, `$`, `esc`, so never rename them to short names). It hooks `window.setTab` (wrapper) and reads `curTab`.
- Data: `Android.fetchUrl('https://www.dsebd.org/latest_share_price_scroll_l.php','lt<N>')` (browser: fetch + CORS proxies). Result id starts with `lt`; `window.__urlRes` is replaced by a dispatcher (defineProperty) that routes `lt*` ids to this feature and the rest to the Website box handler. `parse()` finds the table by the header text TRADING CODE + LTP and maps columns by header name, so column order changes do not matter. **The real DSE page was never fetched in the sandbox (no network); tested with a fake table only. If it fails on the phone, the status line shows the reason; check `parse()` first.**
- Live: auto refresh default 10 s (Off/5/10/30/60), timer runs only while Daily data is visible and the page is not hidden; refreshes on return. Last data cached in `ds_lt_cache` (+ `ds_lt_time`) so the table shows instantly and offline.
- Table: sticky header + first column, sort, search, Sector/Category filter, column show/hide + up/down order + presets (Basic/Trade/All/Reset), Hide table (summary chips stay), Compact, green/red row flash on LTP change. Prefs in `ds_lt_prefs`.
- Sector / Category / EPS are NOT on the DSE page: "＋ Details" accepts pasted text with a Code header plus Sector, Category, EPS columns (tab or comma), saved in `ds_lt_det`, joined by Code. P/E = LTP / EPS.
- Headless test (stub Android bridge): switch, live fetch, id routing, 5 s auto refresh + flash, columns, sort, search, hide, details import, back to My data stops the timer, 0 errors, no horizontal overflow at 390 px. Not run on a phone/APK. Only `index.html` changed: in-app update works, no new Java needed.

## 16. Auto fill Sector/Category/EPS (StockChartBD API)
- `autoDet()` in `ltJs`: `https://stockchartbd.com/api/v1.php?endpoint=companies` (JSON, first array found; keys symbol|code|trading_code|ticker, sector|sector_en|sector_name, category|cat) and `https://stockchartbd.com/api/export.php?type=fundamentals` (CSV, columns symbol/code + eps by header name). Free, no key, CORS open, limit 120 requests/hour/IP, so it runs 2 calls once per day when Daily data first opens (`ds_lt_detT`), or by the "⬇ Auto fill" button in "＋ Details". Result goes into `ds_lt_det` (same store as manual paste); P/E = LTP / EPS.
- **The real API was never opened here (site blocks automated access via robots.txt); field names are guesses with fallbacks. On a format mismatch the message in the Details panel shows the keys/header it found: fix `autoDet()` from that.** Tested with stubbed JSON/CSV (quoted fields, BOM, negative EPS), 0 errors.
- Terms of that site: credit "DSE Intelligence" and dsebd.org (shown in the note under the table). Do not poll it for live prices (limit); live prices stay on the DSE page.

## 17. Auto fill now uses only the fundamentals CSV (format verified from a user paste)
- `autoDet()` makes ONE request: `https://stockchartbd.com/api/export.php?type=fundamentals`. Header (verified): symbol, company, sector, category, price, reported_year, eps, eps_growth_pct, nav_per_share, pe, price_to_book, roe_pct, dividend_pct, dividend_yield_pct, profit_mn, health_score, health_band. Delimiter auto-detected (tab or comma), BOM stripped. The `companies` JSON endpoint is no longer used.
- Stored in `ds_lt_det` per Code: sec, cat, eps, yr, nav, roe, div, dy, risk(health_score). New table columns: EPS year, NAV, P/B (= live LTP / NAV, blank if NAV <= 0), ROE %, Div %, Div yield %, Risk score (higher = riskier). P/E stays live LTP / EPS (blank if EPS <= 0). Old saved column order gets the new columns appended, hidden by default.
- The manual "＋ Details" paste also accepts a `symbol` header. Tested with the exact pasted TSV rows (0 errors). The real file download itself was never opened here.

## 18. Auto fill v2: many free sources (index.html only, in-app update works)
- `autoDet()` in `ltJs` now reads the source from select `#ltSr` (saved `ds_lt_src`): `fund` StockChartBD fundamentals CSV (format verified), `comp` StockChartBD companies JSON (`api/v1.php?endpoint=companies`, field names guessed, matched by alias), `all` both in order, `url` = the user's own link in `#ltUr` (saved `ds_lt_url`). Daily 1 auto-run uses the chosen source.
- Generic importer: `toTable(text)` detects JSON (first array of objects, up to 4 levels deep), HTML table (header row with a Code/Symbol/Trading Code cell), CSV or TSV; `applyTable()` maps columns by header-name aliases (`AL`: code, sector, category, eps, reported_year, nav, roe, dividend, yield, health_score) and writes into `ds_lt_det`. Code is looked up in every alias column per row (mixed JSON keys work). To support a new source, add an entry to `SRC` and aliases to `AL`.
- Result message lists each source with company count and fields found; on mismatch it shows the header found.
- `bdshare` (Python) cannot run inside the APK; it only scrapes dsebd.org, which Daily data already reads directly. The live prices still come from `latest_share_price_scroll_l.php` only.
- Tested headless with stubbed responses (TSV, JSON with mixed keys, HTML table, bad file, empty/invalid link), 0 app errors. The real StockChartBD files and any other site were NOT opened here (no network).

## Update 6 (v11) — index.html only
- Table > Daily data: default columns now Code, LTP, Change, YCP, High, Low, Trade, Value, Volume (Change % replaced by YCP; saved prefs migrate once via `S.ycpV`; Change % still available in Columns).
- New buttons in the Daily data row: ➤ Send (shares the visible, filtered table as .xlsx through `Android.shareFile`, browser download otherwise) and ⬇ Export (Excel, CSV, Copy table). Both use `window.save` and the global `need('xlsx')`.
- The screenshot with only 3 rows came from the web preview, which has 3 demo shares and no network. In the app, Refresh loads every share from dsebd.org.

## Update 7 (v12) — index.html only
- Cards toolbar **Clear** (outside Select mode) used to delete all data on a single tap. It now needs a second tap within 4 s (button shows "Tap again"); `window.__clrForce` lets the Settings "Clear data" flow, which has its own confirm, bypass it. Select mode > Delete behaviour is unchanged.

## Update 8 (v13) — index.html only
- Cards page: the **Select** button is hidden (`#selB` kept in DOM because `selUI()` references it). Selection now starts by **long-press (~0.5 s)** on a content or average card (pointer events on `#list`, the click that follows is swallowed). Selected cards get a **radio circle on the left** (`.item.selm::before`, filled when `.picked`); the bottom bar (All / Delete n / ✕) is unchanged. Daily and Future cards are live widgets, not data rows, so they have no radio.
- Cards toolbar stays after **Clear** or when there is no data: `merge()` now sets `#res.empty` (CSS hides everything except `.tbar`, `#cust`, `#pastebox`, `#emptyMsg`) instead of hiding `#res`; `render()` removes the class. `#noData` is no longer shown.
- Table > Daily data > **➤ Send** now opens a small menu (`#ltSp`): **Cards**, **Compare**, **My data**, **Share Excel file**. The first three call `addFile('DSE-prices-<local date>', [['Date','Name','Price'],...])` with the visible filtered rows (Price = LTP, or YCP if LTP is 0), so the same data feeds Cards, Table > My data and Compare, then switch to that page. Sending twice on the same day replaces the same entry. ⬇ Export is unchanged.

## Update 9 (v14) — index.html only
- `need(k)` now tries the bundled `libs/…` file first (this is what the APK uses, works offline) and only if that file cannot be loaded (single-file web preview) falls back to the matching cdnjs copy (`CDN` map: pdf.js 3.11.174, xlsx 0.18.5, jsPDF 2.5.1, JSZip 3.10.1). `smartsel.js` and `qrdec.js` are custom and have no online copy. Nothing changes inside the APK as long as `assets/libs` is bundled.

## Update 10 (v15) — index.html only
- **PDF/text import column names**: `addFile()` used to label every column "Col N" when the first full-width row was a data row (statement PDFs whose header row wraps or has a different cell count). New helpers `findHeader` (keyword header row among the first 80 rows), `fitLabels` (folds wrapped fragments such as "Transactio"+"n", merges split words such as "Ledger"+"Balance" when the label count is one more than the data width) and `nameCols` (uses `automapOf` to name the date/type/name/quantity/price columns Date/Type/Name/Quantity/Price when a label is generic or duplicated). Repeated header rows on later pages are dropped. Tested headless on two synthetic ledger PDFs (header count equal to / one more than the data width).
- **Daily data > 📋 Paste**: new panel to load the DSE price table by pasting it (tab/space separated rows `# CODE LTP HIGH LOW CLOSEP YCP CHANGE TRADE VALUE VOLUME`, or page HTML). Result goes through the same `derive` / `ds_lt_cache` path as Refresh, so Table, Daily card and Send all use it. This is the fallback when `dsebd.org` cannot be fetched (web preview, no internet). No price snapshot is bundled; the APK still uses `Android.fetchUrl` on Refresh.

## 19. My portfolio on Table (versionCode 20, Gmail part needs new APK)
- New `js/app/portfolio.js` (replaces the v18 gmail-import.js; ids/classes `gp*`, `gm*`). Third button **💼 My portfolio** in the `#ltSw` row (My data | Daily data | My portfolio); its panel `#gpBox` shows only on Table > My data. Storage `ds_pf_v1` = `{open:{date,rows:[{code,qty,avg}]}, trades:[{date,type,code,qty,rate,conf}], sent}`.
- **📕 Portfolio PDF**: `parsePortfolio()` finds code + numbers per row (qty = first whole number, avg = next 2-decimal number, header names used when the cell count matches) and the "as on" date, then shows an editable preview (`NAME QTY AVG` lines + date). Save = opening holdings. Parse failure / unreadable PDF = same preview empty, the user types the lines (manual fallback). `parsePortfolioLB()` (verified on the real LankaBangla CLIENT WISE PORTFOLIO STATEMENT of client H7083: 45 shares, total cost 35,266.93 matches the PDF line `Total:`): row = CODE CAT SALEABLE LIEN LOCK TOTAL-QTY COST-PRICE COST-AMT MKT-PRICE MKT-VALUE %PORT GAIN %GAIN; qty = Total Qty (includes today's unsettled buys), avg = cost amount / total qty, date = "Printed On" (the statement's "As On" showed a later day, 25-Oct, while it was printed 19-Oct), so the 19-Oct trade confirmation is ignored as already included. Codes can start with a digit or contain brackets (1JANATAMF, AMCL(PRAN)). Other layouts still use the generic `parsePortfolio()` + editable preview.
- **🧾 Buy/Sell PDF** (multi-select) and **📧 Gmail**: `parseTrade()` (LankaBangla "TRADE CONFIRMATION" layout) -> `addConfirmation()` (key client|date|confNo; same confirmation replaced, never doubled). Holdings = opening + trades dated AFTER the opening date, date order, average-cost method (`calc()`); older trades are kept but greyed/ignored. Trade list is date-wise with ✕ delete. Sell without holding shows a warning.
- **➤ Send to Cards**: `addFile('My portfolio', [Date,Type,Name,Quantity,Price rows])` (opening rows as Buy at the opening date + counted trades), then `setTab('show')`. After the first send every change re-sends silently (`changed()`).
- Wrong file in the wrong button: a confirmation given to Portfolio PDF is added as a trade; a portfolio given to Buy/Sell shows an error (and tries Gmail when configured).
- Gmail AUTO = `Android.gmailFetch` -> `GmailFetcher.java` (IMAP + App password, android-mail 1.6.7 in app/build.gradle) -> `window.__gmRes`. Failure/no bridge -> file picker (+ "Choose PDF" button, because WebView may block a picker opened without a tap). Only confirmations are imported from Gmail, other PDFs are skipped. ⚙ settings (`ds_gm_cfg`): address, App password, broker email, days, PDF password, auto check every 6 h when the app opens (`ds_gm_seen`).
- Tested headless (390 px, stubbed Android, real Trade Confirmation PDF + synthetic portfolio PDF): preview/save, add trade, re-add = no double, send to Cards (all names shown), Gmail auto + resend, opening date after trades, wrong-file cases, manual typing, hidden on Daily data, 0 errors. `GmailFetcher.java` only type-checked against hand-written stubs (no Android SDK here): check the GitHub build log first.

## 20. One card per share/date (versionCode 23)
- `cardRowsOut()` in `js/app/portfolio.js`: counted trades with the same date + Buy/Sell + share are merged into ONE card row (qty added, price = weighted average). Buy and Sell on the same day stay separate. Re-sending replaces the file, so cards never double.

## 21. Portfolio page layout (versionCode 24)
- Top row on Table: **My data | Portfolio | Daily data** (one line). Portfolio panel: all 7 buttons on ONE line (Portfolio, Buy/Sell, Gmail, settings, Cards, Edit, Reset; labels in `title`), summary line `#gpSum` hidden, success messages are toasts; only errors and the opening-preview note stay on the page. When Portfolio is open, My data is not highlighted (`#ltSw.gpopen`).
