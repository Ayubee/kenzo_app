# Kenzo App

O'zbekcha interfeysli, oflayn Android rejalashtiruvchi. Bugungi vazifalarni saqlang, ixtiyoriy vaqt belgilang va oldingi kunlarni alohida tarixda ko'ring.

**Joriy tuzatishlar: Unreleased.** Ish davom ettirilganda diskda **1.2.0 / 3**, iliq minimal dizayn, pastdan ochiladigan vaqt tanlagichi va mavjud 1.2.0 APK bor edi. Klaviatura, mavzu va katta shriftga oid hozirgi tuzatishlar faqat debug'da; mavjud APK qayta yig‘ilmadi va bu tuzatishlarni o‘z ichiga olmaydi. Versiya va imzolash kalitlari saqlandi. [Joriy tekshiruv](docs/UNRELEASED-VERIFICATION.md) · [Xavfsizlik auditi](docs/SECURITY-AUDIT.md).

[English README](README.md) · [Amaliy tekshiruvlar](docs/VERIFICATION.md) · [Uch agent tekshiruvi](docs/REVIEW.md) · [O'zgarishlar tarixi](CHANGELOG.md)

## Haqiqiy skrinshotlar

Loyihaning bitta Android 16 emulatorida ishlayotgan Unreleased debug'dan olingan. Namuna vazifalar tekshiruv uchun yaratilgan; rasmlar maket emas.

| Yorug' | Qorong'i |
| --- | --- |
| ![Kenzo App yorug' mavzuda](docs/screenshots/unreleased-priority-light.png) | ![Kenzo App qorong'i mavzuda](docs/screenshots/unreleased-priority-dark.png) |

![Mavzu va eslatma sozlamalari](docs/screenshots/unreleased-settings.png)

## Imkoniyatlar

- Akkaunt va serversiz, qurilmada saqlanadigan foydalanuvchi nomi.
- Vazifa qo'shish, tahrirlash, bajarish, qaytarish va tasdiq bilan o'chirish.
- Saqlanadigan uch darajali prioritet; avval prioritet, uning ichida vaqt bo'yicha tartiblash. Vaqtsizlar o'z darajasidagi vaqtlilardan keyin ko'rsatiladi.
- Sanalar bo'yicha tarix; eski yozuvni saqlagan holda bugunga yangi nusxa qo'shish.
- Saqlanadigan **Tizimga mos / Yorug' / Qorong'i** mavzular.
- Vazifadan besh daqiqa oldingi eslatma va bajarilmasa ixtiyoriy +10/+30 daqiqalik ikki takror. Yuborilgan slotlar saqlanadi, o'tgan slotlar qayta yuborilmaydi. Aniq signal ruxsatisiz eslatma kechikishi mumkin.
- Bildirishnoma tegishli vazifani ochadi; `Bajarildi` amali bazani yangilab, qolgan eslatmalarni bekor qiladi. Faqat ochish bajarildi degani emas.
- Bugungi hisob/progress, 200 ms sokin animatsiya va ixtiyoriy yengil vibratsiya; saqlanadigan sozlamalar va tizim cheklovlarini hurmat qilish.
- K + galochka rasmiy adaptive/legacy ikonkalari va sun'iy kutishsiz standart Android splash.
- Iliq minimal ranglar; +15/+30/+60 daqiqalik kumulyativ tugmalar, boshqa vaqt va vaqtsiz qoldirish imkoniyatli, aylantiriladigan vaqt tanlagichi.
- Sozlamalarda haqiqiy ruxsat holati va ruxsatni tiklash tugmalari.
- Bir xil package ID va imzo bilan ma'lumotlarni saqlab yangilash.

Dizayn berilgan rasmdagi iliq fon, yirik matn, sariq aksent, yumaloq konturli kartalar va surilgan soyalarni yumshoqroq vazifalar interfeysiga moslaydi.

## Texnologiyalar

Kotlin, Jetpack Compose, Material 3, ViewModel/StateFlow, coroutines, SQLite, SharedPreferences va AlarmManager. Tuzilish: `data/`, `notification/`, `theme/`, `ui/` va `util/` qatlamlari.

Minimal Android: **7.0 / API 24**. Target va compile SDK: **36**. Ilovada Internet ruxsati va backend yo'q. Android backup sozlamalari alohida ishlaydi; ilova shifrlangan ombor deb taqdim etilmaydi.

## Ishga tushirish

Sozlangan Windows kompyuterida:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\run-android.ps1
```

Buyruq mavjud `Kenzo_API_36` emulatorini ishlatadi, debug build yaratadi, `adb install -r` bilan yangilaydi va ilovani ochadi. Faqat emulator uchun `-EmulatorOnly` qo'shing. [Birinchi sozlash yo'riqnomasi](ANDROID-DEVELOPMENT.md).

Boshqa kompyuterda Android SDK platform 36 va build tools o'rnating, `local.properties` ichida `sdk.dir` ni belgilang va Gradle/AGP'ga mos JDK ishlating. Loyiha Java 17 toolchain talab qiladi.

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

Diskda avvaldan mavjud APK: **[kenzo-app-1.2.0.apk](kenzo-app-1.2.0.apk)**: `versionName=1.2.0`, `versionCode=3`, package: `com.example.kunlikvazifalar`. U hozirgi Unreleased tuzatishlaridan oldingi build. Bu davom ettirishda yangi release APK yaratilmadi. [Avvalgi 1.1.0](kenzo-app-1.1.0.apk) tarixiy build sifatida saqlandi.
Mavjud release kaliti maxfiy `keystore.properties` orqali ishlatiladi. Kalit va parollarni Git'ga kiritmang. Debug va release imzolari farqli: ma'lumotlarni saqlab yangilash uchun bir xil imzo yo'nalishida qoling.

## Tekshiruv va cheklovlar

[Tekshiruv hisobotida](docs/VERIFICATION.md) bajarilgan sinovlar va cheklovlar alohida berilgan. Bitta API 36 emulatori haqiqiy telefonlar, boshqa Android versiyalari yoki ishlab chiqaruvchilarning batareya cheklovlari tekshirilganini anglatmaydi. Eslatma vaqti Android ruxsatlari va quvvat boshqaruviga ham bog'liq.

Versiyalash: tuzatish — patch, mos yangi imkoniyat — minor, moslikni buzish — major. Har bir chiqarilgan APK'da `versionCode` oshiriladi. Mavzu tanlovi qo'shilgani uchun dastlabki **1.0.0 tekshirish build'i** dan **1.1.0** ga o'tildi.
