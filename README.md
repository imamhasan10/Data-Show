# Data Show (Android অ্যাপ)

> 🧩 **কোড এখন আলাদা আলাদা ফাইলে:** `index.html` ভেঙে `css/` ও `js/` ফোল্ডারে ভাগ করা হয়েছে (logic বদলায়নি)। বিস্তারিত: [ARCHITECTURE.md](ARCHITECTURE.md)

> 📖 **সম্পূর্ণ ব্যবহারের গাইড: [MANUAL.md](MANUAL.md)** — সব option বিস্তারিত বলা আছে।

## নতুন: Auto fill-এ একাধিক free source

- Table > Daily data > ＋ Details-এ এখন **source বাছার menu** আছে: StockChartBD Fundamentals (CSV), StockChartBD Companies (JSON), দুটোই একসাথে, অথবা **নিজের link** (CSV / JSON / table-ওয়ালা page)।
- App header-এর নাম দেখে column মেলায় (Symbol/Code, Sector, Category, EPS, NAV, ROE, Dividend…), তাই column-এর ক্রম বদলালেও চলে।
- bdshare Python library, অ্যাপের ভেতরে চলে না; DSE-র দামের জন্য অ্যাপ সরাসরি dsebd.org পড়ে। শুধু index.html বদলেছে, তাই in-app update দিলেই চলে।

## নতুন

- Camera এখন অ্যাপের ভেতরেই খোলে (ফোনের camera app নয়)। Shutter-এর বাঁ দিকে শেষ তোলা ছবির thumbnail থাকে, চাপলে Preview পাতায় ছবি খোলে। Java বদলেছে (CAMERA permission), তাই নতুন APK বানাতে হবে (versionCode 14)।

- Photo editor এখন Snapseed/Lightroom-এর মতো: Curves, HSL, Split tone, Lens blur, Color splash, Heal/Clone/Dodge/Burn brush, Shapes, Stickers, Frames, Expand, ৩৪টা filter, Histogram, Auto white balance। Java বদলায়নি, তবু versionCode 13 তাই নতুন APK বানান।

- অ্যাপ দ্রুত খোলে (ভারী library দরকার হলে তবেই লোড হয়)।
- Customize > Design এখন ৬টা গুছানো গ্রুপে (কোনো option বাদ যায়নি)।
- Slider টানা মসৃণ। Java বদলেছে কি না: না, তবু versionCode 11 তাই নতুন APK বানান।

## New

- **Note tool:** Home > Tools গ্রিডের একদম প্রথমে (Doodle-এর আগে) Note বাটন। চাপলে Samsung Notes স্টাইলের এডিটর খোলে: Title, ⋮ menu (Search, Page sorter, Note style, Page settings, Full screen, Add note to, Collaborate, Add tags, Star, Lock, Print), নিচে toolbar (pen, checkbox, text color, text box, font, size, undo/redo)।
- **Add card:** + menu বা Home থেকে Add card চাপলে **Cards** পাতায় যায়।
- **Folder-এ note:** Files পাতায় folder বেছে "+ New note" / "+ Note" / + menu > Note চাপলে নোট ওই folder-এই তৈরি হয়; folder-এর file সংখ্যায় note-ও ধরা হয়।
- Bug fix: Notes-এর CSS ভুল জায়গায় (jsPDF library-র ভেতরে) বসে গিয়েছিল; ঠিক করা হয়েছে। Java বদলায়নি, তাই শুধু in-app update দিলেই চলে।

## New in 1.6

