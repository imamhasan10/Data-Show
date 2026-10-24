# Data Show — ব্যবহারের ম্যানুয়াল

এই ম্যানুয়ালে অ্যাপের সব অংশ ধাপে ধাপে বলা আছে। বাটনের নাম অ্যাপের ভেতরে যেমন (English) দেখায়, এখানেও তেমন রাখা হয়েছে।

---

## ১. কী বদলেছে

1. **অ্যাপ এখন দ্রুত খোলে।** আগে অ্যাপ খুললেই ৫টা ভারী library (PDF, Excel, PDF-বানানো, ZIP, QR) একসাথে লোড হতো। এখন সেগুলো **যখন যেটা দরকার তখনই** লোড হয়। যেমন Excel Export চাপলে তবেই Excel library লোড হয়। প্রথমবার ওই কাজে এক সেকেন্ডের মতো বেশি লাগতে পারে, তারপর স্বাভাবিক।
2. **Customize > Design পরিষ্কার করা হয়েছে।** আগে ৩০+টা option একটানা লম্বা লিস্টে ছিল। এখন ৬টা ভাঁজ-করা গ্রুপে সাজানো। **কোনো option বাদ যায়নি**, শুধু গুছিয়ে রাখা হয়েছে।
3. **Slider টানার সময় আর আটকায় না।** আগে slider-এর প্রতিটা নড়াচড়ায় সব card নতুন করে আঁকা হতো। এখন স্ক্রিন-রিফ্রেশ অনুযায়ী একবারই আঁকে, আর সেভ হয় একটু পরে।
4. অব্যবহৃত (dead) কোড মুছে কোড ছোট করা হয়েছে।
5. আপনার আগের সব সেভ করা ডেটা, design, preset, note ঠিক থাকবে। নতুন APK আগেরটার ওপর install হয় (আনইনস্টল লাগবে না)।

---

## ২. অ্যাপের কাঠামো

নিচে সবসময় একটা মেনু বার থাকে:

| বাটন | কাজ |
|---|---|
| **Home** | শুরুর পাতা: Search, Import, Tools, Folder, Recent files |
| **Show** | Website / scrap দেখা |
| **Cards** | ডেটা card আকারে দেখা (মূল পাতা) |
| **+ (Add)** | QR, ID card, Barcode, Import, Folder, Paste, Add card, Note |
| **Table** | ডেটা টেবিল আকারে |
| **Compare** | তুলনা করা |

নিচের মেনু লুকাতে বা দেখাতে ডানের ছোট হ্যান্ডেল (`navTog`) চাপুন।

---

## ৩. ডেটা ঢোকানো

- **Import files**: Excel (.xlsx .xls .csv .ods) অথবা PDF বেছে নিন।
- **Paste data**: কপি করা টেবিল/টেক্সট সরাসরি বসান।
- **Add card**: হাতে একটা entry যোগ করুন (Date, Name, Qty, Price, Type)।
- **QR / Barcode / ID card / Photo**: ক্যামেরা বা Gallery থেকে ছবি নিন। QR লিংক থাকলে বাটন হিসেবে দেখায়, চাপলে ওয়েবসাইট খোলে।
- **Website**: লিংক দিলে অ্যাপের নিজস্ব ব্রাউজার খোলে। সেখানে লেখা সিলেক্ট করে **Send text ➤** চাপলে ডেটা অ্যাপে আসে।

Column মিলানো: Cards পাতার উপরের Date / Name / Quantity / Price / Buy ড্রপডাউন থেকে ঠিক করে দিন কোন column কী।

---

## ৪. Cards পাতা

উপরের বাটন সারি (scroll করলেও উপরে আটকে থাকে):

- **🔍 Search** — নাম/তারিখ দিয়ে খুঁজুন।
- **Columns / Filter** — কোন filter দেখাবে।
- **Add files / Paste data** — দ্রুত ডেটা যোগ।
- **⚙ Customize** — ডিজাইন ও ডেটা এডিট (নিচে বিস্তারিত)।
- **⬇ Export** — Excel, PDF, Table PDF, Share।
- **🗑 Clear** — সব ডেটা মোছা (দুইবার চাপ লাগে)।

