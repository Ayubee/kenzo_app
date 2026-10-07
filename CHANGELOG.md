# O‘zgarishlar tarixi (CHANGELOG)

Barcha versiyalar Semantic Versioning (SemVer) qoidalariga asoslanadi.

## Versiyalash qoidalari (Versioning Policy)
- **Kichik tuzatish (Patch / bug fix)**: Masalan, `1.0.0` -> `1.0.1` (xatoliklar va kichik tuzatishlar).
- **Yangi funksiya (Minor feature)**: Masalan, `1.0.0` -> `1.1.0` (orqaga mos keluvchi yangi imkoniyatlar qo‘shilganda).
- **Katta o‘zgarish (Major change)**: Masalan, `1.0.0` -> `2.0.0` (moslikni buzadigan katta o‘zgarishlar).

Har bir yangi chiqariladigan APK uchun `app/build.gradle.kts` faylida:
- `versionCode` qiymati +1 ga oshiriladi (masalan, 1 -> 2);
- `versionName` SemVer qoidasi bo‘yicha yangilanadi (masalan, "1.0.0" -> "1.0.1");
- Interfeysdagi ko‘rinish formati: `[versionName]v` (masalan, "1.0.0v").

---

## [Unreleased]

### "Iliq minimal" (Warm Minimal - 4-variant) dizayn yangilanishi:
- **Ranglar va uslub**:
  - Asosiy fon: iliq bej (`#F5EDE2` yorug' / `#1E1B18` qorong'i).
  - Kartochkalar: och krem (`#FFFAF3` yorug' / `#2A2521` qorong'i).
  - Asosiy matn: to'q kulrang (`#282522` yorug' / `#F4EDE4` qorong'i).
  - Asosiy UI aksenti: sokin terrakota (`#B85E3C` yorug' / `#D47A57` qorong'i).
  - Nozik chegaralar (`outlineVariant`), 18.dp yumaloq burchaklar va yengil soyalar.
  - Kenzo'ning rasmiy sariq "K + galochka" logosi o'zgartirilmasdan saqlandi.
- **Asosiy ekran va progress**:
  - "[Username], xush kelibsiz!" sarlavhasi va ixcham Kenzo App sarlavhasi.
  - "Bugun X ta vazifadan Y tasi bajarildi" va "Y / X bajarildi" real hisobli progress.
  - Vazifalar soni oshganda ham buzilmaydigan ixcham progress nuqtalari.
  - Bitta ustunda vazifa kartochkalari, terrakota rangli yumaloq "+" tugmasi (FAB).
  - Pastda "Bugun" va "Tarix" kapsula ko'rinishidagi navigatsiya.
- **Kartochkalar va prioritet**:
  - Galochka: sokin yashil kvadrat (`#4E7D55` yorug' / `#65996C` qorong'i), oq belgi va 200 ms silliq animatsiya.
  - Bajarilgan vazifa matni ustidan chizilgan chiziq to'liq o'qiladigan darajada saqlangan.
  - Prioritet belgilari: "Muhim emas" (neytral), "Muhim" (yengil qizg'ish/och pushti), "Juda muhim" (kuchliroq qizil nishon), terrakota amallardan aniq ajralib turadi.
  - Soat belgili aniq vaqt indikatori va 3 nuqtali menyu.
- **Vaqt tanlash va dialoglar**:
  - Tizimning standart binafsha ranglari to'liq olib tashlandi; sarlavha, soat ko'rsatkichlari va tanlov terrakota rangiga moslandi.
  - Inglizcha tugmalar o'rniga o'zbekcha "Tanlash" va "Bekor qilish" yozuvlari kiritildi.
- **Dark Mode jufti**:
  - Sof qora o'rniga chuqur iliq to'q fon (`#1E1B18`) va krem matn (`#F4EDE4`).
  - Tizimga mos / Yorug' / Qorong'i mavzu sozlamasi va uning doimiy saqlanishi to'liq ishlaydi.
- **Mavjud funksiyalar va xavfsizlik**:
  - Foydalanuvchi ma'lumotlari ("Kenzo" profili va 7 ta vazifa) to'liq saqlandi.
  - Release APK yaratilmadi va tarqatiladigan versiya (`1.1.0 / 2`) o'zgartirilmadi (faqat alohida so'rovdan keyin chiqariladi).