- **Cards পেজের উপরের বাটন লাইন fixed:** স্ক্রল করলেও Search, Customize, Export, Clear উপরে থাকে। Customize > Design > "Pin top buttons" দিয়ে বন্ধ করা যায়।
- **Total/Average কার্ডে Gain:** মাস অনুযায়ী (বা তারিখ অনুযায়ী) gain। নিজে থেকে সবচেয়ে নতুন মাস দেখায়; কার্ডের "All ▸" চাপলে সব মাস, আবার চাপলে Latest। নিচে Total gain (loss হলে লাল Total loss)।
- **Customize > Design > "Total card gain & top buttons":** Gain দেখান/লুকান, Month/Date, Period (Latest / All / নির্দিষ্ট মাস), Total লাইন দেখান/লুকান, Gain method, Sell reduces T.AMT at, Card order।
- **Files পেজ ঠিক:** বাটন ভুল হলে error screen-এ message আসে; Clear data দুইবার চাপলে মোছে; data না থাকলে Export-এ বার্তা।
- অ্যাপের নাম সব জায়গায় Data Show।

## New in 1.5

- Files front page (Search, Import, Tools, Export), 8 card layout (Classic, Portfolio, 1. Data Show, 2. Portfolio+, 3. Classic+, 4. Broker, 5. Compact row, 6. Tile), প্রতিটি layout-এর Fields / Size / Colors option, 12 card style।
- 1.4 feature: bottom menu, + menu (QR, ID card, PDF, Photo), website link box, share (Excel/PDF/text)।
- Java অংশ 1.4-এ বদলেছে; তাই APK নতুন করে build করে install করুন।

## New in 1.4

- **Bottom menu সব page-এ:** Files, Cards, Add (+), Table, Compare। নিচের ছোট handle-এ tap করলে menu লুকায়, আবার tap করলে ফিরে আসে। উপরের Scrap/Show/Table/Compare button আর লাগে না।
- **Files page:** Files চাপলে Documents (folder, recent files, viewer) খোলে। Customize থেকে Documents tab সরানো হয়েছে।
- **+ menu:** QR code, ID card, PDF, Photo (এর সাথে আগের Import files, New folder, Paste data, Add card)।
  - PDF/Photo/ID/QR বাছলে উপরের Preview box-এ দেখা যায়। ➤ (send) চাপলে data Cards পাতায় যায় (PDF), photo/ID Files-এ save হয়, QR-এ link থাকলে page খোলে, text থাকলে data হিসেবে যায়।
  - ⧉ copy, ⤴ share, ✕ close।
- **Website link box:** প্রথম page-এর উপরে link paste করলে page-এর text/table Preview box-এ আসে। সেখান থেকে text select করে copy করা যায়, বা ➤ দিয়ে select করা অংশ (select না করলে বড় table) Cards-এ পাঠানো যায়। Internet লাগে।
- **Share/Export:** Cards > Export menu-তে "Share Excel…" ও "Share PDF…" (Android share sheet)।
- **Card style (Data Show layout):** Date, বড় Name + Buy/Sell tag, Qty / T.Amt / T.Avg, Total, Gain, ডানে price box + T.Amt/T.Avg box। Customize > Design > Layout > "Data Show"। নতুন install-এ এটাই default।
- **Calculation:** Customize > Design > Calculation: Gain method (cheapest buys matched to sell qty / sell price − previous T.Avg) আর Sell reduces T.Amt at (previous T.Avg / sell price)।
- App-এর নাম Data Show, version 1.4 (versionCode 5)।
- Android অংশ বদলেছে (INTERNET permission, camera, share, ShareProvider), তাই এই version একবার APK build করে install করতে হবে। শুধু in-app update patch দিয়ে এই feature-গুলো পুরো কাজ করবে না।

## New in 1.3 — First screen + Customize Documents + offline viewers