**Total / Average card**: নামের গ্রুপের সবার ওপরে। চাপলে খোলে। এখানে মোট Quantity, গড় দাম, T.AMT এবং Gain/Loss দেখায়।

**Type filter**: All / Buy / Sell / Net (Buy − Sell)।

---

## ৫. ⚙ Customize — সম্পূর্ণ গাইড

Customize খুললে তিনটা ট্যাব: **Data**, **Design**, **App**।

প্যানেলের মাথার ধূসর দাগ ধরে ওপর-নিচ টেনে সাইজ বদলানো যায়। `▾` দিয়ে ছোট করা যায়, `✕` দিয়ে বন্ধ।

### ৫.১ Data ট্যাব
টেবিলের মতো করে ডেটা সরাসরি এডিট/যোগ/মোছা যায়। উপরের ঘরে Date, Name, Qty, Price, Type লিখে **+ Add** চাপুন।

### ৫.২ Design ট্যাব — ৬টা গ্রুপ
প্রতিটা গ্রুপের নামে চাপলে খোলে/বন্ধ হয়। প্রথমে শুধু **Style** খোলা থাকে।

**🎨 Style**
- **Presets** — নাম দিয়ে বর্তমান ডিজাইন সেভ করুন (**Save current**), পরে এক চাপে ফিরিয়ে আনুন।
- **Card style** — ১২টা স্টাইল: Classic, Flat, Outline, Accent bar, Soft gradient, Bold fill, Dark, Minimal line, Ticket, Neon, Portfolio, Pastel paper।
- **Card layout** — Data Show (ডিফল্ট), Classic, Portfolio, Portfolio+, Classic+, Broker, Compact row, Tile, Data Show Lite (T.AVG ছাড়া), Phone classic। কিছু layout বাছলে নিচে বাড়তি প্যানেল আসে: Fields (কোনটা দেখাবে, label, কোন জায়গায়, নতুন লাইন, নিজের field), Size and spacing (Name, Label, Number, Price box…), Colors।
- **Card font** — Style default, System, Serif, Mono, Rounded, Condensed, Casual।
- **Font size** — Small / Normal / Large।
- **Corners** — Round / Square।
- **Shadow** — On / Off।

**🌈 Colors**
- **All cards** — একটা রঙ বেছে সব card-এ লাগান; **Auto** দিলে নাম অনুযায়ী আলাদা রঙ।
- **Color by name** — প্রতিটা নামের জন্য আলাদা রঙ।
- **Card parts** — Card color (Group colors / White), Price box (Group / Blue / Green), Total card (Modern / Group color)।
- **Text color & box** — সব লেখা বা আলাদা আলাদা অংশের (Date, Name, Label, Value, Price) রঙ ও পেছনের box।

**🧾 Card content**
- **Show / hide info** — Single card এবং Total card-এর কোন তথ্য দেখাবে (Date, Qty, Price, T.AMT, T.AVG, Tag, Extra, Cost, Avg, P/L, Last, Min, Max…) টিক দিয়ে ঠিক করুন।
- **Quantity & total** — Buy/Sell tag-এর জায়গা (Middle / Under quantity / Hidden), Quantity label (Quantity / Qty / Total / Hidden), Total column (Off / Separate column / Instead of Quantity), Total counts (Buy − Sell চলতি ব্যালেন্স, অথবা শুধু Buy)।
- **Price box size** — দামের box-এর Size, Height (0 = auto), Width (0 = auto)।

**📊 Total card & gain**
- **Total card position** — Top (চাপলে খোলে) অথবা Bottom।
- **Gain on total card** — Show / Hide।
- **Gain grouped by** — Month / Date।
- **Gain period** — Latest month, All, অথবা নির্দিষ্ট মাস।
- **Total gain / loss line** — নিচের মোট লাইন Show / Hide।
- **Gain method** — Lowest earlier buy (ডিফল্ট) / Cheapest buys matched / Buy average।
- **Sell reduces T.AMT at** — Sell price অথবা Previous T.AVG।