- **Tekshiruvlar**:
  - Barcha 13 ta unit testlar muvaffaqiyatli o'tdi (`.\gradlew.bat test`).
  - `run-android.ps1` orqali `Kenzo_API_36` emulatorida tekshirildi; skrinshotlar olindi.

## [1.1.0] - 2026-10-07

### Qo'shildi va yaxshilandi

- Ilova nomi va Android yorlig'i: **Kenzo App**; yangi launcher belgisi.
- Berilgan rasm asosida iliq fon, yumshoq konturlar/soyalar va rangli aksentlar.
- Saqlanadigan tizimga mos, yorug' va qorong'i mavzular; tizim paneli kontrasti.
- Sozlamalarda bildirishnoma va aniq signalning haqiqiy holati, runtime ruxsat va Android sozlamalariga o'tish.
- Ruxsat berilmagan holatlarda rost xabarlar; aniq signal yo'q bo'lsa taxminiy eslatma kechikishi tushuntiriladi.
- Eslatmalarni yangilanish, vaqt/ruxsat o'zgarishlarida tiklash; eskirgan alarmni DB bilan tekshirish.
- SQLite xatolari, sana/vaqt validatsiyasi, draftlarni saqlash va klaviaturadagi dialog sig'ishi tuzatildi.
- Inglizcha/o'zbekcha portfolio README, haqiqiy emulator skrinshotlari va tekshiruv hisobotlari.
- `versionName=1.1.0`, `versionCode=2`. Package ID va mavjud release imzolash kaliti saqlanadi.

Amalda bajarilgan sinovlar va cheklovlar: [VERIFICATION.md](docs/VERIFICATION.md).

## [1.0.0] - 2026-10-07 — dastlabki tekshirish build'i
### Qo‘shildi:
- **Dastlabki kirish (Onboarding)**:
  - Faqat username so‘rash (bo‘sh va probeldan iborat ismlar qabul qilinmaydi).
  - Ism xotirada saqlanadi va keyingi kirishlarda to‘g‘ridan-to‘g‘ri asosiy ekran ochiladi.
  - Bildirishnoma ruxsatini (POST_NOTIFICATIONS) birinchi kirishda tushuntirish va so‘rash.
- **Asosiy ekran (Bugun)**:
  - "[username], xush kelibsiz!" sarlavhasi va interfeysda "1.0.0v" versiyasi.
  - "+" tugmasi orqali bugun uchun yangi vazifa qo‘shish (matn majburiy, vaqt ixtiyoriy).
  - Ketma-ket bosish orqali takroriy vazifa qo‘shilishini oldini olish (debouncing/lock).
  - Vaqtli vazifalarni xronologik tartiblash, vaqtsizlarini ulardan keyin joylashtirish.
  - Galochka (Checkbox) orqali vazifani "Bajarilganlar" bo‘limiga o‘tkazish va qaytarish imkoniyati.
  - Bo‘sh holat (empty state) xabari.
- **Tahrirlash va o‘chirish**:
  - 3 nuqtali menyu orqali vazifa matni va vaqtini tahrirlash.
  - O‘chirishdan oldin tasdiq oynasi.
- **Tarix bo‘limi**:
  - O‘tgan kunlar vazifalarini sana bo‘yicha guruhlab ko‘rsatish.
  - Vazifaning bajarilgan/bajarilmagan holati.
  - "Bugunga qayta qo‘shish" tugmasi: eski yozuv tarixda saqlanib, bugun uchun yangi nusxa yaratiladi.
- **Eslatmalar (Local notifications)**:
  - Vaqtli vazifadan 5 daqiqa oldin bildirishnoma: `"5 daqiqadan keyin «[vazifa matni]» ishini qilishingiz kerak."`.
  - Vazifa bajarilsa yoki o‘chirilsa eslatmani bekor qilish.
  - Vaqtgacha 5 daqiqadan kam qolgan bo‘lsa o‘tgan vaqtga eslatma o‘rnatmaslik va ogohlantirish.
  - Telefon qayta yoqilganda (BOOT_COMPLETED) kelajakdagi faol eslatmalarni tiklash.
- **Sozlamalar**:
  - Foydalanuvchi ismini o‘zgartirish va 1.0.0v versiyasi ko‘rsatkich.
- **Imzolangan Release APK**:
  - Barqaror release keystore va v2 imzo bilan yig‘ilgan `KunlikVazifalar-1.0.0.apk`.