- **First screen:** the original **Scrap / File import screen** opens on launch with the blue + navigation bar at the bottom, like screenshot 1. The original Scrap, Show, Table, Compare features are retained. When navigating away from the first screen the previous original top tabs return.
- **Documents is inside Customize:** Open **Show → Customize → Documents**. The folder cards, search, recent-file cards and actions appear only here, like screenshot 2. The previous Customize **Data, Design, App** tabs remain unchanged.
- **File viewer:** Tapping a file card opens its viewer. Newly imported PDFs can be viewed page by page (Previous/Next and zoom). Newly imported Excel/CSV files can be viewed as a grid; multi-sheet workbooks offer a sheet selector. Text and DOCX previews are also supported; image-only PDFs can be opened even when no rows can be extracted.
- **Original-file retention:** New imports store the original file in browser IndexedDB, with a small-file localStorage fallback (about 1.1MB). If the device rejects both storage methods or reaches its storage quota, a temporary preview works during the current app session only. These limits have not been confirmed on a physical Android device.
- **Old imports:** Previously stored files contain only extracted rows, not the PDF or XLSX itself. Viewer displays saved rows; re-import original for full PDF pages/workbook sheets.
- **Folders are existing projects.** Creating a new folder inside Documents adds a project. The current project for imports is still chosen using the original Project selector on the first page; folder filtering in Documents does not change that choice.
- In-app updates update `index.html` only and do not delete project data. Back up important data before installing app updates.

## APK বানানোর নিয়ম (Android Studio দিয়ে)

1. কম্পিউটারে **Android Studio** (নতুন সংস্করণ) install করুন।
2. এই zip খুলুন। Android Studio-তে **File > Open** দিয়ে `Data Show` ফোল্ডারটা খুলুন।
3. প্রথমবার নিচে "Gradle sync" চলবে, ইন্টারনেট লাগবে। শেষ হওয়া পর্যন্ত অপেক্ষা করুন।
   (SDK না থাকলে Android Studio নিজেই install করতে বলবে।)
4. উপরের মেনু: **Build > Build Bundle(s) / APK(s) > Build APK(s)**।
5. শেষ হলে নিচে "locate" লিংকে চাপুন। APK এখানে থাকবে:
   `app/build/outputs/apk/debug/app-debug.apk`
6. APK ফোনে পাঠান (USB, WhatsApp, Drive ইত্যাদি) এবং ফোনে খুলে install করুন।
   ফোনে "Unknown sources / অজানা উৎস থেকে install" অনুমতি দিতে হতে পারে।

কমান্ড লাইনে চাইলে: `./gradlew assembleDebug` (JDK 17 লাগবে)।

## ফোনে কীভাবে ব্যবহার হয়
- Upload box চাপলে ফোনের file বাছাই খোলে। PDF বা Excel (.xlsx/.xls/.csv) বাছুন।
- Excel/PDF বোতাম চাপলে file **Downloads** ফোল্ডারে সেভ হয়।

