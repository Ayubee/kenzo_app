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

## [1.0.0] - 2026-10-07
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
