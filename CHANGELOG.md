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

- Klaviatura ochilganda qo‘shish, tahrirlash, qayta qo‘shish va sozlamalar dialoglari IME joyini hisobga oladi; asosiy amallar klaviatura ustida qoladi.
- Material 3 dialog va konteyner ranglari mavjud iliq light/dark mavzuga moslashtirildi; standart binafsha fon olib tashlandi.
- Pastdan ochiladigan vaqt tanlagichi aylantiriladi; tugmalar katta shriftga mos balandlik oladi.
- Har bir kartochkada prioritetga mos qizg‘ish chegara, HIGH uchun sokin fon; bajarilganda qizil urg‘u kamayadi. Kichik ro‘yxatda ham progress chizig‘i, nol holatidagi aniq hisob va ixcham rag‘bat matni saqlanadi.
- Vaqt tanlagichini ochishda matn fokusini bo‘shatish keraksiz klaviatura qaytishini kamaytiradi.
- Joriy debug tekshiruvlari va xavfsizlik auditi hujjatlashtiriladi. Bu davom ettirishda release yig‘ilmadi, diskda avvaldan bor 1.2.0/3 va APK saqlandi.

## [1.2.0] - 2026-10-07

### Qo‘shildi va yangilandi:
- **3-variant: Pastdan chiquvchi yangi vaqt tanlagich (ModalBottomSheet)**:
  - Material 3 `ModalBottomSheet`, yuqori burchaklari 24dp yumaloq, drag handle bilan.
  - Sarlavha: "Vaqtni tanlang" (chapga, qalin).
  - Katta vaqt ko'rsatkichi: 60sp ExtraBold, 24 soatlik (HH:mm) format, yumaloq konteyner ichida.
  - Vaqt o'zgarganda 200 ms yengil animatsiya (tizim va ilova animatsiya sozlamalariga mos).
  - 2x2 tezkor kumulyativ tugmalar: "+15 daqiqa", "+30 daqiqa", "+1 soat", "Boshqa vaqt" (qo'shimcha picker).
  - 24 soat doirasida aylanuvchi kumulyativ vaqt hisoblash (23:50 + 15 daq = 00:05).
  - To'liq kenglikdagi oltin/sariq (#F2C044) "Saqlash" tugmasi, to'q qalin matn (~52dp).
  - To'liq kenglikdagi nozik chegarali "Bekor qilish" tugmasi.
  - Vaqt ixtiyoriy bo'lgani uchun "Vaqtsiz qoldirish" imkoniyati to'liq saqlandi.
  - Alohida qayta ishlatiluvchi `KenzoTimePickerBottomSheet` komponenti.
  - Light va Dark mavzularini to'liq qo'llab-quvvatlaydi.
- **4-variant: "Iliq minimal" vizual dizayni**:
  - Asosiy fon: iliq bej (`#F5EDE2` yorug' / `#1E1B18` qorong'i).
  - Kartochkalar: och krem (`#FFFAF3` yorug' / `#2A2521` qorong'i).
  - Asosiy matn: to'q kulrang (`#282522` yorug' / `#F4EDE4` qorong'i).
  - Asosiy UI aksenti: sokin terrakota (`#B85E3C` yorug' / `#D47A57` qorong'i).
  - Sokin yashil galochka (`#4E7D55` yorug' / `#65996C` qorong'i) 200 ms silliq animatsiya bilan.
  - 3 darajali prioritet nishonlari ("Muhim emas", "Muhim", "Juda muhim").
  - Haqiqiy progress: "Bugun X ta vazifadan Y tasi bajarildi" va "Y / X bajarildi", ixcham nuqtalar.
  - Kenzo'ning rasmiy sariq "K + galochka" logosi saqlandi.
- **Versiyalash va reliz**:
  - `versionName = "1.2.0"`, `versionCode = 3`, interfeysda "1.2.0v".
  - Rasmiy keystore bilan imzolangan `kenzo-app-1.2.0.apk` tayyorlandi (v2 sxemasi, sertifikat mosligi tekshirildi).
  - Barcha unit testlar muvaffaqiyatli o'tdi (`.\gradlew.bat test`).
  - Emulator'da Light/Dark rejimlari, kumulyativ tugmalar va saqlash to'liq tekshirildi.

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