## নতুন যা যোগ হয়েছে
- **Show ট্যাব:** উপরে এক লাইনে ▾ (filter খোলা/বন্ধ), Search+Sort (একটা বক্সে), ⚙ Customize, ⬇ Export, 🗑 Clear।
- **Buy/Sell:** কার্ডের মাঝখানে (নাম আর Quantity-র মাঝে) দেখায়। Customize > Design > "Buy / Sell tag" থেকে Middle of card / Under quantity / Hidden বাছা যায়।
- **Price box:** Customize > Design > "Price box size"-এ Size (বড়/ছোট), Height, Width স্লাইডার। Total/Average কার্ডের price লেখা এখন অন্য কার্ডের সমান সাইজ।
- **Table ট্যাব:** Excel-এর মতো grid টেবিল (# নম্বর, আটকানো header, Buy/Sell ব্যাজ, সর্ট, Columns বাছাই)। "Same filters as Show" টিক দিলে Show ট্যাবের filter অনুযায়ী দেখায়।
- **Compare ট্যাব:** Compare by (Name / Date / Month / Buy-Sell / যেকোনো column), Split by (দুই দিক থেকে তুলনা), View (Table / Bars / Side by side), Items (যেগুলো তুলনা করবেন বাছুন), যেকোনো Measure (Cards, Qty, Avg, Min, Max, Value, Net, Share % ...)। দুইটা item বাছলে Diff ও Diff % দেখায়। সব Export চলে।

- **Table ট্যাব স্টাইল:** "Style" থেকে Grid / Data bars / Heatmap বেছে নেওয়া যায় (Looker-এর মতো)।
- **Compare ট্যাব চার্ট গ্যালারি:** "Chart type" বোতামে চাপলে Looker-এর মতো ছবির গ্যালারি খোলে: Table, Table + bars, Heatmap, Scorecard, Column, Stacked column, Bar, Stacked bar, Line, Area, Pie, Donut, Scatter, Side by side। Compare by / Split by / Items / Measure দিয়ে যেকোনো data যেকোনো ভাবে তুলনা করা যায়। Export-এ টেবিল ডাটা যায়।

- **Customize এখন ভাসমান (float):** নিচ থেকে উঠে আসা প্যানেল, তাই বদলানোর সময় উপরে কার্ড দেখা যায়। হেডারে টেনে উচ্চতা বাড়ানো/কমানো যায়, ▾ দিয়ে ছোট করা যায়, ✕ দিয়ে বন্ধ।

## মনে রাখুন
- ওয়েব সংস্করণের **Screenshot থেকে data পড়া** এই অ্যাপে নেই (ওটা শুধু Claude-এর ভেতরে চলে)।
- এটা debug APK, নিজের ব্যবহারের জন্য ঠিক আছে। Play Store-এ দিতে চাইলে আলাদা signing key লাগবে।
- সাইটের কোড: `app/src/main/assets/index.html`
- Android 9 বা তার নিচে প্রথমবার file সেভ করার অনুমতি চাইবে।

## Android Studio ছাড়া APK বানানো (GitHub দিয়ে)
1. github.com-এ বিনামূল্যে account খুলুন, **New repository** বানান (Private রাখতে পারেন)।
2. **uploading an existing file** লিংকে চাপুন এবং zip-এর **ভেতরের সব ফাইল ও ফোল্ডার** টেনে ছাড়ুন (zip নিজে নয়), তারপর **Commit changes**।
3. `.github` ফোল্ডার না উঠলে: **Add file > Create new file** চাপুন, নাম দিন `.github/workflows/build.yml` এবং এই zip-এর ওই ফাইলের লেখা পেস্ট করে commit করুন।
4. উপরের **Actions** ট্যাবে যান, **Build APK** বেছে **Run workflow** চাপুন।
5. ৫-১০ মিনিট পর সবুজ টিক এলে ওই run খুলুন, নিচে **Artifacts** থেকে `DataShow-apk` ডাউনলোড করুন। zip খুললে `app-debug.apk` পাবেন।

## ⚙ ইন-অ্যাপ আপডেট (APK আবার না বানিয়ে ফ্রন্টএন্ড বদলানো)

অ্যাপে **Show** ট্যাবে **⚙ Customize** চাপুন, তারপর **App** ট্যাব > **Update app** বোতাম। মেনু আসবে:

| মেনু | কাজ |
|---|---|
| Update from file | ফোনে রাখা zip বা txt আপডেট ফাইল বেছে প্রয়োগ |
| Paste update code | Claude-এর দেওয়া আপডেট কোড কপি করে পেস্ট করলেই হয় |
| Undo last update | শেষ আপডেট বাতিল |
| Save updated files | বদলানো ফাইলের zip Downloads-এ সেভ, GitHub-এ তুলে রাখার জন্য |
| Reset to original | APK-র মূল অবস্থা |

আপডেট ভুল হলে (লেখা মেলেনি ইত্যাদি) **কিছুই বদলায় না**, শুধু কারণ দেখায়। আপডেটের পর অ্যাপ চালু না হলে ৮ সেকেন্ড পর নিজেই "App did not load" ডায়ালগ আসে, সেখান থেকে **Undo update** চাপলেই আগের অবস্থায় ফেরে। সেভ করা ডাটা (প্রজেক্ট, থিম) আপডেটে মুছে যায় না।

### আপডেট ফাইলের দুই রকম
**১) Patch লেখা (শুধু বদলানো অংশ):**
```
#SRUPDATE
@@VERSION বোতামের রং বদল
@@FILE index.html
@@FIND
পুরনো লেখা (হুবহু)
@@REPLACE
নতুন লেখা
@@END
```
- একই @@FILE-এর নিচে একাধিক @@FIND ... @@END দেওয়া যায়।
- পুরনো লেখা ফাইলে একবারই থাকতে হবে। বহুবার থাকলে `@@FIND ALL` লিখুন।
- নতুন ফাইল বানাতে: `@@WRITE libs/নাম.js` ... `@@END`
- ফাইল মুছতে: `@@DELETE libs/নাম.js`

