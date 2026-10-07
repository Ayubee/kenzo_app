# Kunlik Vazifalar (Android)

Faqat Android uchun mo‘ljallangan, yengil, tezkor, sodda va 100% oflayn ishlaydigan kundalik vazifalar ilovasi.

## Xususiyatlar
- **100% Oflayn & Xavfsiz**: Hech qanday server, tashqi API, akkaunt yoki internet talab qilinmaydi. Ma’lumotlar qurilmaning ichki SQLite bazasida saqlanadi.
- **O‘zbekcha interfeys**: Toza, o‘qilishi oson va qulay dizayn (Material 3).
- **Birinchi kirish**: Faqat foydalanuvchi ismi so‘raladi, saqlanadi va qayta so‘ralmaydi. Kirganda darhol `[Ism], xush kelibsiz!` yozuvi bilan ochiladi.
- **Vazifalar boshqaruvi**:
  - "+" tugmasi orqali vazifa qo‘shish (matn majburiy, vaqt ixtiyoriy).
  - Takroriy bosishdan himoya (debouncing).
  - Vaqtli vazifalar xronologik tartibda, vaqtsizlari keyin turadi.
  - Galochka orqali bajarilganlar bo‘limiga o‘tkazish va qayta faollashtirish.
  - 3 nuqtali menyu orqali tahrirlash va tasdiq bilan o‘chirish.
- **Tarix bo‘limi**:
  - O‘tgan kunlar vazifalari sana bo‘yicha guruhlanadi.
  - "Bugunga qayta qo‘shish" funksiyasi orqali eski tarix saqlangan holda bugunga yangi nusxa yaratiladi.
- **Mahalliy eslatmalar**:
  - Belgilangan vaqtdan 5 daqiqa oldin bildirishnoma beradi: `5 daqiqadan keyin «[vazifa matni]» ishini qilishingiz kerak.`
  - Vazifa bajarilsa yoki o‘chirilsa eslatma bekor qilinadi.
  - 5 daqiqadan kam qolganda o‘tgan vaqtga eslatma rejalashtirilmaydi.
  - Telefon qayta yoqilganda (BOOT_COMPLETED) kelgusi faol eslatmalar avtomatik tiklanadi.

---

## Versiyalash qoidalari (AI va dasturchilar uchun ko‘rsatma)
- **Hozirgi versiya**: `1.0.0v` (versionCode: 1, versionName: "1.0.0")
- **Kichik tuzatish (patch)**: `1.0.1` (xatoliklar va barqarorlik)
- **Yangi funksiya (minor)**: `1.1.0` (yangi imkoniyatlar)
- **Katta o‘zgarish (major)**: `2.0.0` (moslikni buzuvchi o‘zgarishlar)
Har bir chiqariladigan yangi APK’da `app/build.gradle.kts` ichidagi `versionCode` +1 ga oshiriladi va `versionName` yangilanadi.

---

## Loyihani yig‘ish (Build)
```bash
# Debug APK
.\gradlew.bat assembleDebug

# Imzolangan Release APK
.\gradlew.bat assembleRelease

# Testlarni yurgizish
.\gradlew.bat test
```

Tayyor imzolangan APK fayllar:
- `D:\kenzo_app\KunlikVazifalar-1.0.0.apk`
- `D:\kenzo_app\app\build\outputs\apk\release\app-release.apk`