**📄 Lists & pages**
- **Groups start** — Open / Collapsed।
- **Card order (same name)** — Oldest first / Newest first।
- **Cards per page** — 20, 50, 100, 250, 500, All at once। (ফোন ধীর লাগলে 20 বা 50 রাখুন।)
- **Page buttons** — Show / Hide।
- **Filters per row** — 2 থেকে 5।
- **Pin top buttons** — উপরের বাটন সারি আটকে রাখা On/Off।

**🛠 Advanced (fine-tune)**
সবকিছু খুঁটিনাটি: Font family, প্রতিটা লেখার Size/Weight/Color, Card background (tint/tone/shade), Border, Corner radius, Padding, Gap, Shadow, Price box, Total card, Filters। এখানকার **Reset all customization** শুধু এই Advanced মানগুলো ফেরত দেয়।

গ্রুপগুলোর নিচে **Reset design** — পুরো design ডিফল্টে ফেরত দেয়। সব পরিবর্তন নিজে নিজেই সেভ হয়।

### ৫.৩ App ট্যাব
**Update app** — প্যাচ বা zip দিয়ে অ্যাপের ফ্রন্টএন্ড বদলানো (নিচে "আপডেট" অংশ দেখুন)।

---

## ৬. গণনার নিয়ম (Gain / T.AMT / T.AVG)

প্রতিটা নামের জন্য সব সারি পুরনো থেকে নতুন ক্রমে ধরা হয়:
- **TOTAL** = Buy যোগ, Sell বিয়োগ (চলতি ব্যালেন্স)।
- **T.AMT** = চলতি মোট টাকা। Sell হলে কমে (Sell price বা আগের T.AVG দামে, Customize-এ ঠিক করা যায়)।
- **T.AVG** = গড় দাম।
- **Gain** = প্রতিটা Sell-এর লাভ/লোকসান, বেছে নেওয়া Gain method অনুযায়ী।

নমুনা: Gain +4.80 / +0.70 / −6.20; TOTAL 1,2,3,1,2,3,4।

---

## ৭. Home-এর Tools

PDF: **Edit PDF, Merge PDF, Split PDF, Lock PDF, Watermark, Compress PDF, Sign, Smart erase, Extract text, To Word, Edit Word**।
স্ক্যান: **Docs, ID card, Passport, Book, Images**।
অন্যান্য: **Note** (ফোল্ডারেও বানানো যায়), **Doodle**।

PDF দেখার সময় নিচে বার আসে: Search, Pages (thumbnail), Marks (bookmark), Markup (pen, highlight, text…), OCR (ইন্টারনেট লাগে একবার)।

---

## ৮. Export ও Share

- **Excel (.xlsx)**, **CSV**, **PDF (card)**, **PDF (table)**, **Copy table**।
- Android-এ ফাইল **Downloads** ফোল্ডারে সেভ হয়। Cards-এর Export মেনু থেকে Share-ও করা যায়।

---

## ৯. APK বানানো (GitHub দিয়ে)

1. এই zip-এর ভেতরের সব ফাইল GitHub repo-র **root**-এ আপলোড করুন।
2. **Actions > Build APK > Run workflow** চাপুন।
3. শেষ হলে **Artifacts** থেকে `DataShow-apk` অথবা **Releases** থেকে `app-debug.apk` নামিয়ে ফোনে install করুন।
4. পুরনো অ্যাপের ওপরেই install হবে (একই signing key), ডেটা থাকবে।

JDK 17 থাকলে নিজের পিসিতেও চলে: `./gradlew assembleDebug`।

---

## ১০. আপডেট (APK ছাড়া শুধু ফ্রন্টএন্ড)

Customize > App > **Update app**। শুধু `index.html` বদলায়। Java বদলাতে হলে (যেমন Share, ব্রাউজার, ক্যামেরা) নতুন APK লাগবে। প্যাচের ফরম্যাট `example-update.txt`-এ আছে।