**২) zip:** ভেতরে ফাইলগুলো assets-এর পাথ ধরে রাখুন (যেমন `index.html`, `libs/abc.js`)। পুরো প্রজেক্ট zip দিলেও চলে, `app/src/main/assets/` এর ভেতরের ফাইলই শুধু নেওয়া হয়। zip-এর ভেতরে `patch.txt` থাকলে সেটাও প্রয়োগ হয়।

### সীমা
- শুধু `app/src/main/assets/` এর ফাইল (index.html, libs) আপডেট হয়। `MainActivity.java`, permission, আইকন বদলাতে হলে নতুন APK বানাতে হবে।
- নতুন APK install করলে ইন-অ্যাপ আপডেট মুছে মূল অবস্থায় ফেরে, তাই আগে 💾 দিয়ে ফাইল সেভ করে GitHub-এর `app/src/main/assets/` তে দিয়ে রাখুন।

## GitHub-এ APK বানানো
`.github/workflows/build.yml` প্রতিবার `main` ব্রাঞ্চে কিছু commit করলে (বা Actions > Build APK > Run workflow চাপলে) APK বানায়।
APK পাবেন দুই জায়গায়:
- **Releases** (রিপোর অধ্যায়ে ডান পাশে): ফোন থেকেই `app-debug.apk` সরাসরি ডাউনলোড করা যায়।
- **Actions > run > Artifacts**।

`app/debug.keystore` ফাইলটা repo-তে থাকা জরুরি। এটা থাকায় প্রতিটা নতুন APK আগেরটার ওপর install হয়, আনইনস্টল করতে হয় না।


## Website Smart Selection
Website khulle "Select" button: ek tap-e Table/Row/Column/Paragraph/Heading/List select, double-tap-e puro article, long-press-e nijer moto. Copy, Send to Data Show, Translate, Share, Search, Save, Speak, Dictionary, Highlight, History, Reader mode, Settings. AI/server lage na.


## Daily data (live table)
Table pate ekhon duita button: **My data** (apnar data) ar **Daily data** (DSE-er live share price table). Daily data khulle prothome last save kora data dekhay, tarpor internet theke live update hoy (Auto 10 s, Off/5/10/30/60 select kora jay). Column hide/show ar order (⚙ Columns), Hide table, Compact, Search, Sector/Category filter, header chaple sort. Sector ar EPS DSE page-e nei, tai "＋ Details" e Code soho paste korun. Shudhu `index.html` bodleche, tai In-app update dilei hobe.

"＋ Details" e ekhon **⬇ Auto fill** button ache: StockChartBD-er free API theke Sector, Category, EPS niye ashe (Daily data prothom khulle din-e ekbar nijei). Source: StockChartBD (DSE Intelligence), dsebd.org.

Auto fill ekhon ek request-e Sector, Category, EPS, NAV, ROE, Dividend, Risk score ane. Notun column: EPS year, NAV, P/B, ROE %, Div %, Div yield %, Risk score (⚙ Columns e tick din).