---

## ১১. সমস্যা হলে

| সমস্যা | সমাধান |
|---|---|
| অ্যাপ ধীর লাগছে | Customize > Design > **Lists & pages** > Cards per page 20 বা 50 করুন |
| Excel/PDF প্রথমবার একটু দেরিতে খোলে | স্বাভাবিক — library তখনই লোড হচ্ছে। দ্বিতীয়বার দ্রুত |
| কোনো বাটনে কিছু হয় না | স্ক্রিনের নিচে লাল/লেখা বার্তা (toast) পড়ুন; কোন বাটন তা জানান |
| Design এলোমেলো | Customize > Design > নিচে **Reset design** |
| ডেটা মুছে গেল ভয় | Clear দুইবার চাপলে তবেই মোছে; আগে Export Excel করে রাখুন |
| QR পড়ে না | Photo editor-এ crop/rotate/Magic/B&W করে আবার **Scan** চাপুন |

---

## ১২. গুরুত্বপূর্ণ নাম (বদলাবেন না)

আপডেট/ডেটা ঠিক রাখতে এগুলো পরিবর্তন করা যাবে না: package `com.sharepricereader.app`, IndexedDB `ShareReader_OriginalFiles_v1`, localStorage `ds_projects_v1`, `ds_design_v1`, `ds_theme_v1`।

## Update v11 — Daily data
- Default column: Change % er jaygay **YCP** (ager din-er price). Change % lagle Columns theke on kora jay.
- **➤ Send**: table-ta share sheet diye Excel file hishabe pathay. **⬇ Export**: Excel / CSV / Copy table.

## Update v12 — Clear
- **Clear** ekhon duibar tap korle shob data muche (ager moto ek tap-e na). Bhul kore chaple data jabe na.

## Update v13
- Cards: Select button nei. Card-e **chepe dhore rakho** (~0.5 sec), left-e radio ashbe. Aro card tap kore select koro, **All** diye shob, tarpor **Delete** (duibar tap).
- **Clear** korleo upore toolbar (Add files, Paste, Customize, Export) thake.
- Daily data > **➤ Send**: Cards / Compare / My data / Share Excel file — jekhane chao pathao.

## Update v14
- PDF/Excel library age local `libs/` folder theke load hoy (app-e offline-o kaj kore). Local file na thakle (web preview-te) online copy theke load hoy. Dui option-i ache.

## Update v15
- PDF import: Date / Name / Quantity / Price / Type column-er shothik naam ekhon filter-e ashe (ager moto "Col 1, Col 3" na).
- Daily data > **📋 Paste**: dsebd.org-er price table copy kore paste korle shob share-er data ashe (internet na thakleo). Daily card-o ei data theke price dekhay.

## My portfolio (Table page)
1. Table > "💼 My portfolio" chaple panel khole.
2. Prothome **📕 Portfolio PDF** dien. App share name, qty, avg cost ar date dekhabe. Thik thakle Save, vul thakle lekha thik kore Save. PDF na porle nije lekhen: `NAME QTY AVG` (protiti line e ekta).
3. Tarpor shudhu **🧾 Buy/Sell PDF** dien (ekshathe onek file dewa jay). Share date onujayi portfolio te jog/biyog hoy. Portfolio date er ager ba same dine r trade ghunano hoy na (ta portfolio PDF e ache).
4. **📧 Gmail**: Gmail theke notun Buy/Sell PDF ene dey. Auto na hole file bachar option khule jay. Auto er jonno ⚙ e Gmail address ar App password (Google Account > Security > 2-Step Verification > App passwords) din.
5. **➤ Send to Cards**: portfolio ta Cards page e pathay ("My portfolio" naame). Ekbar pathale pore jog hole nijei update hoy.
6. Ekoi confirmation bar bar dile double hoy na. Trade list theke ✕ diye mucha jay. 🗑 Reset duibar chaple sob muche.
